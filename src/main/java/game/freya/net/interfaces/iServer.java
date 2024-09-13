package game.freya.net.interfaces;

import game.freya.net.ConnectedServerPlayerThread;
import game.freya.net.data.ClientDataDto;

import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

public interface iServer {
//    void open(int port);

    boolean isOpen();

    void close();

    boolean isClosed();

    void acceptNewClient(Socket socket) throws SocketException;

    int connectedClients();

    void destroyClient(ConnectedServerPlayerThread connectedServerPlayerThread);

    void clearDiedClients();

    void handleException(Exception e);

    Set<ConnectedServerPlayerThread> getPlayers();

    void broadcast(ClientDataDto dataDto, ConnectedServerPlayerThread connectedServerPlayerThread);
}
