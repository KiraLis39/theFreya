package game.freya.net;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.World;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventClientDied;
import game.freya.net.data.events.EventDenied;
import game.freya.net.data.events.EventPingPong;
import game.freya.net.data.events.EventWorldData;
import game.freya.net.data.types.TypeChat;
import game.freya.services.GameControllerService;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public final class LocalSocketConnection implements Runnable, AutoCloseable {
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    private final AtomicBoolean isPing = new AtomicBoolean(false);
    @Getter
    private GameControllerService gameControllerService;
    private ObjectOutputStream oos;

    private Thread connectionThread, connectionLiveThread;

    private Socket socket;

    private String host;

    private int port;

    @Getter
    private volatile String lastExplanation;

    private volatile long lastDataReceivedTimestamp;

    @Setter
    private boolean isControlledExit = false;

    public synchronized void openSocket(String host, Integer port, GameControllerService gameControllerService, boolean isPing) {
        this.gameControllerService = gameControllerService;
        this.isPing.set(isPing);
        this.isControlledExit = false;

        this.host = host;
        this.port = port != null ? port : Constants.DEFAULT_SERVER_PORT;
        connectionThread = new Thread(LocalSocketConnection.this) {{
            setName("Socket connection thread");
            setDaemon(true);
            setUncaughtExceptionHandler((_, e) -> {
                lastExplanation = ExceptionUtils.getFullExceptionMessage(e);
                log.error("Ошибка потока {}: {}", connectionThread.getName(), lastExplanation);
            });
            start();
        }};

        connectionLiveThread = Thread.startVirtualThread(() -> {
            log.info("Поток поддержки жизни соединения начал свою работу.");
            while (connectionThread.isAlive() && !Thread.currentThread().isInterrupted()) {
                long timePass = System.currentTimeMillis() - this.lastDataReceivedTimestamp;
                if (isOpen() && timePass >= Constants.getMaxConnectionWasteTime()) {
                    log.info("Тишина с Сервера уже {} мс. Допустимо: {}. Пинг для поддержки соединения...",
                            timePass, Constants.getMaxConnectionWasteTime());
                    toServer(ClientDataDto.builder()
                            .dataType(NetDataType.EVENT)
                            .dataEvent(NetDataEvent.PING)
                            .content(EventPingPong.builder().worldUid(gameControllerService.getCurrentWorldUid()).build())
                            .build());
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

        // ждём пока сокет откроется и будет готов к работе:
        if (!isOpen()) {
            long was = System.currentTimeMillis();
            while (!isOpen() && System.currentTimeMillis() - was < 3_000) {
                Thread.yield();
            }
            log.info("");
            log.info("Сокет успешно открыт и готов к работе: {}", isOpen());
        }
    }

    private void parseNextData(ClientDataDto readed) {
        if (!readed.dataEvent().equals(NetDataEvent.PONG)) {
            log.debug("Приняты данные от Сервера: {} (игрок {})", readed.dataType(), readed.playerName());
        }
        this.lastDataReceivedTimestamp = System.currentTimeMillis();

        switch (readed.dataType()) {
            case AUTH_DENIED -> {
                EventDenied deny = (EventDenied) readed.content();
                this.isAuthorized.set(false);
                this.lastExplanation = deny.explanation();
                log.error("Сервер отказал в авторизации по причине: {}", lastExplanation);
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил запрос на авторизацию: %s"
                        .formatted(deny.explanation()), 15, true);
            }
            case AUTH_SUCCESS -> {
                EventWorldData auth = (EventWorldData) readed.content();
                World serverWorld = auth.world();
                serverWorld.setUid(auth.worldUid());
                serverWorld.setNetworkAddress(host + ":" + port);
                gameControllerService.saveServerWorldAndSetAsCurrent(serverWorld);
                this.isAuthorized.set(true);
                log.info("Сервер принял запрос авторизации, его мир установлен как текущий.");
            }
            case HERO_ACCEPTED -> {
                this.isAccepted.set(true);
                log.info("Сервер принял выбор Героя");
                Collection<CharacterDto> otherHeroes = readed.heroes();
                log.info("На Сервере уже есть героев: {}", otherHeroes.size());
                for (CharacterDto otherHero : otherHeroes) {
                    CharacterDto saved = gameControllerService.justSaveAnyHero(otherHero);
//                    gameControllerService.getPlayedHeroes().addHero(saved);
                }
            }
            case HERO_RESTRICTED -> {
                EventDenied deny = (EventDenied) readed.content();
                this.isAccepted.set(false);
                this.lastExplanation = deny.explanation();
                log.error("Сервер отказал в выборе Героя по причине: {}", deny.explanation());
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отказал в выборе Героя: %s"
                        .formatted(deny.explanation()), 15, true);
            }
            case HERO_REQUEST -> {
//                gameController.saveNewRemoteHero(readed);
//                gameController.setRemoteHeroRequestSent(false);
            }
            case CHAT -> {
                TypeChat chat = (TypeChat) readed.content();
                if (chat.chatMessage() != null) {
                    String message = chat.chatMessage();
                    log.info("Новые сообщения чата: {}", message);
                }
            }
            case EVENT -> {
                switch (readed.dataEvent()) {
                    case CLIENT_DIE -> {
                        EventClientDied died = (EventClientDied) readed.content();
                        if (died != null) {
                            this.lastExplanation = died.explanation();
                        }
                        log.info("Сервер изъявил своё желание покончить с нами"
                                + (this.lastExplanation != null ? ": {}" : "") + ". Сворачиваемся...", this.lastExplanation);
                        killSelf();
                    }
                    case PING -> toServer(ClientDataDto.builder()
                            .dataType(NetDataType.EVENT)
                            .dataEvent(NetDataEvent.PONG)
                            .build());
                    case PONG -> {
                        EventPingPong pong = (EventPingPong) readed.content();
                        this.lastExplanation = pong.worldUid().toString();
                        this.isPongReceived.set(true);
                    }
                    case WRONG_WORLD_PING -> {
                        EventDenied deny = (EventDenied) readed.content();
                        log.info("Сервер сообщил о некорректном пинге активного мира: {}", deny.explanation());
                        this.lastExplanation = deny.explanation();
                        this.isPongReceived.set(false);
                        killSelf();
                    }
                    case HERO_OFFLINE, HERO_MOVING -> {
                        log.debug("Приняты данные синхронизации {} от игрока: {} (герой: {})",
                                readed.dataEvent(), readed.playerName(), readed.heroName());
//                        gameController.syncServerDataWithCurrentWorld(readed);
                    }
                    default -> log.error(Constants.getNotRealizedString());
                }
            }
            default -> log.error("От Сервера пришел необработанный тип данных: {}", readed.dataType());
        }
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public synchronized void toServer(ClientDataDto dataDTO) {
        // PONG никому не интересен, лишь мешает логу. EVENT тоже.
        if (!dataDTO.dataEvent().equals(NetDataEvent.PONG)) {
            if (dataDTO.dataEvent().equals(NetDataEvent.PING)) {
                log.info("Пингуем Мир {} Сервера {}:{}...", ((EventPingPong) dataDTO.content()).worldUid(), host, port);
            } else {
                log.info("Шлём на Сервер {} {}...", dataDTO.dataType(), dataDTO.dataEvent());
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
            log.error("Ошибка сокета. Он вообще открыт? ({}): {}", isOpen(), ExceptionUtils.getFullExceptionMessage(se));
            this.lastExplanation = se.getMessage();
            // killSelf();
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

        if (connectionLiveThread != null && !connectionLiveThread.isInterrupted()) {
            connectionLiveThread.interrupt();
        }

        if (this.socket != null && !this.socket.isClosed()) {
            log.warn("Destroy the connection...");
            if (!isPing.get() && isOpen()) {
                try {
                    toServer(ClientDataDto.builder()
                            .dataType(NetDataType.EVENT)
                            .dataEvent(NetDataEvent.CLIENT_DIE)
                            .build());
                } catch (Exception e) {
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

//        if (!isPing.get() && gameController.isGameActive()) {
//            gameController.setGameActive(false);
//            log.info("Переводим героя {} ({}) в статус offlile и сохраняем...",
//                    gameController.getCurrentHeroName(), gameController.getCurrentHeroUid());
//            gameController.setCurrentHeroOfflineAndSave(null);
//            gameController.loadScreen(ScreenType.MENU_SCREEN);
//        }
    }

    public boolean isOpen() {
        return this.socket != null
                && !this.socket.isClosed()
                && this.socket.isConnected()
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

    @Override
    public void run() {
        try (Socket client = new Socket()) {
            this.socket = client;
            this.socket.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
            this.socket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
            this.socket.setReuseAddress(true);
            // this.socket.setKeepAlive(true);
            this.socket.setTcpNoDelay(true);

            this.socket.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);
            this.socket.connect(new InetSocketAddress(this.host, this.port), Constants.SOCKET_PING_AWAIT_TIMEOUT);

            try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()));
                 ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))
            ) {
                this.oos = outs;
                log.info("Socket connection to '{}:{}' is ready!", host, port);

                ClientDataDto readed;
                while ((readed = (ClientDataDto) inps.readObject()) != null && !connectionThread.isInterrupted()) {
                    parseNextData(readed);
                }

                log.info("Завершена работа inputs-outputs клиентского соединения с Сервером.");
                killSelf();
            } catch (EOFException eof) {
                log.error("При работе InputStream сокетного соединения с Сервером произошла ошибка EOF: {}",
                        ExceptionUtils.getFullExceptionMessage(eof));
            }
            log.warn("Завершена работа всех соединений текущего подключения с Сервером.");
        } catch (ConnectException ce) {
            if (!isPing.get()) {
                if (ce.getMessage().contains("Connection timed out")) {
                    log.error("За указанное время не было получено никаких данных от Сервера.");
                } else {
                    log.error("Ошибка подключения: {}", ExceptionUtils.getFullExceptionMessage(ce));
                }
            }
        } catch (SocketException e) {
            if (!isPing.get() && !isControlledExit) {
                log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } catch (UnknownHostException e) {
            log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (SocketTimeoutException ste) {
            if (!isPing.get()) {
                log.error("Вылет по тайм-ауту: {}", ExceptionUtils.getFullExceptionMessage(ste));
                killSelf();
            }
        } catch (IOException e) {
            if (!isPing.get()) {
                log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } catch (ClassNotFoundException e) {
            log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (Exception e) {
            log.warn("Not handled exception here (6): {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        killSelf();
        if (!isPing.get()) {
            log.info("Теперь локальное сокетное подключение полностью закрыто.");
        }
    }

    public boolean isAlive() {
        return connectionThread != null && connectionThread.isAlive();
    }

    @Override
    public void close() {
        killSelf();
    }
}
