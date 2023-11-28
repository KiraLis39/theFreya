package game.freya.net.interfaces;

import game.freya.GameController;
import game.freya.net.ConnectedServerPlayer;
import game.freya.net.data.ClientDataDTO;

import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

public interface iServer {
    void open(GameController gameController);

    boolean isOpen();

    void close();

    boolean isClosed();

    ConnectedServerPlayer acceptNewClient(Socket socket) throws SocketException;

    long connected();

    void destroyClient(ConnectedServerPlayer connectedServerPlayerToDestroy);

    void clearDiedClients();

    void handleException(Exception e);

    Set<ConnectedServerPlayer> getPlayers();

    void broadcast(ClientDataDTO dataDto, ConnectedServerPlayer excludedPlayer);
}
