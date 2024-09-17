package game.freya.net;

import game.freya.net.data.ClientDataDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
@RequiredArgsConstructor
public class ConnectionHandler {
    private final ArrayDeque<ClientDataDto> deque = new ArrayDeque<>();

    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isPongReceived = new AtomicBoolean(false);
    private final AtomicBoolean isPing = new AtomicBoolean(false);

    @Setter
    private ObjectOutputStream outputStream;

    @Setter
    private Socket serverSocket;

    @Setter
    private Thread connectionThread, liveThread, netDataTranslator;

    public void stopBroadcast() {
        if (netDataTranslator != null && netDataTranslator.isAlive()) {
            netDataTranslator.interrupt();
        }
    }

    public void sendPacket(ClientDataDto data) {
        this.deque.offer(data);
    }

    protected boolean hasNewMessages() {
        return !this.deque.isEmpty();
    }

    protected void setPingReceived(boolean isPing) {
        this.isPing.set(isPing);
    }

    protected boolean isPingRecieved() {
        return this.isPing.get();
    }

    public boolean isAuthorized() {
        return this.isAuthorized.get();
    }

    protected void setAuthorized(boolean isAuthorized) {
        this.isAuthorized.set(isAuthorized);
    }

    protected boolean isPongReceived() {
        return this.isPongReceived.get();
    }

    protected void setPongReceived(boolean isPongReceived) {
        this.isPongReceived.set(isPongReceived);
    }

    protected void resetPingPong() {
        log.debug("Сброс статуса PONG на false по-умолчанию...");
        setPongReceived(false);
    }

    protected boolean isAccepted() {
        return this.isAccepted.get();
    }

    protected void setAccepted(boolean isAccepted) {
        this.isAccepted.set(isAccepted);
    }

    public boolean isOpen() {
        return this.serverSocket != null && !this.serverSocket.isClosed();
        // && this.serverSocket.isConnected()
        // && !this.serverSocket.isOutputShutdown()
        // && !this.serverSocket.isInputShutdown();
    }

    protected boolean isAlive() {
        return getConnectionThread() != null && getConnectionThread().isAlive() && (getLiveThread() == null || getLiveThread().isAlive());
    }

    public void join(int millis) throws InterruptedException {
        if (connectionThread != null && connectionThread.isAlive()) {
            connectionThread.join(millis);
        }
    }
}
