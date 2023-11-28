package game.freya.net;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.World;
import game.freya.enums.NetDataType;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class LocalSocketConnection {
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    private ObjectOutputStream oos;
    private Thread connectionThread;
    private Socket socket;
    private String host;

    public synchronized void openSocket(String host, Integer port, GameController gameController) {
        // убиваемся, если кто-то запустил нас ранее и не закрыл:
        killSelf();

        this.host = host;

        this.isPongReceived.set(false);
        this.isAuthorized.set(false);
        this.isAccepted.set(false);

        connectionThread = new Thread(() -> {
            try (Socket client = new Socket(host, port != null ? port : Constants.DEFAULT_SERVER_PORT)) {
                this.socket = client;
                this.socket.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
                this.socket.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                this.socket.setReuseAddress(true);
                this.socket.setKeepAlive(true);
                this.socket.setTcpNoDelay(true);

                if (!gameController.isServerIsOpen()) {
                    // если сервер локальный - нет смысла ставить себе таймаут, т.к. broadcast Сервера всё равно не возвращается обратно:
                    this.socket.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);
                }

                try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
                    this.oos = outs;
                    log.info("Socket connection from {} to '{}:{}' is prepared. Connect...", client.getInetAddress().getHostName(), host, port);
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
                                    this.isAuthorized.set(true);
                                    World serverWorld = readed.world();
                                    serverWorld.setUid(readed.worldUid());
                                    serverWorld.setNetworkAddress(host + ":" + (port == null ? Constants.DEFAULT_SERVER_PORT : port));
                                    gameController.setCurrentWorld(serverWorld);
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
                                    return;
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
                // надо бы как-то понять, если это умышленное завершение:
                log.error("Ошибка сокета: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (UnknownHostException e) {
                log.error("Ошибка хоста: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (IOException e) {
                log.error("Ошибка ввода-вывода: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (ClassNotFoundException e) {
                log.error("Ошибка чтения в класс: {}", ExceptionUtils.getFullExceptionMessage(e));
            } catch (Exception e) {
                log.warn("Not handled exception here: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            killSelf();
        });
        connectionThread.setName("Socket connection thread");
        connectionThread.start();
    }

    /**
     * Выступая в роли клиента, шлём свои данные на Сервер через этот метод.
     *
     * @param dataDTO данные об изменениях локальной версии мира.
     */
    public synchronized void toServer(ClientDataDTO dataDTO) {
        log.info("Шлём свои данные на Сервер...");
        try {
            this.oos.writeObject(dataDTO);
            this.oos.flush();
        } catch (IOException e) {
            log.error("Ошибка отправки данных {} на Сервер", dataDTO);
        } catch (Exception e) {
            log.warn("Not handled exception here: {}", ExceptionUtils.getFullExceptionMessage(e));
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
    }

    public boolean isOpen() {
        return this.socket != null
                && !this.socket.isClosed()
                && !this.socket.isConnected()
                && this.oos != null
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
