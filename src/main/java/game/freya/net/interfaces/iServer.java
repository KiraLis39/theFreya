package game.freya.net.interfaces;

import game.freya.GameController;
import game.freya.net.ConnectedPlayer;
import game.freya.net.data.ClientDataDTO;

import java.net.Socket;
import java.util.Set;

public interface iServer {
    void open(GameController gameController);

    boolean isOpen();

    void close();

    boolean isClosed();

    ConnectedPlayer acceptNewClient(Socket socket);

    int connected();

    void destroyClient(ConnectedPlayer connectedPlayerToDestroy);

    void clearDiedClients();

    void handleException(Exception e);

    Set<ConnectedPlayer> getPlayers();

    void broadcast(ClientDataDTO dataDto, ConnectedPlayer excludedPlayer);
}
