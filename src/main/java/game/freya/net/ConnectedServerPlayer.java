package game.freya.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDTO;
import game.freya.entities.World;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.data.events.EventDenied;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.net.data.events.EventPingPong;
import game.freya.net.data.events.EventPlayerAuth;
import game.freya.net.data.events.EventWorldData;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

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

    private final GameControllerService gameController;

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

    private NetDataEvent lastEvent;

    public ConnectedServerPlayer(Server server, Socket client, GameControllerService gameController) throws SocketException {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.server = server;
        this.playedHeroesService = gameController.getPlayedHeroesService();
        this.gameController = gameController;
        this.clientUid = UUID.randomUUID();

        this.client = client;
        this.client.setSendBufferSize(Constants.SOCKET_BUFFER_SIZE);
        this.client.setReceiveBufferSize(Constants.SOCKET_BUFFER_SIZE);
        // this.client.setReuseAddress(true);
        // this.client.setKeepAlive(true);
        this.client.setTcpNoDelay(true);
        this.client.setSoTimeout(Constants.SOCKET_CONNECTION_AWAIT_TIMEOUT);

        setDaemon(true);
        setUncaughtExceptionHandler((_, e) -> log.error("Client`s socket thread exception: {}", ExceptionUtils.getFullExceptionMessage(e)));
        start();
    }

    @Override
    public void run() {
        log.info("Запущен новый поток-клиент {}...", clientUid);

        try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
            this.oos = outs;

            // сразу шлём подключенному Клиенту сигнал, для "прокачки" соединения:
            push(ClientDataDTO.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.PONG)
                    .content(EventPingPong.builder().worldUid(gameController.getCurrentWorldUid()).build())
                    .build());

            try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                ClientDataDTO readed;
                while ((readed = (ClientDataDTO) inps.readObject()) != null && this.client.isConnected() && !Thread.currentThread().isInterrupted()) {
                    if (!readed.dataEvent().equals(NetDataEvent.PING) && !readed.dataEvent().equals(NetDataEvent.HERO_MOVING)) {
                        log.info("Игрок {} прислал на Сервер данные {} ({})", playerName, readed.dataType(), readed.dataEvent());
                    }

                    lastType = readed.dataType();
                    lastEvent = readed.dataEvent();

                    if (lastType.equals(NetDataType.AUTH_REQUEST)) {
                        doPlayerAuth(readed);
                    } else if (lastType.equals(NetDataType.HERO_REQUEST)) {
                        saveConnectedHero(readed);
                    } else if (lastType.equals(NetDataType.HERO_REMOTE_NEED)) {
                        sendHeroDataByRequest(readed);
                    } else if (lastType.equals(NetDataType.EVENT)) {
                        if (lastEvent.equals(NetDataEvent.PONG)) {
                            log.debug("Клиент {} прислал PONG в знак того, что он еще жив.", clientUid);
                        } else if (lastEvent.equals(NetDataEvent.PING)) {
                            doPongAnswerToClient(((EventPingPong) readed.content()).worldUid());
                        } else if (lastEvent.equals(NetDataEvent.CLIENT_DIE)) {
                            log.warn("Клиент {} сообщил о скорой смерти соединения.", clientUid);
                        } else if (lastEvent.equals(NetDataEvent.HERO_REGISTER)) {
                            saveConnectedHero(readed); // readed.heroUid!
                        } else {
                            server.broadcast(readed, this);
                        }
                    } else {
                        log.error("Неопознанный тип входящего пакета: {}", readed.dataType());
                    }
                }
                log.warn("Соединение данного клиентского подключения завершено.");
            }
            log.warn("Соединение-входной поток клиентского подключения завершено.");
        } catch (IOException e) {
            log.warn("Something wrong with client`s data stream: {}", ExceptionUtils.getFullExceptionMessage(e));
            if (playerName != null && !playerName.equals(gameController.getCurrentPlayerNickName())) {
                new FOptionPane().buildFOptionPane("Подключение разорвано",
                        "Подключение с %s было разорвано".formatted(playerName), 30, false);
            }
        } catch (ClassNotFoundException cnf) {
            log.warn("Client`s input stream thread cant read class: {}", ExceptionUtils.getFullExceptionMessage(cnf));
        } catch (Exception e) {
            if (!lastType.equals(NetDataType.EVENT) && !lastEvent.equals(NetDataEvent.PING)) {
                log.warn("Поймали ошибку потока клиента: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        log.warn("Player's {} connection is full closed now.", clientUid);
        kill();
    }

    private void sendHeroDataByRequest(ClientDataDTO data) {
        EventHeroRegister heroNeed = (EventHeroRegister) data.content();
        CharacterDTO found = gameController.getConnectedHeroes().stream()
                .filter(h -> h.getUid().equals(heroNeed.heroUid())).findFirst().orElse(null);
        if (found != null) {
            push(gameController.heroToCli(found, gameController.getCurrentPlayer()));
        } else {
            log.error("Запрошен герой {}, но такого нет в карте героев Сервера! Ответ невозможен.", heroNeed.heroUid());
        }
    }

    private void doPongAnswerToClient(UUID uuid) {
        if (uuid != null && uuid.equals(gameController.getCurrentWorldUid())) {
            // Сервер не знает в какой именно из его миров стучится клиент, который
            //  сейчас загружен или другой, на этом же порту - потому сверяем.
            log.info("Клиент успешно пингует мир {}", uuid);
            push(ClientDataDTO.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.PONG)
                    .content(EventPingPong.builder().worldUid(gameController.getCurrentWorldUid()).build())
                    .build());
        } else {
            log.debug("Пингуется не тот мир, потому WRONG_WORLD_PING");
            push(ClientDataDTO.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.WRONG_WORLD_PING)
                    .content(EventDenied.builder()
                            .explanation("Возможно, вы ищете другой мир, запущенный на этом Сервере данный момент. "
                                    + "Пожалуйста, уточните данные для подключения у администраторов Сервера.").build())
                    .build());
        }
    }

    private void doPlayerAuth(ClientDataDTO readed) throws IOException {
        EventPlayerAuth auth = (EventPlayerAuth) readed.content();
        playerUid = auth.ownerUid();
        playerName = auth.playerName();
        if (gameController.getCurrentWorld() == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current world");
        }

        // подготовка игрового мира, проверка пароля:
        World cw = gameController.getCurrentWorld();
        if (cw.getPasswordHash() != 0 && cw.getPasswordHash() != auth.passwordHash()) {
            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано! (пароль мира {} - пароль клиента {})",
                    playerName, playerUid, cw.getPasswordHash(), auth.passwordHash());
            isAuthorized.set(false);
            push(ClientDataDTO.builder()
                    .dataType(NetDataType.AUTH_DENIED)
                    .content(EventDenied.builder().explanation("Не верный пароль").build()).build());
        } else {
            log.info("Игрок {} ({}) успешно авторизован", auth.playerName(), auth.ownerUid());
            // для создателя этот мир - Локальный,для удалённого игрока этот мир не может быть Локальным:
            cw.setLocalWorld(playerUid.equals(cw.getAuthor()));
            isAuthorized.set(true);
            push(ClientDataDTO.builder()
                    .dataType(NetDataType.AUTH_SUCCESS)
                    .content(EventWorldData.builder()
                            .worldUid(cw.getUid())
                            .world(cw)
                            .build())
                    .build());
        }
    }

    private void saveConnectedHero(ClientDataDTO readed) {
        EventHeroRegister connected = (EventHeroRegister) readed.content();
        CharacterDTO hero;
        if (gameController.isHeroExist(connected.heroUid())) {
            hero = gameController.getHeroByUid(connected.heroUid());
            BeanUtils.copyProperties(readed, hero, "heroUid");
        } else {
            hero = gameController.saveNewHero((PlayCharacterDto) gameController.cliToHero(readed), false);
        }

        this.isAccepted.set(true);
        push(ClientDataDTO.builder()
                .dataType(NetDataType.HERO_ACCEPTED)
                .heroes(playedHeroesService.getHeroes())
                .build());

        hero.setOnline(true);
        playedHeroesService.addHero(hero);

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
            log.warn("Some connected socket error: {}", ExceptionUtils.getFullExceptionMessage(se));
            kill();
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
                push(ClientDataDTO.builder().dataType(NetDataType.EVENT).dataEvent(NetDataEvent.CLIENT_DIE).build());
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
