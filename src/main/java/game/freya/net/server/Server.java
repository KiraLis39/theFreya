package game.freya.net.server;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroOffline;
import game.freya.net.interfaces.iServer;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public final class Server extends Thread implements iServer {
    private static Server server;
    private final Map<InetAddress, ConnectedPlayerThread> clients = new ConcurrentHashMap<>(4);
    private final GameControllerService gameControllerService;
    private Thread diedClientsCleaner;
    private ServerSocket serverSocket;
    @Getter
    private String address;

    @Setter
    @Getter
    private int port = -1;
    private Runnable serverRunnable;

    @Setter
    @Getter
    private volatile boolean isServerHandleClosing = false;

    private Server(GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
        setName("Freya_Server_Thread");

        log.info("Подготовка Сервера '{}' в потоке {}...", getName(), Thread.currentThread().getName());
        if (serverRunnable != null) {
            log.warn("Server is opened already. And can`t open again now.");
            return;
        }

        // устанавливаем порт Сервера из файла конфигурации игры:
        setPort(Constants.getGameConfig().getDefaultServerPort());
    }

    public static Server getInstance(GameControllerService gameControllerService) {
        if (server == null) {
            server = new Server(gameControllerService);
        }
        return server;
    }

    @Override
    public void start() {
        // запуск вспомогательного потока чистки мертвых клиентов:
        startDiedClientsCleaner();

        // запуск серверного потока:
        serverRunnable = () -> {
            log.info("Запуск Сервера в потоке {}...", Thread.currentThread().getName());
            try (ServerSocket socket = new ServerSocket(getPort())) {
                serverSocket = socket;
                serverSocket.setReuseAddress(true);
                serverSocket.setReceiveBufferSize(Constants.getGameConfig().getSocketReceiveBufferSize());

                address = serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort();
                log.info("Создан сервер на {} (buffer: {})", address, serverSocket.getReceiveBufferSize());

                while (!Server.this.isInterrupted() && !serverSocket.isClosed()) {
                    log.debug("Awaits a new connections...");
                    acceptNewClient(serverSocket.accept());
                }
                log.info("Connection server is shutting down...");
            } catch (Exception e) {
                if (!isServerHandleClosing) {
                    handleException(e);
                }
            } finally {
                close();
                log.warn("Поток сервера {} прекратил свою работу.", Thread.currentThread().getName());
            }
        };

        super.start();
    }

    @Override
    public void acceptNewClient(Socket socket) {
        try {
            log.info("Подключился новый клиент: {} ({})", socket.getInetAddress().getHostName(), socket);
            clients.put(socket.getInetAddress(), new ConnectedPlayerThread(socket, gameControllerService));
        } catch (IOException e) {
            log.info("Ошибка при принятии нового подключения Клиента: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (Exception e) {
            log.warn("Not handled exception here (3): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Override
    public void close() {
        log.info("Остановка вспомогательного потока чистки клиентов...");
        if (diedClientsCleaner != null && diedClientsCleaner.isAlive()) {
            diedClientsCleaner.interrupt();
        }

        if (Constants.getLocalSocketConnection() != null) {
            log.info("Остановка бродкаст-потока Сервера...");
            Constants.getLocalSocketConnection().stopBroadcast();
        }

        log.info("Остановка основного потока Сервера...");
        if (isOpen()) {
            try {
                log.info("Финальная чистка подключений...");
                clients.values().forEach(ConnectedPlayerThread::kill);
                clients.clear();

                if (serverSocket != null && !serverSocket.isClosed()) {
                    log.info("Закрытие серверного сокета...");
                    this.serverSocket.close();
                }
            } catch (IOException e) {
                log.info("Ошибка остановки Сервера.");
            } catch (Exception e) {
                log.warn("Not handled exception here (2): {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }
    }

    /**
     * Подключенные игроки (сокетные потоки).
     *
     * @return коллекцию подключенных игроков (потоков сокетных подключений), прошедших предварительную проверку.
     */
    @Override
    public Set<ConnectedPlayerThread> getAuthorizedPlayers() {
        return clients.values().stream().filter(ConnectedPlayerThread::isAuthorized).collect(Collectors.toSet());
    }

    /**
     * Подключенные игроки (сокетные потоки).
     *
     * @return коллекцию подключенных игроков (потоков сокетных подключений), прошедших полную проверку.
     */
    @Override
    public Set<ConnectedPlayerThread> getAcceptedPlayers() {
        return clients.values().stream().filter(ConnectedPlayerThread::isAccepted).collect(Collectors.toSet());
    }

    /**
     * Герои подключенных игроков (клиентов).
     *
     * @return коллекцию героев игроков, прошедших полную проверку.
     */
    @Override
    public Set<PlayCharacterDto> getAcceptedHeroes() {
        return clients.values().stream()
                .filter(ConnectedPlayerThread::isAccepted)
                .map(ConnectedPlayerThread::getHero)
                .collect(Collectors.toSet());
    }

    @Override
    public void destroyClient(ConnectedPlayerThread playerToDestroy) {
        clients.entrySet().iterator().forEachRemaining(entry -> {
            if (entry.getValue().getPlayerUid().equals(playerToDestroy.getPlayerUid())) {
                log.info("Удаление из карты клиентов Клиента {}...", entry.getValue().getPlayerName());
                entry.getValue().kill();
                clients.remove(entry.getKey());

                log.info("Удаление из карты игровых героев Героя Клиента {}...", entry.getValue().getPlayerName());
                Optional<CharacterDto> charDtoOpt = gameControllerService.getCharacterService().getByUid(playerToDestroy.getPlayerUid());
                if (charDtoOpt.isPresent()) {
                    PlayCharacterDto charDto = (PlayCharacterDto) charDtoOpt.get();
                    charDto.setOnline(false);
                    gameControllerService.getCharacterService().justSaveAnyHero(charDto);
                }
            }
        });
    }

    @Override
    public void handleException(Exception e) {
        switch (e) {
            case BindException _ ->
                    new FOptionPane().buildFOptionPane("Адрес занят", "Перезапустите игру или попробуйте ещё раз");
            case IOException io -> {
                log.warn("Завершение работы сервера по IOException: {}", ExceptionUtils.getFullExceptionMessage(io));
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, ExceptionUtils.getFullExceptionMessage(io));
            }
            case InterruptedException _ -> {
                log.info("Поступило исключение прерывания основного потока Сервера.");
                interrupt();
            }
            case null, default ->
                    log.warn("Неожиданная ошибка в работе Сервера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    /**
     * Выступая в роли Сервера, шлём свои данные клиентам через этот метод.
     *
     * @param dataDto данные об изменениях серверной версии мира.
     */
    @Override
    public void broadcast(ClientDataDto dataDto, ConnectedPlayerThread excludedPlayer) {
        log.debug("Бродкастим инфо всем клиентам...");
        for (ConnectedPlayerThread connectedServerPlayer : getAuthorizedPlayers()) {
            if (!connectedServerPlayer.isAccepted() || connectedServerPlayer.equals(excludedPlayer)) {
                // connectedPlayer.push(ClientDataDTO.builder().type(NetDataType.PING).build());
                continue; // не слать себе свои же данные. Только пинг, если нужно.
            }

            connectedServerPlayer.push(dataDto);
        }
    }

    private void startDiedClientsCleaner() {
        if (diedClientsCleaner != null) {
            try {
                diedClientsCleaner.interrupt();
                diedClientsCleaner.join(9_000);
            } catch (InterruptedException e) {
                diedClientsCleaner.interrupt();
            }
        }

        diedClientsCleaner = Thread.startVirtualThread(() -> {
            log.info("Поток чистки Клиентов начал свою работу.");
            while (!Server.this.isInterrupted() && !Thread.currentThread().isInterrupted()) {
                try {
                    clearDiedClients();
                    Thread.sleep(9_000);
                } catch (InterruptedException e) {
                    log.error("Прерван поток {}", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Поток чистки Клиентов завершил свою работу.");
        });
        diedClientsCleaner.setName("Died clients cleaner thread");
    }

    @Override
    public void clearDiedClients() {
        if (clients == null || clients.isEmpty()) {
            return;
        }

        Iterator<ConnectedPlayerThread> cliterator = clients.values().iterator();
        while (cliterator.hasNext()) {
            ConnectedPlayerThread clientThread = cliterator.next();
            if (clientThread.isClosed() || clientThread.isInterrupted()) {
                log.info("Удаляем игрока {} ({}) из списка подключенных, т.к. его поток прерван.",
                        clientThread.getPlayerName(), clientThread.getPlayerUid());
                destroyClient(clientThread);
                cliterator.remove();

                broadcast(ClientDataDto.builder()
                        .dataType(NetDataType.EVENT)
                        .dataEvent(NetDataEvent.HERO_OFFLINE)
                        .content(EventHeroOffline.builder().ownerUid(clientThread.getPlayerUid()).build())
                        .build(), clientThread);
            }
        }
    }

    public void untilOpen(int waitTime) {
        final long was = System.currentTimeMillis();
        try {
            while (!isOpen() && System.currentTimeMillis() - was < waitTime) {
                join(250);
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    public void untilClose(int waitTime) {
        final long was = System.currentTimeMillis();
        try {
            while (!isClosed() && System.currentTimeMillis() - was < waitTime) {
                join(250);
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    @Override
    public void run() {
        serverRunnable.run();
    }

    @Override
    public boolean isOpen() {
        return !isClosed();
    }

    @Override
    public boolean isClosed() {
        // сервер мёртв или прерван, а так же сокет не существует либо уже закрыт:
        return (!isAlive() || isInterrupted()) && (serverSocket == null || serverSocket.isClosed());
    }
}
