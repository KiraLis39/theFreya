package game.freya.net.interfaces;

import game.freya.dto.PlayCharacterDto;
import game.freya.net.data.ClientDataDto;
import game.freya.net.server.ConnectedPlayerThread;

import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

public interface iServer {
    boolean isOpen();

    void close();

    boolean isClosed();

    void acceptNewClient(Socket socket) throws SocketException;

    void destroyClient(ConnectedPlayerThread connectedServerPlayerThread);

    void clearDiedClients();

    void handleException(Exception e);

    Set<PlayCharacterDto> getAcceptedHeroes();

    Set<ConnectedPlayerThread> getAuthorizedPlayers();

    Set<ConnectedPlayerThread> getAcceptedPlayers();

    void broadcast(ClientDataDto dataDto, ConnectedPlayerThread connectedServerPlayerThread);
}
