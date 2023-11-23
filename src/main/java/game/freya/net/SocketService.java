package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SocketService {
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>(3);
    private GameController gameController;
    private Thread serverThread;

    public boolean openServer(GameController gameController) {
        this.gameController = gameController;

        if (serverThread == null || !serverThread.isAlive()) {
            serverThread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
                    /*
                        setReuseAddress: Эта опция сокета сообщает ядру, что даже если этот порт занят (в состоянии TIME_WAIT), все равно
                        продолжайте использовать его повторно.
                        Если он занят, но находится в другом состоянии, вы все равно получите ошибку «Адрес уже используется».
                        Это полезно, если ваш сервер был выключен, а затем сразу же перезапущен, пока на его порту еще активны сокеты.
                     */
                    serverSocket.setReuseAddress(true);
                    serverSocket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                    serverSocket.setSoTimeout(Constants.SERVER_CONNECTION_AWAIT_TIMEOUT);
                    log.info("Создан сервер на порту {} (buffer: {})", serverSocket.getLocalPort(), serverSocket.getReceiveBufferSize());

                    while (!serverThread.isInterrupted()) {
                        // проверка на жизнеспособность игроков:
                        clients.entrySet().iterator().forEachRemaining(client -> {
                            if (client.getValue().isInterrupted()) {
                                log.info("Удаляем игрока {} ({}) из списка подключенных, т.к. его поток прерван.",
                                        client.getKey(), client.getValue().getPlayerName());
                                clients.remove(client.getKey());
                            }
                        });

                        // ожидание нового подключения:
                        log.info("Awaits a new connections...");
                        Socket client = serverSocket.accept();

                        // новый игрок в сети:
                        log.info("Подключился новый клиент: {} ({})", client.getInetAddress().getHostName(), client);
                        String cliHostName = client.getInetAddress().getHostName();
                        clients.putIfAbsent(cliHostName, new ClientHandler(cliHostName, client, gameController));
                    }
                    log.info("Connection server is shutting down...");
                } catch (SocketTimeoutException ste) {
                    log.warn("Завершение работы сервера по SoTimeout: {}", ste.getMessage());
                    new FOptionPane().buildFOptionPane("Нет подключений:",
                            "Не обнаружено входящих подключений за %s секунд.".formatted(Constants.SERVER_CONNECTION_AWAIT_TIMEOUT),
                            FOptionPane.TYPE.INFO, Constants.getDefaultCursor(), 5, false);

                    // прекращаем вещание:
                    gameController.stopBroadcast();
                } catch (IOException e) {
                    // прекращаем вещание:
                    gameController.stopBroadcast();
                    throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, ExceptionUtils.getFullExceptionMessage(e));
                }
            });
            serverThread.start();
            // сервер успешно открыт:
            return true;
        }
        log.warn("Server is opened already. And can`t open again now.");
        return false;
    }

    public boolean close() {
        log.info("Убийство всех клиентов...");
        for (ClientHandler client : clients.values()) {
            client.kill();
        }
        clients.clear();

        if (gameController != null) {
            log.info("Остановка системы оповещений...");
            gameController.stopBroadcast();
        }

        if (serverThread != null) {
            log.info("Закрытие потока сетевого взаимодействия...");
            try {
                serverThread.interrupt();
                serverThread.join(6_000);
            } catch (InterruptedException e) {
                serverThread.interrupt();
            }

            return serverThread.isInterrupted() && !serverThread.isAlive();
        } else {
            return true;
        }
    }

    public ClientHandler openSocket(String host, Integer port, GameController gameController) throws IOException {
        this.gameController = gameController;

        Socket socket = new Socket(host, port != null ? port : Constants.SERVER_PORT) {
            {
                //setReuseAddress(true);
                //setKeepAlive(true);
                setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
            }
        };

        String cliHostName = socket.getLocalAddress().getHostName();
        log.info("Socket connection {} to '{}:{}' is prepared. Connect...", cliHostName, host, port);
        clients.putIfAbsent(cliHostName, new ClientHandler(cliHostName, socket, gameController));

        // возвращаем удачное соединение с сервером для отправки авторизации:
        return clients.get(cliHostName);
    }

    public boolean isServerOpen() {
        return serverThread != null && !serverThread.isInterrupted() && serverThread.isAlive();
    }

    // todo: получать количество игроков нужно всегда от Сервера.
    public long getPlayersCount() {
        if (!isServerOpen() && clients.size() > 0) {
            // если сервер внезапно умер:
            log.error("Сервер внезапно мёртв. Что случилось?..");
            close();
        }
        return clients.values().stream().filter(ClientHandler::isAutorized).count();
    }

    public Set<ClientHandler> getPlayers() {
        return clients.values().stream()
                .filter(c -> c.isAutorized() && !c.getPlayerUid().equals(gameController.getCurrentPlayerUid()))
                .collect(Collectors.toSet());
    }

    public ClientHandler getMe() {
        return clients.values().stream()
                .filter(c -> c.getPlayerUid().equals(gameController.getCurrentPlayerUid()))
                .findFirst().orElseThrow();
    }

    /**
     * Выступая в роли Сервера, шлём свои данные клиентам через этот метод.
     *
     * @param dataDTO данные об изменениях серверной версии мира.
     */
    public void broadcast(ClientDataDTO dataDTO) {
        log.info("Бродкастим инфо всем клиентам...");
        for (ClientHandler client : getPlayers()) { // а разве тут будут клиенты когда я подключаюсь к серверу? А сам сервер?
            try {
                client.push(dataDTO);
            } catch (IOException e) {
                log.error("Ошибка рассылки данных {} клиенту {}", dataDTO, client.getPlayerName());
            }
        }
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public void toServer(ClientDataDTO dataDTO) {
        log.info("Шлём свои данные на Сервер...");
        try {
            getMe().push(dataDTO);
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
        }
    }
}
