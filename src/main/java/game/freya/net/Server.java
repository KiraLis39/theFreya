package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.interfaces.iServer;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Server implements iServer {
    private final Map<InetAddress, ConnectedPlayer> clients = HashMap.newHashMap(3);
    private Thread diedClientsCleaner, serverThread;
    private GameController gameController;
    private volatile boolean doStop;
    private String address;

    @PostConstruct
    public void init() {
        // запуск вспомогательного потока чистки мертвых клиентов:
        if (diedClientsCleaner == null) {
            diedClientsCleaner = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        clearDiedClients();
                        Thread.sleep(3_000);
                    } catch (InterruptedException e) {
                        log.error("Прерван поток {}", Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                    }
                }
            });
            diedClientsCleaner.setName("Died clients cleaner thread");
            diedClientsCleaner.setDaemon(true);
            diedClientsCleaner.start();
        }
    }

    @Override
    public void open(GameController gameController) {
        this.gameController = gameController;

        if (serverThread == null) {
            this.doStop = false;
            serverThread = new Thread(() -> {
                log.info("Создание Сервера...");
                try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
                    serverSocket.setReuseAddress(true);
                    serverSocket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                    serverSocket.setSoTimeout(Constants.SERVER_CONNECTION_AWAIT_TIMEOUT);
                    this.address = serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort();
                    log.info("Создан сервер на {} (buffer: {})", address, serverSocket.getReceiveBufferSize());
                    // InetAddress.anyLocalAddress()
                    while (!serverThread.isInterrupted() && !doStop) {
                        log.info("Awaits a new connections...");
                        acceptNewClient(serverSocket.accept());
                    }

                    log.info("Connection server is shutting down...");
                } catch (Exception e) {
                    handleException(e);
                } finally {
                    log.warn("Сервер прекратил свою работу."); // или нет?
                }
            });
            serverThread.start();
        } else if (!serverThread.isAlive()) {
            log.info("Перезапуск прерванного ранее Сервера...");
            serverThread.start();
        } else {
            log.warn("Server is opened already. And can`t open again now.");
        }
    }

    @Override
    public boolean isOpen() {
        return serverThread != null && serverThread.isAlive() && !serverThread.isInterrupted();
    }

    @Override
    public void close() {
        log.info("Остановка вспомогательного потока чистки клиентов...");
        if (diedClientsCleaner != null && diedClientsCleaner.isAlive()) {
            diedClientsCleaner.interrupt();
        }

        log.info("Остановка основного потока Сервера...");
        if (isOpen()) {
            this.doStop = true;
            serverThread.interrupt();
            serverThread = null;
        }

        log.info("Убийство всех клиентов...");
        clients.values().forEach(ConnectedPlayer::kill);
        clients.clear();
    }

    @SuppressWarnings("removal")
    public void hardStop() {
        if (serverThread.isAlive()) {
            serverThread.stop();
        }
    }

    @Override
    public boolean isClosed() {
        return !isOpen();
    }

    @Override
    public ConnectedPlayer acceptNewClient(Socket socket) {
        if (clients.containsKey(socket.getInetAddress())) {
            log.error("В клиентах уже числится Клиент {}", socket.getInetAddress());
            return null;
        }

        String cliHostName = socket.getInetAddress().getHostName();
        log.info("Подключился новый клиент: {} ({})", cliHostName, socket);
        clients.put(socket.getInetAddress(), new ConnectedPlayer(this, socket, gameController));
        return clients.get(socket.getInetAddress());
    }

    @Override
    public int connected() {
        return clients.size();
    }

    @Override
    public Set<ConnectedPlayer> getPlayers() {
        return clients.values().stream().filter(ConnectedPlayer::isAuthorized).collect(Collectors.toSet());
    }

    /**
     * Выступая в роли Сервера, шлём свои данные клиентам через этот метод.
     *
     * @param dataDto данные об изменениях серверной версии мира.
     */
    @Override
    public void broadcast(ClientDataDTO dataDto, ConnectedPlayer excludedPlayer) {
        log.info("Бродкастим инфо всем клиентам...");
        for (ConnectedPlayer connectedPlayer : getPlayers()) {
            if (connectedPlayer.getClientUid().equals(excludedPlayer.getClientUid())) {
                continue; // не слать самому себе.
            }

            connectedPlayer.push(dataDto);
        }
    }

    @Override
    public void destroyClient(ConnectedPlayer playerToDestroy) {
        for (ConnectedPlayer connectedPlayer : clients.values()) {
            if (connectedPlayer.getClientUid().equals(playerToDestroy.getClientUid())) {
                connectedPlayer.kill();
                clients.remove(connectedPlayer.getInetAddress());
                break;
            }
        }
    }

    @Override
    public void clearDiedClients() {
        clients.values().iterator().forEachRemaining(client -> {
            if (client.isInterrupted()) {
                log.info("Удаляем игрока {} ({}) из списка подключенных, т.к. его поток прерван.",
                        client.getPlayerName(), client.getPlayerUid());
                destroyClient(client);
            }
        });
    }

    @Override
    public void handleException(Exception e) {
        if (e instanceof SocketTimeoutException ste) {
            if (!serverThread.isInterrupted()) {
                log.warn("Завершение работы сервера по SoTimeout: {}", ExceptionUtils.getFullExceptionMessage(ste));
                new FOptionPane().buildFOptionPane("Нет подключений:", "Не обнаружено входящих подключений за %s секунд."
                                .formatted(Constants.SERVER_CONNECTION_AWAIT_TIMEOUT), FOptionPane.TYPE.INFO,
                        Constants.getDefaultCursor(), 5, false);
            }
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, ExceptionUtils.getFullExceptionMessage(ste));
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

    public void untilAlive(int waitTime) {
        try {
            serverThread.join(waitTime);
        } catch (InterruptedException e) {
            serverThread.interrupt();
        }
    }

    public Set<HeroDTO> getHeroes() {
        return getPlayers().stream().map(ConnectedPlayer::getHeroDto).collect(Collectors.toSet());
    }

    public String getAddress() {
        return this.address;
    }
}
