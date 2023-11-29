package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.NetDataType;
import game.freya.enums.ScreenType;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
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
    private volatile ObjectOutputStream oos;
    private Thread connectionThread;
    private volatile Socket socket;
    private String host;
    private GameController gameController;
    @Setter
    private volatile boolean isPing;

    public synchronized void openSocket(String host, Integer port, GameController gameController) {
        this.gameController = gameController;
        this.host = host;

        // убиваемся, если кто-то запустил нас ранее и не закрыл:
        killSelf();

        this.isPongReceived.set(false);
        this.isAuthorized.set(false);
        this.isAccepted.set(false);

        this.isPing = false;

        connectionThread = new Thread(() -> {
            try (Socket client = new Socket(host, port != null ? port : Constants.DEFAULT_SERVER_PORT)) {
                this.socket = client;
                this.socket.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
                this.socket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
//                this.socket.setReuseAddress(true);
//                this.socket.setKeepAlive(true);
                this.socket.setTcpNoDelay(true);

                if (!gameController.isServerIsOpen()) {
                    // если сервер локальный - нет смысла ставить себе таймаут, т.к. broadcast Сервера всё равно не возвращается обратно:
                    this.socket.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);
                }

                try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
                    this.oos = outs;
                    log.info("Socket connection to '{}:{}' is ready! Send pong message...", host, port);
                    // сразу шлём Серверу сигнал, для "прокачки" соединения:
                    toServer(ClientDataDTO.builder().type(NetDataType.PONG).build());

                    try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                        ClientDataDTO readed;
                        while ((readed = (ClientDataDTO) inps.readObject()) != null && !connectionThread.isInterrupted()) {
                            log.info("Приняты данные от Сервера: {}", readed);
                            switch (readed.type()) {
                                case AUTH_DENIED -> {
                                    this.isAuthorized.set(false);
                                    log.error("Сервер отказал в авторизации по причине: {}", readed.explanation());
                                    new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил запрос на авторизацию: %s"
                                            .formatted(readed.explanation()), 15, true);
                                }
                                case AUTH_SUCCESS -> {
                                    World serverWorld = readed.world();
                                    serverWorld.setUid(readed.worldUid());
                                    serverWorld.setNetworkAddress(host + ":" + (port == null ? Constants.DEFAULT_SERVER_PORT : port));
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
                                    log.error("Сервер отказал в выборе Героя по причине: {}", readed.explanation());
                                    new FOptionPane().buildFOptionPane("Отказ:", "Сервер отказал в выборе Героя: %s"
                                            .formatted(readed.explanation()), 15, true);
                                }
                                case SYNC -> {
                                    log.info("Приняты данные синхронизации от игрока: {}", readed.playerName());
                                    gameController.syncServerDataWithCurrentWorld(readed);
                                }
                                case CHAT -> {
                                    if (readed.chatMessage() != null) {
                                        String message = readed.chatMessage();
                                        log.info("Новые сообщения чата: {}", message);
                                    }
                                }
                                case DIE -> {
                                    log.info("Сервер изъявил своё желание покончить с нами. Сворачиваемся...");
                                    toServer(ClientDataDTO.builder().type(NetDataType.DIE).build());
                                    killSelf();
                                }
                                case PING ->
                                        toServer(ClientDataDTO.builder().type(NetDataType.PONG).build()); // поддержка канала связи.
                                case PONG -> this.isPongReceived.set(true);
                                default -> log.error("От Сервера пришел необработанный тип данных: {}", readed.type());
                            }
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
                log.error("Ошибка подключения: {}", ExceptionUtils.getFullExceptionMessage(ce));
            } catch (SocketException e) {
                if (notAcceptedStop()) {
                    // надо бы как-то понять, если это умышленное завершение:
                    log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
                }
            } catch (UnknownHostException e) {
                log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (IOException e) {
                log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (ClassNotFoundException e) {
                log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (Exception e) {
                log.warn("Not handled exception here (6): {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            killSelf();
            gameController.getLocalSocketConnection().setPing(false);
        });
        connectionThread.setName("Socket connection thread");
        connectionThread.start();
    }

    private boolean notAcceptedStop() {
        return !isPing;
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public synchronized void toServer(ClientDataDTO dataDTO) {
        log.info("Шлём свои данные на Сервер...");

        if (this.oos == null) {
            long was = System.currentTimeMillis();
            while (this.oos == null && System.currentTimeMillis() - was < 3_000) {
                Thread.yield();
            }
        }

        try {
            this.oos.writeObject(dataDTO);
            this.oos.flush();
        } catch (SocketException se) {
            log.error("Ошибка сокета. Он точно закрыт? {}", this.socket.isClosed());
            if (!this.socket.isClosed() && this.oos != null) {
                try {
                    this.oos.writeObject(dataDTO);
                    this.oos.flush();
                } catch (Exception e) {
                    log.warn("Какого хера?! {}", ExceptionUtils.getFullExceptionMessage(e));
                }
            }
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
            killSelf();
        } catch (Exception e) {
            log.warn("Not handled exception here (1): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void killSelf() {
        log.warn("Destroy the connection...");

        if (this.socket != null && !this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (Exception e) {
                log.error("Провал гашения локального подключения к Серверу: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        if (connectionThread != null && !connectionThread.isInterrupted()) {
            connectionThread.interrupt();
        }

        if (gameController.isGameIsActive()) {
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

    public void resetPong() {
        this.isPongReceived.set(false);
    }

    public boolean isAuthorized() {
        return isAuthorized.get();
    }

    public boolean isAccepted() {
        return isAccepted.get();
    }
}
