package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ClientService {
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    private ObjectMapper mapper;
    private DataOutputStream outputStream;
    private volatile boolean doClose = false;

    public void openSocket(String host, Integer port, GameController gameController) throws IOException {
        try (Socket client = new Socket(host, port != null ? port : Constants.SERVER_PORT) {
            {
                setKeepAlive(true);
                setReuseAddress(true);
                setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
                setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
            }
        }) {
            this.mapper = new ObjectMapper();
            this.mapper.registerModule(new JavaTimeModule());
            this.outputStream = new DataOutputStream(client.getOutputStream());

            log.info("Socket connection from {} to '{}:{}' is prepared. Connect...", client.getInetAddress().getHostName(), host, port);
            try (DataInputStream inputStream = new DataInputStream(client.getInputStream())) {
                ClientDataDTO readed;
                while ((readed = mapper.readValue(inputStream.readUTF(), ClientDataDTO.class)) != null && !doClose) { // .readUTF() // .readObject()
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
                            log.info("Приняты новые сообщения чата", readed);
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
            }

            // завершение соединения:
            closeOutputStream();
            log.info("Завершена работа клиентского соединения с Сервером.");
        }
    }

    private void closeOutputStream() {
        try {
            if (this.outputStream != null) {
                this.outputStream.flush();
                this.outputStream.close();
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
        log.info("Шлём свои данные на Сервер...");
        try {
            outputStream.writeUTF(mapper.writeValueAsString(dataDTO));
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

    public void kill() {
        log.warn("Destroy the connection...");
        this.doClose = true;
    }
}
