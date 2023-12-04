package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.interfaces.iServer;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Server implements iServer {
    private final Map<InetAddress, ConnectedServerPlayer> clients = new ConcurrentHashMap<>();

    private Thread diedClientsCleaner, serverThread;

    private GameController gameController;

    private ServerSocket serverSocket;

    private String address;

    private boolean isServerCloseAccepted = false;

    @Override
    public void open(GameController gameController) {
        this.gameController = gameController;

        if (isOpen()) {
            log.warn("Server is opened already. And can`t open again now.");
        } else {
            // запуск вспомогательного потока чистки мертвых клиентов:
            startDiedClientsCleaner();

            serverThread = new Thread(() -> {
                log.info("Создание Сервера...");
                try (ServerSocket socket = new ServerSocket(Constants.DEFAULT_SERVER_PORT)) {
                    this.serverSocket = socket;
                    this.serverSocket.setReuseAddress(true);
                    this.serverSocket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);

                    this.address = serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort();
                    log.info("Создан сервер на {} (buffer: {})", address, serverSocket.getReceiveBufferSize());

                    while (!serverThread.isInterrupted() && !this.serverSocket.isClosed()) {
                        log.debug("Awaits a new connections...");
                        acceptNewClient(serverSocket.accept());
                    }
                    log.info("Connection server is shutting down...");
                } catch (Exception e) {
                    if (!isServerCloseAccepted) {
                        handleException(e);
                    }
                } finally {
                    log.warn("Сервер прекратил свою работу.\n");
                }
            });
            serverThread.start();
        }
    }

    @Override
    public boolean isOpen() {
        return serverThread != null && serverThread.isAlive() && serverSocket != null && !serverSocket.isClosed();
    }

    @Override
    public void close() {
        isServerCloseAccepted = true;

        log.info("Остановка вспомогательного потока чистки клиентов...");
        if (diedClientsCleaner != null && diedClientsCleaner.isAlive()) {
            diedClientsCleaner.interrupt();
        }

        log.info("Остановка бродкаст-потока Сервера...");
        if (gameController != null) {
            gameController.stopBroadcast();
        }

        log.info("Остановка основного потока Сервера...");
        if (isOpen()) {
            clients.values().forEach(ConnectedServerPlayer::kill);

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
        gameController.clearConnectedHeroes();
    }

    @Override
    public boolean isClosed() {
        return (serverThread == null || !serverThread.isAlive()) && (serverSocket == null || serverSocket.isClosed());
    }

    @Override
    public ConnectedServerPlayer acceptNewClient(Socket socket) {
        try {
            if (clients.containsKey(socket.getInetAddress()) && !clients.get(socket.getInetAddress()).isClosed()) {
                log.warn("Клиент уже есть в карте клиентов! Возвращаем...");
            } else {
                log.info("Подключился новый клиент: {} ({})", socket.getInetAddress().getHostName(), socket);
                clients.put(socket.getInetAddress(), new ConnectedServerPlayer(this, socket, gameController));
            }

            return clients.get(socket.getInetAddress());
        } catch (IOException e) {
            log.info("Ошибка при принятии нового подключения Клиента: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (Exception e) {
            log.warn("Not handled exception here (3): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
        return null;
    }

    @Override
    public long connected() {
        return clients.values().stream().filter(ConnectedServerPlayer::isAuthorized).count();
    }

    @Override
    public void destroyClient(ConnectedServerPlayer playerToDestroy) {
        clients.entrySet().iterator().forEachRemaining(entry -> {
            if (entry.getValue().getClientUid().equals(playerToDestroy.getClientUid())) {
                log.info("Удаление из карты клиентов Клиента {}...", entry.getValue().getPlayerName());
                entry.getValue().kill();
                clients.remove(entry.getKey());

                log.info("Удаление из карты игровых героев Героя Клиента {}...", entry.getValue().getPlayerName());
                gameController.offlineSaveAndRemoveOtherHeroByPlayerUid(playerToDestroy.getClientUid());
            }
        });
    }

    @Override
    public void clearDiedClients() {
        clients.values().iterator().forEachRemaining(client -> {
            if (client.isClosed() || client.isInterrupted()) {
                log.info("Удаляем игрока {} ({}) из списка подключенных, т.к. его поток прерван.",
                        client.getPlayerName(), client.getPlayerUid());
                destroyClient(client);
            }
        });
    }

    @Override
    public void handleException(Exception e) {
        if (e instanceof BindException) {
            new FOptionPane().buildFOptionPane("Адрес занят", "Перезапустите игру или попробуйте ещё раз");
        } else if (e instanceof IOException io) {
            log.warn("Завершение работы сервера по IOException: {}", ExceptionUtils.getFullExceptionMessage(io));
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, ExceptionUtils.getFullExceptionMessage(io));
        } else if (e instanceof InterruptedException) {
            log.info("Поступило исключение прерывания основного потока Сервера.");
            serverThread.interrupt();
        } else {
            log.warn("Неожиданная ошибка в работе Сервера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Override
    public Set<ConnectedServerPlayer> getPlayers() {
        return clients.values().stream().filter(ConnectedServerPlayer::isAccepted).collect(Collectors.toSet());
    }

    /**
     * Выступая в роли Сервера, шлём свои данные клиентам через этот метод.
     *
     * @param dataDto данные об изменениях серверной версии мира.
     */
    @Override
    public void broadcast(ClientDataDTO dataDto, ConnectedServerPlayer excludedPlayer) {
        log.debug("Бродкастим инфо всем клиентам...");
        for (ConnectedServerPlayer connectedServerPlayer : getPlayers()) {
            if (connectedServerPlayer.getClientUid().equals(excludedPlayer.getClientUid())) {
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

    public void untilOpen(int waitTime) {
        if (serverThread != null) {
            try {
                final long was = System.currentTimeMillis();
                while (!isOpen() && System.currentTimeMillis() - was < waitTime) {
                    serverThread.join(250);
                }
            } catch (InterruptedException e) {
                serverThread.interrupt();
            }
        }
    }

    public void untilClose(int waitTime) {
        if (serverThread != null) {
            try {
                final long was = System.currentTimeMillis();
                while (!isClosed() && System.currentTimeMillis() - was < waitTime) {
                    serverThread.join(250);
                }
            } catch (InterruptedException e) {
                serverThread.interrupt();
            }
        }
    }

    public String getAddress() {
        return this.address;
    }
}
