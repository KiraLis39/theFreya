package game.freya.net;

import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
@Getter
@RequiredArgsConstructor
public class ClientHandler extends Thread implements Runnable {
    private final String clientId;
    private final Socket client;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    @Setter
    private long dynamicReadStreamDelay; // оно повышать-понижать при нагрузке или т.т.

    public ClientHandler(String clientId, Socket client) throws IOException {
        this.clientId = clientId;
        this.client = client;
        this.outputStream = new ObjectOutputStream(client.getOutputStream()); // DataOutputStream
        this.inputStream = new ObjectInputStream(client.getInputStream()); // DataOutputStream

        setDaemon(true);
        setUncaughtExceptionHandler((t, e) -> log.error("Client`s socket thread exception: {}", ExceptionUtils.getFullExceptionMessage(e)));
        start();
    }

    @Override
    public void run() {
        log.info("Run the client`s thread for {}...", clientId);
        try {
            ClientDataDTO readed;
            while ((readed = (ClientDataDTO) inputStream.readObject()) != null) { // .readUTF()
                log.info("Income client`s data here: {}", readed);
                sleep(dynamicReadStreamDelay);
            }
        } catch (IOException e) {
            log.warn("Something wrong with client`s data stream: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (ClassNotFoundException e) {
            log.warn("Unknown data object from Client {}: {}", clientId, ExceptionUtils.getFullExceptionMessage(e));
        } catch (InterruptedException e) {
            log.warn("Client`s input stream thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
            interrupt();
        }
    }

    public void push(ClientDataDTO data) throws IOException {
//        outputStream.writeUTF(new ObjectMapper().write(data));
        this.outputStream.writeObject(data);
        this.outputStream.flush();
    }

    public void kill() {
        log.warn("Destroy the client {} connection...", clientId);
        try {
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException e) {
            log.warn("Output stream closing error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        try {
//        this.inputStream.reset();
//        this.inputStream.readAllBytes();
            this.inputStream.close();
        } catch (IOException e) {
            log.warn("Input stream closing error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        try {
            this.client.close();
        } catch (IOException e) {
            log.error("Не удалось закрыть соединение с {} по причине: {}", clientId, ExceptionUtils.getFullExceptionMessage(e));
        }
    }
}
