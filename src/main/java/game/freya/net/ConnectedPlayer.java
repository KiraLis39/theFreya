package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.components.FOptionPane;
import game.freya.GameController;
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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class ConnectedPlayer extends Thread implements Runnable {
    @Getter
    private final UUID clientUid;

    private final ObjectMapper mapper;
    private final GameController gameController;
    private final Socket client;
    private final Server server;
    private final InetAddress inetAddress;
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private ObjectOutputStream oos;
    @Getter
    private UUID playerUid;
    // это тот самый удалённый игрок, данные которого обновляются при каждом SYNC-запросе с клиента:
    private HeroDTO connectedHero;
    @Getter
    private String playerName;
    private volatile boolean doClose = false;

    public ConnectedPlayer(Server server, Socket client, GameController gameController) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.gameController = gameController;

        this.server = server;
        this.client = client;
        this.inetAddress = client.getInetAddress();
        this.clientUid = UUID.randomUUID();

        try {
            this.oos = new ObjectOutputStream(client.getOutputStream());
            push(ClientDataDTO.builder().type(NetDataType.PONG).build());
        } catch (IOException e) {
            log.error("Не удалось получить исходящий поток данных сокетного подключения {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        //setDaemon(true);
        setUncaughtExceptionHandler((t, e) -> log.error("Client`s socket thread exception: {}", ExceptionUtils.getFullExceptionMessage(e)));
        start();
    }

    @Override
    public void run() {
        log.info("Запущен новый поток для клиента {}...", clientUid);

        try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            ClientDataDTO readed;
            while ((readed = (ClientDataDTO) ois.readObject()) != null && !doClose) {
                log.info("Income client`s data here: {}", readed);

                NetDataType mesType = readed.type();
                if (mesType.equals(NetDataType.AUTH_REQUEST)) {
                    doPlayerAuth(readed);
                } else if (mesType.equals(NetDataType.HERO_REQUEST)) {
                    saveConnectedHero(readed);
                } else if (mesType.equals(NetDataType.PING)) {
                    push(ClientDataDTO.builder().type(NetDataType.PONG).build());
                } else if (mesType.equals(NetDataType.SYNC)) {
                    server.broadcast(readed, this);
                } else {
                    log.error("Неопознанный тип входящего пакета: {}", readed.type());
                }

                sleep(500);
            }

            log.warn("Close the player's {} connection...", clientUid);
            this.client.close();

        } catch (IOException e) {
            if (!this.doClose) {
                // если закрыли соединение не умышленно, не сами:
                log.warn("Something wrong with client`s data stream: {}", ExceptionUtils.getFullExceptionMessage(e));
                new FOptionPane().buildFOptionPane("Подключение разорвано",
                        "Связь с удалённым Сервером утеряна.", 60, false);
            }
        } catch (ClassNotFoundException cnf) {
            log.warn("Client`s input stream thread cant read class: {}", ExceptionUtils.getFullExceptionMessage(cnf));
        } catch (InterruptedException e) {
            log.warn("Client`s input stream thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
            interrupt();
        }
    }

    private void doPlayerAuth(ClientDataDTO readed) throws IOException {
        playerUid = readed.playerUid();
        playerName = readed.playerName();

        // подготовка игрового мира, проверка пароля:
        World cw = gameController.getCurrentWorld();
        if (cw == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current world");
        }
        if (cw.getPasswordHash() != 0 && cw.getPasswordHash() != readed.passwordHash()) {
            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано! (пароль мира {} - пароль клиента {})",
                    playerName, playerUid, cw.getPasswordHash(), readed.passwordHash());
            isAuthorized.set(false);
            push(ClientDataDTO.builder().type(NetDataType.AUTH_DENIED).explanation("Не верный пароль").build());
        } else {
            log.info("Игрок {} ({}) успешно авторизован", readed.playerName(), readed.playerUid());
            isAuthorized.set(true);
            // для создателя этот мир - Локальный:
            // для удалённого игрока этот мир не может быть Локальным:
            cw.setLocalWorld(playerUid.equals(cw.getAuthor()));
            push(ClientDataDTO.builder().type(NetDataType.AUTH_SUCCESS).world(cw).build());
        }
    }

    private void saveConnectedHero(ClientDataDTO readed) {
        this.connectedHero = HeroDTO.builder()
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
                .build();

        push(ClientDataDTO.builder().type(NetDataType.HERO_ACCEPTED).build());
//        push(ClientDataDTO.builder().type(NetDataType.HERO_RESTRICTED).build());

        // надо ли?
//        gameController.saveNewHero(this.heroDto);
    }

    public void push(ClientDataDTO data) {
        try {
            oos.writeObject(data);
        } catch (NotSerializableException nse) {
            log.warn("Output stream not serializable exception: {}", ExceptionUtils.getFullExceptionMessage(nse));
        } catch (IOException io) {
            log.warn("Output stream closing error: {}", ExceptionUtils.getFullExceptionMessage(io));
        }
    }

    public void kill() {
        log.warn("Destroy the client {} connection...", clientUid);
        this.doClose = true;

        try {
            if (this.oos != null) {
                push(ClientDataDTO.builder().type(NetDataType.DIE).build());
                this.oos.flush();
                this.oos.close();
            }
        } catch (IOException e) {
            log.warn("Output stream closing error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public InetAddress getInetAddress() {
        return this.inetAddress;
    }

    public boolean isAuthorized() {
        return this.isAuthorized.get();
    }

    public HeroDTO getHeroDto() {
        return this.connectedHero;
    }
}
