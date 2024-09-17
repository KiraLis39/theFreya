package game.freya.net;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public final class Server extends Thread implements iServer {
    private Map<InetAddress, ConnectedServerPlayerThread> clients;
    private GameControllerService gameControllerService;
    private Thread diedClientsCleaner;
    private ServerSocket serverSocket;

    @Getter
    private String address;

    @Setter
    @Getter
    private int port;
    private Runnable serverRunnable;
    private boolean isServerCloseAccepted;

    public Server(GameControllerService gameControllerService) {
        setName("Freya_Server_Thread");
        log.info("Подготовка Сервера в потоке {}...", Thread.currentThread().getName());

        if (isOpen()) {
            log.warn("Server is opened already. And can`t open again now.");
            return;
        }

        this.gameControllerService = gameControllerService;
        this.clients = new ConcurrentHashMap<>();

        this.isServerCloseAccepted = false;

        setPort(Constants.getGameConfig().getDefaultServerPort());
    }

    @Override
    public void run() {
        serverRunnable.run();
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
                serverSocket.setReceiveBufferSize(Constants.getGameConfig().getSocketBufferSize());

                address = serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort();
                log.info("Создан сервер на {} (buffer: {})", address, serverSocket.getReceiveBufferSize());

                while (!isInterrupted() && !serverSocket.isClosed()) {
                    log.debug("Awaits a new connections...");
                    acceptNewClient(serverSocket.accept());
                }
                log.info("Connection server is shutting down...");
            } catch (Exception e) {
                if (!isServerCloseAccepted) {
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
    public boolean isOpen() {
        return !isClosed();
    }

    @Override
    public boolean isClosed() {
        // сервер мёртв или прерван, а так же сокет не существует либо уже закрыт:
        return (!isAlive() || isInterrupted()) && (serverSocket == null || serverSocket.isClosed());
    }

    @Override
    public void close() {
        isServerCloseAccepted = true;

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
            clients.values().forEach(ConnectedServerPlayerThread::kill);

            try {
                this.serverSocket.close();
            } catch (IOException e) {
                log.info("Ошибка остановки Сервера.");
            } catch (Exception e) {
                log.warn("Not handled exception here (2): {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        log.info("Финальная чистка героев и игроков...");
        clients.clear();
    }

    @Override
    public void acceptNewClient(Socket socket) {
        try {
            log.info("Подключился новый клиент: {} ({})", socket.getInetAddress().getHostName(), socket);
            clients.put(socket.getInetAddress(), new ConnectedServerPlayerThread(socket, gameControllerService));
        } catch (IOException e) {
            log.info("Ошибка при принятии нового подключения Клиента: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (Exception e) {
            log.warn("Not handled exception here (3): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Override
    public int connectedClients() {
        return (int) clients.values().stream().filter(ConnectedServerPlayerThread::isAuthorized).count();
    }

    @Override
    public void destroyClient(ConnectedServerPlayerThread playerToDestroy) {
        clients.entrySet().iterator().forEachRemaining(entry -> {
            if (entry.getValue().getClientUid().equals(playerToDestroy.getClientUid())) {
                log.info("Удаление из карты клиентов Клиента {}...", entry.getValue().getPlayerName());
                entry.getValue().kill();
                clients.remove(entry.getKey());

                log.info("Удаление из карты игровых героев Героя Клиента {}...", entry.getValue().getPlayerName());
                gameControllerService.offlineSaveAndRemoveOtherHeroByPlayerUid(playerToDestroy.getClientUid());
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

    @Override
    public Set<ConnectedServerPlayerThread> getPlayers() {
        return clients.values().stream().filter(ConnectedServerPlayerThread::isAccepted).collect(Collectors.toSet());
    }

    /**
     * Выступая в роли Сервера, шлём свои данные клиентам через этот метод.
     *
     * @param dataDto данные об изменениях серверной версии мира.
     */
    @Override
    public void broadcast(ClientDataDto dataDto, ConnectedServerPlayerThread excludedPlayer) {
        log.debug("Бродкастим инфо всем клиентам...");
        for (ConnectedServerPlayerThread connectedServerPlayer : getPlayers()) {
            if (excludedPlayer == null || connectedServerPlayer.getClientUid().equals(excludedPlayer.getClientUid())) {
                // connectedPlayer.push(ClientDataDTO.builder().type(NetDataType.PING).build());
                continue; // не слать себе свои же данные. Только пинг, если нужно.
            }
            if (!connectedServerPlayer.isAccepted()) {
                continue; // не слать синхро тем, кто еще не загрузил своего Героя - будет исключение!
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
            while (!Thread.currentThread().isInterrupted()) {
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
        if (clients == null) {
            return;
        }

        clients.values().iterator().forEachRemaining(client -> {
            if (client.isClosed() || client.isInterrupted()) {
                log.info("Удаляем игрока {} ({}) из списка подключенных, т.к. его поток прерван.",
                        client.getPlayerName(), client.getPlayerUid());
                destroyClient(client);
                broadcast(ClientDataDto.builder()
                        .dataType(NetDataType.EVENT)
                        .dataEvent(NetDataEvent.HERO_OFFLINE)
                        .content(EventHeroOffline.builder().ownerUid(client.getPlayerUid()).build())
                        .build(), null);
            }
        });
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

    public Collection<PlayCharacterDto> getConnectedHeroes() {
        return Collections.emptyList(); // todo
    }
}
