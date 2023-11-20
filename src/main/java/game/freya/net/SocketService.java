package game.freya.net;

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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SocketService {
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>(3);
    private Thread serverThread;

    public boolean openServer() {
        if (serverThread == null) {

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
                    serverSocket.setSoTimeout(60_000);
                    log.info("Создан сервер на порту {} (buffer: {})", serverSocket.getLocalPort(), serverSocket.getReceiveBufferSize());

                    while (!serverThread.isInterrupted()) {
                        log.info("Ready to accept new connections...");
                        Socket client = serverSocket.accept();
                        log.info("Подключился новый клиент: {} ({})", client.getInetAddress().getHostName(), client);
                        String cliHostName = client.getInetAddress().getHostName();
                        clients.putIfAbsent(cliHostName, new ClientHandler(cliHostName, client));
                    }
                    log.info("Connection server is shutting down...");
                } catch (SocketTimeoutException ste) {
                    log.error("Это все-таки случилось. Из-за SoTimeout?");
                    throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, ExceptionUtils.getFullExceptionMessage(ste));
                } catch (IOException e) {
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
        log.info("Закрытие сервиса сетевой игры...");
        for (ClientHandler handler : clients.values()) {
            handler.kill();
        }
        if (serverThread != null) {
            try {
                serverThread.interrupt();
                serverThread.join(5_000);
            } catch (InterruptedException e) {
                log.error("Ошибка при прерывании потока сервера: {}", e.getMessage());
                serverThread.interrupt();
            }

            return serverThread.isInterrupted() && !serverThread.isAlive();
        } else {
            return true;
        }
    }

    public void openSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port) {
            {
                setReuseAddress(true);
                setKeepAlive(true);
                setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
            }
        };

        String cliHostName = socket.getLocalAddress().getHostName();
        log.info("Socket connection to '{}:{}' is prepared. Connect...", host, port);
        clients.putIfAbsent(cliHostName, new ClientHandler(cliHostName, socket));
    }

    public boolean isOpen() {
        return serverThread != null && !serverThread.isInterrupted() && serverThread.isAlive();
    }
}
