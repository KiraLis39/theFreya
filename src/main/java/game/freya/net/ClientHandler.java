package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.NetDataType;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
@RequiredArgsConstructor
public class ClientHandler extends Thread implements Runnable {
    private final ObjectMapper mapper;
    private final String clientId;
    private final Socket client;
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;
    private final GameController gameController;
    @Getter
    private HeroDTO heroDto;
    @Getter
    private UUID playerUid;
    @Getter
    private String playerName;
    @Setter
    private long dynamicReadStreamDelay = 1_000L; // оно повышать-понижать при нагрузке или т.т.
    @Getter
    private AtomicBoolean isAutorized = new AtomicBoolean(false);

    public ClientHandler(String clientId, Socket client, GameController gameController) throws IOException {
        this.mapper = new ObjectMapper();
        this.gameController = gameController;

        this.clientId = clientId;
        this.client = client;

        this.outputStream = new DataOutputStream(client.getOutputStream()); // DataOutputStream
        this.inputStream = new DataInputStream(client.getInputStream()); // DataOutputStream

        //setDaemon(true);
        setUncaughtExceptionHandler((t, e) -> log.error("Client`s socket thread exception: {}", ExceptionUtils.getFullExceptionMessage(e)));
        start();
    }

    @Override
    public void run() {
        log.info("Run the client`s thread for {}...", clientId);
        try {
            ClientDataDTO readed;
            while ((readed = mapper.readValue(inputStream.readUTF(), ClientDataDTO.class)) != null) { // .readUTF() // .readObject()
                log.info("Income client`s data here: {}", readed);

                NetDataType mesType = readed.type();
                if (mesType.equals(NetDataType.AUTH)) {
                    if (heroDto == null) {
                        playerUid = readed.playerUid();
                        playerName = readed.playerName();

                        // подготовка игрового мира, проверка пароля:
                        World cw = gameController.getCurrentWorld();
                        if (cw.getPasswordHash() != -1 && cw.getPasswordHash() != readed.passwordHash()) {
                            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано!", playerName, playerUid);
                            push(ClientDataDTO.builder().type(NetDataType.DENIED).explanation("Не верный пароль").build());
                        } else {
                            // инициализация героя подключившегося игрока
                            heroDto = HeroDTO.builder()
                                    .uid(readed.heroUuid())
                                    .heroName(readed.heroName())
                                    .type(readed.heroType())
                                    .level(readed.level())
                                    .experience(readed.experience())
                                    .health(readed.hp())
                                    .hurtLevel(readed.hurtLevel())
                                    .maxHealth(readed.maxHp())
                                    .position(readed.position())
                                    .vector(readed.vector())
                                    .speed(readed.speed())
                                    .currentAttackPower(readed.power())
//                            .buffs(readed.buffs())
//                            .inventory(readed.inventory())
//                            .inGameTime(readed.inGameTime())
//                            .ownerUid(readed.ownerUid())
//                            .worldUid(readed.wUid())
//                            .createDate(readed.created())
                                    .isOnline(readed.isOnline())
                                    .build();
                            push(ClientDataDTO.builder().type(NetDataType.SUCCESS).world(cw).build());
                            log.info("Игрок {} ({}) успешно авторизован", readed.playerName(), readed.playerUid());
                            isAutorized.set(true);
                        }
                    } else {
                        log.error("Игрок {} ({}) уже был авторизован, но повторно шлёт тип AUTH", readed.playerName(), readed.playerUid());
                        push(ClientDataDTO.builder().type(NetDataType.DENIED).explanation("Уже был авторизован").build());
                    }
                } else if (mesType.equals(NetDataType.SUCCESS)) {
                    log.info("Сервер успешно принял авторизацию! Получен мир {}", readed.world().getTitle());
                    gameController.setCurrentWorld(readed.world());
                } else if (mesType.equals(NetDataType.DENIED)) {
                    log.warn("Сервер отклонил авторизацию по причине: {}", readed.explanation());
                    new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил соединение: %s"
                            .formatted(readed.explanation()), 5, false);
                    kill();
                } else {
                    // здесь будет мерж входящих данных с игровым миром...
                    log.info("Входящие данные игрока {} типа {}...", readed.heroName(), readed.type());
                }

                sleep(dynamicReadStreamDelay);
            }
        } catch (IOException e) {
            log.warn("Something wrong with client`s data stream: {}", ExceptionUtils.getFullExceptionMessage(e));
        } catch (InterruptedException e) {
            log.warn("Client`s input stream thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
            interrupt();
        }
    }

    public void push(ClientDataDTO data) throws IOException {
        outputStream.writeUTF(mapper.writeValueAsString(data));
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

        interrupt();
    }

    public boolean isAutorized() {
        return this.isAutorized.get();
    }
}
