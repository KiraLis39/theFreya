package game.freya.net.interfaces;

import game.freya.net.ConnectedServerPlayer;
import game.freya.net.data.ClientDataDTO;
import game.freya.services.GameControllerService;

import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

public interface iServer {
    void open(GameControllerService gameController);

    boolean isOpen();

    void close();

    boolean isClosed();

    void acceptNewClient(Socket socket) throws SocketException;

    long connected();

    void destroyClient(ConnectedServerPlayer connectedServerPlayerToDestroy);

    void clearDiedClients();

    void handleException(Exception e);

    Set<ConnectedServerPlayer> getPlayers();

    void broadcast(ClientDataDTO dataDto, ConnectedServerPlayer excludedPlayer);
}
