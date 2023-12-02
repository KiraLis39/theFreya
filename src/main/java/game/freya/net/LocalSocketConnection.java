package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.NetDataType;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class LocalSocketConnection {
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    @Getter
    private final Set<HeroDTO> otherHeroes = HashSet.newHashSet(3);
    private ObjectOutputStream oos;
    private Thread connectionThread, connectionLiveThread;
    private Socket socket;
    private String host;
    private int port;
    private GameController gameController;
    private volatile String lastExplanation;
    private volatile long lastDataReceivedTimestamp;
    @Getter
    private volatile boolean isPing;

    public synchronized void openSocket(String host, Integer port, GameController gameController, boolean isPing) {
        this.gameController = gameController;
        this.host = host;
        this.port = port != null ? port : Constants.DEFAULT_SERVER_PORT;
        this.isPing = isPing;

        // убиваемся, если кто-то запустил нас ранее и не закрыл:
        killSelf();

        connectionThread = new Thread(() -> {
            try (Socket client = new Socket(host, port != null ? port : Constants.DEFAULT_SERVER_PORT)) {
                this.socket = client;
                this.socket.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
                this.socket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                this.socket.setReuseAddress(true);
                // this.socket.setKeepAlive(true);
                this.socket.setTcpNoDelay(true);

                if (!gameController.isServerIsOpen()) {
                    // если сервер локальный - нет смысла ставить себе таймаут, т.к. broadcast Сервера всё равно не возвращается обратно:
                    // this.socket.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT); // todo: включить после отладки
                }

                try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
                    this.oos = outs;
                    log.info("Socket connection to '{}:{}' is ready!", host, port);

                    try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                        ClientDataDTO readed;
                        while ((readed = (ClientDataDTO) inps.readObject()) != null && !connectionThread.isInterrupted()) {
                            parseNextData(readed);
                        }
                        log.info("Завершена работа клиентского соединения с Сервером.");
                    }
                    log.info("Завершена работа inputs клиентского соединения с Сервером.");

                    // завершение соединения:
                    killSelf();
                }
                log.warn("Завершена работа всех соединений текущего подключения с Сервером.");
            } catch (EOFException eof) {
                log.error("При работе потока сокетного соединения с Сервером произошла ошибка: {}",
                        ExceptionUtils.getFullExceptionMessage(eof));
            } catch (ConnectException ce) {
                if (!isPing) {
                    if (ce.getMessage().contains("Connection timed out")) {
                        log.error("За указанное время не было получено никаких данных от Сервера.");
                    } else {
                        log.error("Ошибка подключения: {}", ExceptionUtils.getFullExceptionMessage(ce));
                    }
                }
            } catch (SocketException e) {
                // надо бы как-то понять, если это умышленное завершение:
                log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (UnknownHostException e) {
                log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (IOException e) {
                if (!isPing) {
                    log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
                }
            } catch (ClassNotFoundException e) {
                log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (Exception e) {
                log.warn("Not handled exception here (6): {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            killSelf();
            if (!isPing) {
                log.info("Теперь локальное сокетное подключение полностью закрыто.");
            }
        });
        connectionThread.setName("Socket connection thread");
        connectionThread.start();

        if (gameController.isServerIsOpen()) {
            connectionLiveThread = new Thread(() -> {
                log.info("Поток поддержки жизни соединения начал свою работу.");
                while (connectionThread.isAlive() && !Thread.currentThread().isInterrupted()) {
                    long timePass = System.currentTimeMillis() - this.lastDataReceivedTimestamp;
                    if (isOpen() && timePass >= Constants.getMaxConnectionWasteTime()) {
                        log.info("Тишина с Сервера уже {} мс. Допустимо: {}. Пинг для поддержки соединения...",
                                timePass, Constants.getMaxConnectionWasteTime());
                        toServer(ClientDataDTO.builder().type(NetDataType.PING).worldUid(gameController.getCurrentWorldUid()).build());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                log.info("Поток поддержки жизни соединения завершил свою работу.");
            });
            connectionLiveThread.setName("Connection live thread");
            connectionLiveThread.setDaemon(true);
            connectionLiveThread.start();
        }

        // ждём пока сокет откроется и будет готов к работе:
        if (!isOpen()) {
            long was = System.currentTimeMillis();
            while (!isOpen() && System.currentTimeMillis() - was < 9_000) {
                Thread.yield();
            }
            log.info("Сокет успешно открыт и готов к работе: {}", isOpen());
        }
    }

    private void parseNextData(ClientDataDTO readed) {
        log.debug("Приняты данные от Сервера: {}", readed);
        this.lastDataReceivedTimestamp = System.currentTimeMillis();

        switch (readed.type()) {
            case AUTH_DENIED -> {
                this.isAuthorized.set(false);
                this.lastExplanation = readed.explanation();
                log.error("Сервер отказал в авторизации по причине: {}", lastExplanation);
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил запрос на авторизацию: %s"
                        .formatted(readed.explanation()), 15, true);
            }
            case AUTH_SUCCESS -> {
                World serverWorld = readed.world();
                serverWorld.setUid(readed.worldUid());
                serverWorld.setNetworkAddress(host + ":" + port);
                gameController.saveServerWorldAndSetAsCurrent(serverWorld);
                this.isAuthorized.set(true);
                log.info("Сервер принял запрос авторизации, его мир установлен как текущий.");
            }
            case HERO_ACCEPTED -> {
                this.isAccepted.set(true);
                log.info("Сервер принял выбор Героя");
            }
            case HERO_RESTRICTED -> {
                this.isAccepted.set(false);
                this.lastExplanation = readed.explanation();
                log.error("Сервер отказал в выборе Героя по причине: {}", readed.explanation());
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отказал в выборе Героя: %s"
                        .formatted(readed.explanation()), 15, true);
            }
            case SYNC -> {
                log.debug("Приняты данные синхронизации от игрока: {}", readed.playerName());
                gameController.syncServerDataWithCurrentWorld(readed);
            }
            case CHAT -> {
                if (readed.chatMessage() != null) {
                    String message = readed.chatMessage();
                    log.info("Новые сообщения чата: {}", message);
                }
            }
            case DIE -> {
                this.lastExplanation = readed.explanation();
                log.info("Сервер изъявил своё желание покончить с нами. Сворачиваемся...");
                killSelf();
            }
            case PING -> toServer(ClientDataDTO.builder().type(NetDataType.PONG).build());
            case PONG -> this.isPongReceived.set(true);
            case WRONG_WORLD_PING -> {
                log.info("Сервер сообщил о некорректном пинге активного мира: {}", readed.explanation());
                this.lastExplanation = readed.explanation();
                this.isPongReceived.set(false);
                killSelf();
            }
            default -> log.error("От Сервера пришел необработанный тип данных: {}", readed.type());
        }
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public synchronized void toServer(ClientDataDTO dataDTO) {
        // PONG никому не интересен, лишь мешает логу.
        if (!dataDTO.type().equals(NetDataType.PONG)) {
            if (dataDTO.type().equals(NetDataType.PING)) {
                log.info("Пингуем Мир {} Сервера {}:{}...", dataDTO.worldUid(), host, port);
            } else {
                log.info("Шлём на Сервер {}...", dataDTO.type());
            }
        }

        if (this.oos == null || !isOpen()) {
            long was = System.currentTimeMillis();
            while ((this.oos == null || !isOpen()) && System.currentTimeMillis() - was < 6_000) {
                Thread.yield();
            }
        }
        if (this.oos == null || !isOpen()) {
            throw new GlobalServiceException(ErrorMessages.SOCKET_CLOSED);
        }

        try {
            this.oos.writeObject(dataDTO);
            this.oos.flush();
        } catch (SocketException se) {
            log.error("Ошибка сокета. Он точно закрыт? {}: {}", this.socket.isClosed(), ExceptionUtils.getFullExceptionMessage(se));
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
            killSelf();
        } catch (NullPointerException npe) {
            log.warn("Подключение имеет проблемы и будет закрыто: {}", ExceptionUtils.getFullExceptionMessage(npe));
            killSelf();
        } catch (Exception e) {
            log.warn("Not handled exception here (1): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void killSelf() {
        resetPingPong();
        this.isAuthorized.set(false);
        this.isAccepted.set(false);

        if (this.socket != null && !this.socket.isClosed()) {
            log.warn("Destroy the connection...");
            try {
                toServer(ClientDataDTO.builder().type(NetDataType.DIE).build());
            } catch (Exception e) {
                if (!isPing) {
                    log.error("Провал отправки посмертного предупреждения Серверу: {}", ExceptionUtils.getFullExceptionMessage(e));
                }
            }
            try {
                this.socket.close();
            } catch (Exception e) {
                log.error("Провал гашения локального подключения к Серверу: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        if (connectionThread != null && !connectionThread.isInterrupted()) {
            connectionThread.interrupt();
        }
        if (connectionLiveThread != null && !connectionLiveThread.isInterrupted()) {
            connectionLiveThread.interrupt(); // поднять выше?
        }

        if (!isPing && gameController.isGameActive()) {
            gameController.setGameActive(false);
            log.info("Переводим героя {} ({}) в статус offlile и сохраняем...",
                    gameController.getCurrentHeroName(), gameController.getCurrentHeroUid());
            gameController.setHeroOfflineAndSave(null);
            gameController.loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    public boolean isOpen() {
        return this.socket != null
                && !this.socket.isClosed()
                && !this.socket.isOutputShutdown()
                && !this.socket.isInputShutdown();
    }

    public String getActiveHost() {
        return this.host;
    }

    public boolean isPongReceived() {
        return isPongReceived.get();
    }

    public void resetPingPong() {
        log.debug("Сброс статуса PONG на false по-умолчанию...");
        this.isPongReceived.set(false);
    }

    public boolean isAuthorized() {
        return isAuthorized.get();
    }

    public boolean isAccepted() {
        return isAccepted.get();
    }

    public String getLastExplanation() {
        return this.lastExplanation;
    }
}
