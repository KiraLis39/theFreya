package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class ConnectedServerPlayer extends Thread implements Runnable {
    @Getter
    private final UUID clientUid;

    private final ObjectMapper mapper;
    private final GameController gameController;
    private final PlayedHeroesService playedHeroesService;
    private final Socket client;
    private final Server server;
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private ObjectOutputStream oos;

    @Getter
    private UUID playerUid;

    @Getter
    private String playerName;
    private NetDataType lastType;

    public ConnectedServerPlayer(Server server, Socket client, GameController gameController) throws SocketException {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.server = server;
        this.playedHeroesService = gameController.getPlayedHeroesService();
        this.gameController = gameController;
        this.clientUid = UUID.randomUUID();

        this.client = client;
        this.client.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);
        this.client.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
        this.client.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
        this.client.setReuseAddress(true);
//        this.client.setKeepAlive(true);
        this.client.setTcpNoDelay(true);

        //setDaemon(true);
        setUncaughtExceptionHandler((t, e) -> log.error("Client`s socket thread exception: {}", ExceptionUtils.getFullExceptionMessage(e)));
        start();
    }

    @Override
    public void run() {
        log.info("Запущен новый поток-клиент {}...", clientUid);

        try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
            this.oos = outs;

            // сразу шлём подключенному Клиенту сигнал, для "прокачки" соединения:
            push(ClientDataDTO.builder().type(NetDataType.PONG).build());

            try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                ClientDataDTO readed;
                while ((readed = (ClientDataDTO) inps.readObject()) != null && this.client.isConnected() && !Thread.currentThread().isInterrupted()) {
                    log.info("Income client`s data here: {}", readed);

                    lastType = readed.type();
                    if (lastType.equals(NetDataType.AUTH_REQUEST)) {
                        doPlayerAuth(readed);
                    } else if (lastType.equals(NetDataType.HERO_REQUEST)) {
                        saveConnectedHero(readed);
                    } else if (lastType.equals(NetDataType.PING)) {
                        if (readed.worldUid().equals(gameController.getCurrentWorldUid())) {
                            // Сервер не знает в какой именно из его миров стучится клиент, который
                            //  сейчас загружен или другой, на этом же порту - потому сверяем.
                            log.info("Клиент пингует мир {}. Текущий мир Сервера {}", readed.worldUid(), gameController.getCurrentWorldUid());
                            push(ClientDataDTO.builder().type(NetDataType.PONG).build());
                        }
                    } else if (lastType.equals(NetDataType.SYNC)) {
                        server.broadcast(readed, this);
                    } else if (lastType.equals(NetDataType.DIE)) {
                        log.warn("Клиент {} сообщил о скорой смерти соединения.", clientUid);
                    } else if (lastType.equals(NetDataType.PONG)) {
                        log.debug("Клиент {} прислал PONG в знак того, что он всё еще жив.", clientUid);
                    } else {
                        log.error("Неопознанный тип входящего пакета: {}", readed.type());
                    }
                }
                log.warn("Соединение данного клиентского подключения завершено.");
            } catch (ClassNotFoundException cnf) {
                log.warn("Client`s input stream thread cant read class: {}", ExceptionUtils.getFullExceptionMessage(cnf));
            } catch (Exception inputStreamException) {
                if (!lastType.equals(NetDataType.PONG)) {
                    log.warn("Поймали ошибку входящего потока клиента: {}", ExceptionUtils.getFullExceptionMessage(inputStreamException));
                }
            }
            log.warn("Соединение-входной поток клиентского подключения завершено.");
        } catch (IOException e) {
            // надо бы проверить как-то, если закрыли соединение не умышленно, не сами:
            log.warn("Something wrong with client`s data stream: {}", ExceptionUtils.getFullExceptionMessage(e));
            new FOptionPane().buildFOptionPane("Подключение разорвано",
                    "Подключение с Клиентом %s было разорвано: %s".formatted(clientUid, e.getMessage()), 60, false);
        } catch (Exception e) {
            log.warn("Not handled exception here (4): {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        log.warn("Player's {} connection is full closed now.", clientUid);
        kill();
    }

    private void doPlayerAuth(ClientDataDTO readed) throws IOException {
        playerUid = readed.playerUid();
        playerName = readed.playerName();
        if (gameController.getCurrentWorld() == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current world");
        }

        // подготовка игрового мира, проверка пароля:
        World cw = gameController.getCurrentWorld();
        if (cw.getPasswordHash() != 0 && cw.getPasswordHash() != readed.passwordHash()) {
            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано! (пароль мира {} - пароль клиента {})",
                    playerName, playerUid, cw.getPasswordHash(), readed.passwordHash());
            isAuthorized.set(false);
            push(ClientDataDTO.builder().type(NetDataType.AUTH_DENIED).explanation("Не верный пароль").build());
        } else {
            log.info("Игрок {} ({}) успешно авторизован", readed.playerName(), readed.playerUid());
            // для создателя этот мир - Локальный,для удалённого игрока этот мир не может быть Локальным:
            cw.setLocalWorld(playerUid.equals(cw.getAuthor()));
            isAuthorized.set(true);
            push(ClientDataDTO.builder().type(NetDataType.AUTH_SUCCESS)
                    .worldUid(cw.getUid())
                    .world(cw)
                    .build());
        }
    }

    private void saveConnectedHero(ClientDataDTO readed) {
        playedHeroesService.addHero(HeroDTO.builder()
                .uid(readed.heroUuid())
                .heroName(readed.heroName())
                .type(readed.heroType())
                .power(readed.power())
                .speed(readed.speed())
                .position(readed.position())
                .vector(readed.vector())
                .level(readed.level())
                .experience(readed.experience())
                .curHealth(readed.hp())
                .maxHealth(readed.maxHp())
                .hurtLevel(readed.hurtLevel())
//                .buffs(readed.buffs())
//                .inventory(readed.inventory())
//                .inGameTime(readed.inGameTime())
                .worldUid(readed.world() == null ? gameController.getCurrentWorldUid() : readed.world().getUid())
                .ownerUid(readed.playerUid())
                .lastPlayDate(readed.lastPlayDate())
                .createDate(readed.createDate())
                .isOnline(readed.isOnline())
                .build());

        this.isAccepted.set(true);
        push(ClientDataDTO.builder().type(NetDataType.HERO_ACCEPTED).build());

//        this.isAccepted.set(false);
//        push(ClientDataDTO.builder().type(NetDataType.HERO_RESTRICTED).build());
    }

    public void push(ClientDataDTO data) {
        try {
            oos.writeObject(data);
            oos.flush();
        } catch (NotSerializableException nse) {
            log.warn("Output stream not serializable exception: {}", ExceptionUtils.getFullExceptionMessage(nse));
        } catch (SocketException se) {
            // надо бы проверить как-то, если не мы сами, умышленно вызвали это прерывание:
            log.warn("Some connected socket error: {}", ExceptionUtils.getFullExceptionMessage(se));
        } catch (IOException io) {
            log.warn("Output stream closing error: {}", ExceptionUtils.getFullExceptionMessage(io));
        } catch (Exception e) {
            log.warn("Not handled exception here (6): {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void kill() {
        log.warn("Destroy the client {} connection...", clientUid);

        gameController.getPlayedHeroesService().offlineSaveAndRemoveOtherHeroByPlayerUid(playerUid);

        if (!this.client.isClosed()) {
            try {
                // шлём подключенному Клиенту пожелание его смерти:
                push(ClientDataDTO.builder().type(NetDataType.DIE).build());
            } catch (Exception e) {
                log.warn("Push DIE-message error: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
            try {
                this.client.close();
            } catch (Exception e) {
                log.warn("Server client {} closing error: {}", clientUid, ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        ConnectedServerPlayer.this.interrupt();
    }

    public boolean isAccepted() {
        return this.isAccepted.get();
    }

    public boolean isAuthorized() {
        return this.isAuthorized.get();
    }

    public boolean isClosed() {
        return this.client.isClosed();
    }
}
