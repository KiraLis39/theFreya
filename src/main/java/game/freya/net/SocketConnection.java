package game.freya.net;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.WorldDto;
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

@Slf4j
@Getter
@RequiredArgsConstructor
public final class SocketConnection extends ConnectionHandler implements Runnable, AutoCloseable {
    private GameControllerService gameControllerService;
    private String host;
    private int port;
    private volatile String lastExplanation;
    private volatile long lastDataReceivedTimestamp;
    @Setter
    private volatile boolean isHandledExit;

    public void openSocket(String host, Integer port, GameControllerService gameControllerService, boolean isPing) {
        this.gameControllerService = gameControllerService;
        setPingReceived(isPing);
        this.isHandledExit = false;

        this.host = host;
        this.port = port != null ? port : Constants.getGameConfig().getDefaultServerPort();
        setConnectionThread(new Thread(SocketConnection.this) {{
            setName("Socket connection thread");
            setDaemon(true);
            setUncaughtExceptionHandler((_, e) -> {
                lastExplanation = ExceptionUtils.getFullExceptionMessage(e);
                log.error("Ошибка потока {}: {}", getConnectionThread().getName(), lastExplanation);
            });
            start();
        }});

        setLiveThread(Thread.startVirtualThread(() -> {
            log.info("Поток поддержки жизни соединения начал свою работу.");
            while (getConnectionThread().isAlive() && !Thread.currentThread().isInterrupted()) {
                long timePass = System.currentTimeMillis() - this.lastDataReceivedTimestamp;
                if (isOpen() && timePass >= Constants.getGameConfig().getMaxConnectionWasteTime()) {
                    log.info("Тишина с Сервера уже {} мс. Допустимо: {}. Пинг для поддержки соединения...",
                            timePass, Constants.getGameConfig().getMaxConnectionWasteTime());
                    toServer(ClientDataDto.builder()
                            .dataType(NetDataType.EVENT)
                            .dataEvent(NetDataEvent.PING)
                            .content(EventPingPong.builder()
                                    .worldUid(gameControllerService.getWorldService().getCurrentWorld().getUid())
                                    .build())
                            .build());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Поток поддержки жизни соединения завершил свою работу.");
        }));
        getLiveThread().setName("Connection live thread");

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
                setAuthorized(false);
                this.lastExplanation = deny.explanation();
                log.error("Сервер отказал в авторизации по причине: {}", lastExplanation);
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил запрос на авторизацию: %s"
                        .formatted(deny.explanation()), 15, true);
            }
            case AUTH_SUCCESS -> {
                EventWorldData auth = (EventWorldData) readed.content();
                WorldDto serverWorld = auth.world();
                serverWorld.setUid(auth.worldUid());
                serverWorld.setAddress(host + ":" + port);
                gameControllerService.saveServerWorldAndSetAsCurrent(serverWorld); // todo: check createdBy not null
                setAuthorized(true);
                log.info("Сервер принял запрос авторизации, его мир установлен как текущий.");
            }
            case HERO_ACCEPTED -> {
                setAccepted(true);
                log.info("Сервер принял выбор Героя");
                Collection<PlayCharacterDto> otherHeroes = readed.heroes();
                log.info("На Сервере уже есть героев: {}", otherHeroes.size());
                for (PlayCharacterDto otherHero : otherHeroes) {
                    CharacterDto saved = gameControllerService.getCharacterService().justSaveAnyHero(otherHero);
//                    gameControllerService.getPlayedHeroes().addHero(saved);
                }
            }
            case HERO_RESTRICTED -> {
                EventDenied deny = (EventDenied) readed.content();
                setAccepted(false);
                this.lastExplanation = deny.explanation();
                log.error("Сервер отказал в выборе Героя по причине: {}", deny.explanation());
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отказал в выборе Героя: %s"
                        .formatted(deny.explanation()), 15, true);
            }
            case HERO_REQUEST -> gameControllerService.getCharacterService()
                    .justSaveAnyHero(gameControllerService.getEventService().cliToHero(readed));
            case CHAT -> {
                TypeChat chat = (TypeChat) readed.content();
                if (chat.chatMessage() != null) {
                    String message = chat.chatMessage();
                    log.info("Новые сообщения чата: {}", message);
                }
            }
            case EVENT -> readEvent(readed);
            default -> log.error("От Сервера пришел необработанный тип данных: {}", readed.dataType());
        }
    }

    private void readEvent(ClientDataDto clientDataDto) {
        switch (clientDataDto.dataEvent()) {
            case CLIENT_DIE -> {
                EventClientDied died = (EventClientDied) clientDataDto.content();
                if (died != null) {
                    this.lastExplanation = died.explanation();
                }
                log.info("Сервер изъявил своё желание покончить с нами по причине: {}. Сворачиваемся...", this.lastExplanation);
                close();
            }
            case PING -> toServer(ClientDataDto.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.PONG)
                    .build());
            case PONG -> {
                EventPingPong pong = (EventPingPong) clientDataDto.content();
                this.lastExplanation = pong.worldUid().toString();
                setPongReceived(true);
            }
            case WRONG_WORLD_PING -> {
                EventDenied deny = (EventDenied) clientDataDto.content();
                log.info("Сервер сообщил о некорректном пинге активного мира: {}", deny.explanation());
                this.lastExplanation = deny.explanation();
                setPongReceived(false);
                close();
            }
            case HERO_OFFLINE, HERO_MOVING -> {
                log.debug("Приняты данные синхронизации {} от игрока: {} (герой: {})",
                        clientDataDto.dataEvent(), clientDataDto.playerName(), clientDataDto.heroName());
                gameControllerService.syncServerDataWithCurrentWorld(clientDataDto);
            }
            default -> log.error(Constants.getNotRealizedString());
        }
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public void toServer(ClientDataDto dataDTO) {
        // PONG никому не интересен, лишь мешает логу. EVENT тоже.
        if (!dataDTO.dataEvent().equals(NetDataEvent.PONG)) {
            if (dataDTO.dataEvent().equals(NetDataEvent.PING)) {
                log.info("Пингуем Мир {} Сервера {}:{}...", ((EventPingPong) dataDTO.content()).worldUid(), host, port);
            } else {
                log.info("Шлём на Сервер {} {}...", dataDTO.dataType(), dataDTO.dataEvent());
            }
        }

        if (getOutputStream() == null || !isOpen()) {
            long was = System.currentTimeMillis();
            while ((getOutputStream() == null || !isOpen()) && System.currentTimeMillis() - was < 6_000) {
                Thread.yield();
            }
        }
        if (getOutputStream() == null || !isOpen()) {
            throw new GlobalServiceException(ErrorMessages.SOCKET_CLOSED);
        }

        try {
            getOutputStream().writeObject(dataDTO);
            getOutputStream().flush();
        } catch (SocketException se) {
            log.error("Ошибка сокета. Он вообще открыт? ({}): {}", isOpen(), ExceptionUtils.getFullExceptionMessage(se));
            this.lastExplanation = se.getMessage();
            // killSelf();
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
            close();
        } catch (NullPointerException npe) {
            log.warn("Подключение имеет проблемы и будет закрыто: {}", ExceptionUtils.getFullExceptionMessage(npe));
            close();
        } catch (Exception e) {
            log.warn("Not handled exception here (1): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Override
    public void run() {
        try (Socket client = new Socket()) {
            setServerSocket(client);
            getServerSocket().setSendBufferSize(Constants.getGameConfig().getSocketBufferSize());
            getServerSocket().setReceiveBufferSize(Constants.getGameConfig().getSocketBufferSize());
            getServerSocket().setReuseAddress(true);
            // getServerSocket().setKeepAlive(true);
            getServerSocket().setTcpNoDelay(true);

            getServerSocket().setSoTimeout(Constants.getGameConfig().getConnectionNoDataTimeout());
            getServerSocket().connect(new InetSocketAddress(this.host, this.port), Constants.getGameConfig().getSocketPingAwaitTimeout());

            try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()));
                 ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))
            ) {
                setOutputStream(outs);
                log.info("Socket connection to '{}:{}' is ready!", host, port);

                ClientDataDto readed;
                while ((readed = (ClientDataDto) inps.readObject()) != null && !getConnectionThread().isInterrupted()) {
                    parseNextData(readed);
                }

                log.info("Завершена работа inputs-outputs клиентского соединения с Сервером.");
                close();
            } catch (EOFException eof) {
                log.error("При работе InputStream сокетного соединения с Сервером произошла ошибка EOF: {}",
                        ExceptionUtils.getFullExceptionMessage(eof));
            }
            log.warn("Завершена работа всех соединений текущего подключения с Сервером.");
        } catch (ConnectException ce) {
            if (!isPingRecieved()) {
                if (ce.getMessage().contains("Connection timed out")) {
                    log.error("За указанное время не было получено никаких данных от Сервера.");
                } else {
                    log.error("Ошибка подключения: {}", ExceptionUtils.getFullExceptionMessage(ce));
                }
            }
        } catch (SocketException e) {
            if (!isPingRecieved() && !isHandledExit) {
                log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } catch (UnknownHostException e) {
            log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (SocketTimeoutException ste) {
            if (!isPingRecieved()) {
                log.error("Вылет по тайм-ауту: {}", ExceptionUtils.getFullExceptionMessage(ste));
                close();
            }
        } catch (IOException e) {
            if (!isPingRecieved()) {
                log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } catch (ClassNotFoundException e) {
            log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (Exception e) {
            log.warn("Not handled exception here (6): {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        close();
        if (!isPingRecieved()) {
            log.info("Теперь локальное сокетное подключение полностью закрыто.");
        }
    }

    @Override
    public void close() {
        resetPingPong();
        setAuthorized(false);
        setAccepted(false);

        if (getLiveThread() != null && !getLiveThread().isInterrupted()) {
            getLiveThread().interrupt();
        }

        if (getServerSocket() != null && !getServerSocket().isClosed()) {
            log.warn("Destroy the connection...");
            if (!isPingRecieved() && isOpen()) {
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
                getServerSocket().close();
            } catch (Exception e) {
                log.error("Провал гашения локального подключения к Серверу: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        if (getConnectionThread() != null && !getConnectionThread().isInterrupted()) {
            getConnectionThread().interrupt();
        }

        if (!isPingRecieved() && gameControllerService.isGameActive()) {
            // останавливаем игру:
            gameControllerService.setGameActive(false);
            log.info("Переводим героя {} ({}) в статус offline и сохраняем...",
                    gameControllerService.getCharacterService().getCurrentHero().getName(),
                    gameControllerService.getCharacterService().getCurrentHero().getUid());
            gameControllerService.getCharacterService().getCurrentHero().setOnline(false);
            gameControllerService.getCharacterService().saveCurrent();
            // возвращаемся в главное меню:
            gameControllerService.getGameFrameController().loadMenuScreen();
        }
    }

    public void startClientBroadcast() {
        log.info("Начало вещания на Сервер...");
        if (getNetDataTranslator() != null && !getNetDataTranslator().isInterrupted()) {
            getNetDataTranslator().interrupt();
        }

        // создаётся поток текущего состояния на Сервер:
        setNetDataTranslator(new Thread(() -> {
            while (!getNetDataTranslator().isInterrupted() && isOpen() && gameControllerService.isGameActive()) {
                if (!getDeque().isEmpty()) {
                    while (!getDeque().isEmpty()) {
                        toServer(getDeque().pollFirst());
                    }
                }

                try {
                    Thread.sleep(Constants.getGameConfig().getServerBroadcastDelay());
                } catch (InterruptedException e) {
                    log.warn("Прерывание потока отправки данных на сервер!");
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Поток трансляции данных игры на Сервер остановлен.");
        }));
        getNetDataTranslator().setName("Game-to-Server data broadcast thread");
        getNetDataTranslator().setDaemon(true);
        getNetDataTranslator().start();
    }

    public boolean registerOnServer() {
        log.info("Отправка данных текущего героя на Сервер...");
        toServer(gameControllerService.getEventService()
                .heroToCli(gameControllerService.getCharacterService().getCurrentHero(), gameControllerService.getPlayerService().getCurrentPlayer()));

        Thread heroCheckThread = Thread.startVirtualThread(() -> {
            while (!isAccepted() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });

        try {
            heroCheckThread.join(9_000);
            if (heroCheckThread.isAlive()) {
                log.error("Не получили разрешения на Героя от Сервера.");
                heroCheckThread.interrupt();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            heroCheckThread.interrupt();
            return false;
        }
    }
}
