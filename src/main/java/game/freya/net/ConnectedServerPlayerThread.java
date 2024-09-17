package game.freya.net;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDto;
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
public class ConnectedServerPlayerThread extends Thread implements Runnable {
    @Getter
    private final UUID clientUid;

    private final GameControllerService gameControllerService;

    private final Socket client;

    private final AtomicBoolean isAccepted = new AtomicBoolean(false);

    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);

    private ObjectOutputStream oos;

    @Getter
    private UUID playerUid;

    @Getter
    private String playerName;

    private NetDataType lastType;

    private NetDataEvent lastEvent;

    public ConnectedServerPlayerThread(Socket client, GameControllerService gameControllerService) throws SocketException {
        this.gameControllerService = gameControllerService;
        this.clientUid = UUID.randomUUID();

        this.client = client;
        this.client.setSendBufferSize(Constants.getGameConfig().getSocketBufferSize());
        this.client.setReceiveBufferSize(Constants.getGameConfig().getSocketBufferSize());
        // this.client.setReuseAddress(true);
        // this.client.setKeepAlive(true);
        this.client.setTcpNoDelay(true);
        this.client.setSoTimeout(Constants.getGameConfig().getConnectionNoDataTimeout());

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
            push(ClientDataDto.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.PONG)
                    .content(EventPingPong.builder()
                            .worldUid(gameControllerService.getWorldService().getCurrentWorld().getUid())
                            .build())
                    .build());

            try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                ClientDataDto readed;
                while ((readed = (ClientDataDto) inps.readObject()) != null && this.client.isConnected() && !Thread.currentThread().isInterrupted()) {
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
                            Constants.getServer().broadcast(readed, this);
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
            if (playerName != null && !playerName.equals(gameControllerService.getPlayerService().getCurrentPlayer().getNickName())) {
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

    private void sendHeroDataByRequest(ClientDataDto data) {
        EventHeroRegister heroNeed = (EventHeroRegister) data.content();
//        CharacterDTO found = gameController.getConnectedHeroes().stream()
//                .filter(h -> h.getUid().equals(heroNeed.heroUid())).findFirst().orElse(null);
//        if (found != null) {
//            push(gameController.heroToCli(found, gameController.getCurrentPlayer()));
//        } else {
        log.error("Запрошен герой {}, но такого нет в карте героев Сервера! Ответ невозможен.", heroNeed.heroUid());
//        }
    }

    private void doPongAnswerToClient(UUID uid) {
        if (uid != null && uid.equals(gameControllerService.getWorldService().getCurrentWorld().getUid())) {
            // Сервер не знает в какой именно из его миров стучится клиент, который
            //  сейчас загружен или другой, на этом же порту - потому сверяем.
            log.info("Клиент успешно пингует мир {}", uid);
            push(ClientDataDto.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.PONG)
                    .content(EventPingPong.builder()
                            .worldUid(gameControllerService.getWorldService().getCurrentWorld().getUid())
                            .createdBy(gameControllerService.getWorldService().getCurrentWorld().getCreatedBy())
                            .build())
                    .build());
        } else {
            log.debug("Пингуется не тот мир, потому WRONG_WORLD_PING");
            push(ClientDataDto.builder()
                    .dataType(NetDataType.EVENT)
                    .dataEvent(NetDataEvent.WRONG_WORLD_PING)
                    .content(EventDenied.builder()
                            .explanation("Возможно, вы ищете другой мир, запущенный на этом Сервере данный момент. "
                                    + "Пожалуйста, уточните данные для подключения у администраторов Сервера.").build())
                    .build());
        }
    }

    private void doPlayerAuth(ClientDataDto readed) throws IOException {
        EventPlayerAuth auth = (EventPlayerAuth) readed.content();
        playerUid = auth.ownerUid();
        playerName = auth.playerName();
        if (gameControllerService.getWorldService().getCurrentWorld() == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current world");
        }

        // подготовка игрового мира, проверка пароля:
        WorldDto cw = gameControllerService.getWorldService().getCurrentWorld();
        if (cw.getPassword().isBlank() && !cw.getPassword().equals(auth.password())) {
            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано! (пароль мира {} - пароль клиента {})",
                    playerName, playerUid, cw.getPassword(), auth.password());
            isAuthorized.set(false);
            push(ClientDataDto.builder()
                    .dataType(NetDataType.AUTH_DENIED)
                    .content(EventDenied.builder().explanation("Не верный пароль").build()).build());
        } else {
            log.info("Игрок {} ({}) успешно авторизован", auth.playerName(), auth.ownerUid());
            // для создателя этот мир - Локальный,для удалённого игрока этот мир не может быть Локальным:
            cw.setLocal(playerUid.equals(cw.getCreatedBy()));
            isAuthorized.set(true);
            push(ClientDataDto.builder()
                    .dataType(NetDataType.AUTH_SUCCESS)
                    .content(EventWorldData.builder()
                            .worldUid(cw.getUid())
                            .world(cw)
                            .build())
                    .build());
        }
    }

    private void saveConnectedHero(ClientDataDto readed) {
        EventHeroRegister connected = (EventHeroRegister) readed.content();
        CharacterDto hero;
        if (gameControllerService.getCharacterService().isHeroExist(connected.heroUid())) {
            hero = gameControllerService.getCharacterService().getByUid(connected.heroUid()).get();
            BeanUtils.copyProperties(readed, hero, "heroUid");
        } else {
            hero = gameControllerService.getCharacterService()
                    .justSaveAnyHero(gameControllerService.getEventService().cliToHero(readed));
        }

        this.isAccepted.set(true);
        push(ClientDataDto.builder()
                .dataType(NetDataType.HERO_ACCEPTED)
//                .heroes(playedHeroes.getHeroes())
                .build());

        hero.setOnline(true);
//        playedHeroes.addHero(hero);

        // or:
//        this.isAccepted.set(false);
//        push(ClientDataDto.builder().dataType(NetDataType.HERO_RESTRICTED).build());
    }

    public void push(ClientDataDto data) {
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

//        gameController.getPlayedHeroes().offlineSaveAndRemoveOtherHeroByPlayerUid(playerUid);

        if (!this.client.isClosed()) {
            try {
                // шлём подключенному Клиенту пожелание его смерти:
                push(ClientDataDto.builder().dataType(NetDataType.EVENT).dataEvent(NetDataEvent.CLIENT_DIE).build());
            } catch (Exception e) {
                log.warn("Push DIE-message error: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
            try {
                this.client.close();
            } catch (Exception e) {
                log.warn("Server client {} closing error: {}", clientUid, ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        ConnectedServerPlayerThread.this.interrupt();
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
