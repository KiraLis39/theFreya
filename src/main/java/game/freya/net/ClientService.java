package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ClientService {
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectOutputStream oos;
    private volatile boolean doClose = false;

    private Thread connectionThread;
    private String host;

    public void openSocket(String host, Integer port, GameController gameController) {
        this.mapper.registerModule(new JavaTimeModule());

        if (connectionThread != null && connectionThread.isAlive()) {
            kill();
        }

        this.doClose = false;
        this.host = host;
        connectionThread = new Thread(() -> {
            try (Socket client = new Socket(host, port != null ? port : Constants.SERVER_PORT) {
                {
                    //setKeepAlive(true);
                    //setReuseAddress(true);
                    setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                    setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
                    setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);
                }
            }) {
                try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                    this.oos = new ObjectOutputStream(client.getOutputStream());
                    log.info("Socket connection from {} to '{}:{}' is prepared. Connect...", client.getInetAddress().getHostName(), host, port);

                    ClientDataDTO readed;
                    while ((readed = (ClientDataDTO) ois.readObject()) != null && !doClose) {
                        log.info("Приняты данные от Сервера: {}", readed);
                        switch (readed.type()) {
                            case AUTH_DENIED -> {
                                this.isAuthorized.set(false);
                                log.error("Сервер отказал в авторизации по причине: {}", readed.explanation());
                                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил запрос на авторизацию: %s"
                                        .formatted(readed.explanation()), 15, true);
                            }
                            case AUTH_SUCCESS -> {
                                this.isAuthorized.set(true);
                                gameController.setCurrentWorld(readed.world());
                                log.info("Сервер принял запрос авторизации");
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
                                log.info("Приняты данные синхронизации от Сервера: {}", readed);
                                // ...
                            }
                            case CHAT -> {
                                log.info("Приняты новые сообщения чата: {}", readed.chat());
                                // ...
                            }
                            case DIE -> {
                                log.info("Сервер изъявил своё желание покончить с нами. Сворачиваемся...");
                                kill();
                            }
                            case PONG -> this.isPongReceived.set(true);
                            default -> log.error("От Сервера пришел необработанный тип данных: {}", readed.type());
                        }
                    }

                    // завершение соединения:
                    closeOutputStream();
                    log.info("Завершена работа клиентского соединения с Сервером.");
                }
            } catch (EOFException eof) {
                log.error("При работе потока сокетного соединения с Сервером произошла ошибка: {}",
                        ExceptionUtils.getFullExceptionMessage(eof));
            } catch (ConnectException ce) {
                log.error("Ошибка подключения: {}", ExceptionUtils.getFullExceptionMessage(ce));
            } catch (SocketException e) {
                log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (UnknownHostException e) {
                log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (IOException e) {
                log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (ClassNotFoundException e) {
                log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            // на всякий случай:
            kill();
            if (connectionThread != null) {
                connectionThread.interrupt();
            }
            closeOutputStream();
        });
        connectionThread.setName("Socket connection thread");
        connectionThread.start();
    }

    private void closeOutputStream() {
        try {
            if (this.oos != null) {
                this.oos.flush();
                this.oos.close();
                this.oos = null;
            }
        } catch (IOException e) {
            log.warn("Output stream closing error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public void toServer(ClientDataDTO dataDTO) {
        if (!isOpen()) {
            long was = System.currentTimeMillis();
            while (!isOpen() && System.currentTimeMillis() - was < 15_000) {
                Thread.yield();
            }
        }
        if (!isOpen()) {
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, "no socket connection");
        }

        log.info("Шлём свои данные на Сервер...");
        try {
            this.oos.writeObject(dataDTO);
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
        }
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

    @SuppressWarnings("removal")
    public void kill() {
        log.warn("Destroy the connection...");
        this.doClose = true;
        if (connectionThread == null) {
            return;
        }

        connectionThread.interrupt();

        long was = System.currentTimeMillis();
        Thread killAwait = new Thread(() -> {
            while (System.currentTimeMillis() - was < 3_000) {
                Thread.yield();
            }
            if (connectionThread != null && connectionThread.isAlive()) {
                try {
                    connectionThread.stop();
                } catch (UnsupportedOperationException uoe) {
                    log.warn("Не удаётся остановить поток сокета полностью.");
                }
                connectionThread = null;
            }
        });
        killAwait.start();
        try {
            killAwait.join();
        } catch (InterruptedException e) {
            killAwait.interrupt();
        } finally {
            if (connectionThread != null && (connectionThread.isAlive() || !connectionThread.isInterrupted())) {
                try {
                    connectionThread.stop();
                } catch (UnsupportedOperationException uoe) {
                    log.warn("Не удаётся остановить поток сокета полностью.");
                }
                connectionThread = null;
            }
        }
    }

    public boolean isOpen() {
        return !doClose && this.oos != null;
    }

    public String getActiveHost() {
        return this.host;
    }
}
