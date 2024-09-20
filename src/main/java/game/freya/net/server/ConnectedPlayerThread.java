package game.freya.net.server;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventDenied;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class ConnectedPlayerThread extends Thread implements Runnable {
    private final GameControllerService gameControllerService;
    private final Socket client;
    private final AtomicBoolean isAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isAuthorized = new AtomicBoolean(false);
    private ObjectOutputStream oos;
    private NetDataType lastType;
    private NetDataEvent lastEvent;
    private PlayCharacterDto connectedHero;

    @Getter
    private UUID playerUid;

    @Getter
    private String playerName;

    public ConnectedPlayerThread(Socket client, GameControllerService gameControllerService) throws SocketException {
        this.gameControllerService = gameControllerService;

        this.client = client;
        this.client.setSendBufferSize(Constants.getGameConfig().getSocketSendBufferSize());
        this.client.setReceiveBufferSize(Constants.getGameConfig().getSocketReceiveBufferSize());
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
        log.info("Запущен новый поток-клиент {}...", client.getInetAddress());

        try (ObjectOutputStream outs = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream(), client.getSendBufferSize()))) {
            this.oos = outs;

            // сразу шлём подключенному Клиенту сигнал, для "прокачки" соединения:
            sendFirstPongToClient();

            try (ObjectInputStream inps = new ObjectInputStream(new BufferedInputStream(client.getInputStream(), client.getReceiveBufferSize()))) {
                ClientDataDto readed;

                // в цикле, до обрыва связи, читаем входящие сообщения:
                while ((readed = (ClientDataDto) inps.readObject()) != null && !isInterrupted()) {
                    try {
                        tryToReadNextData(readed);
                    } catch (Exception e) {
                        log.error("Возникла ошибка во входном потоке Клиента {}: {}", client.getInetAddress(), ExceptionUtils.getFullExceptionMessage(e));
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

        log.warn("Player's {} connection is full closed now.", client.getInetAddress());
        kill();
    }

    private void tryToReadNextData(ClientDataDto readed) throws IOException {
        if (!readed.dataEvent().equals(NetDataEvent.PING) && !readed.dataEvent().equals(NetDataEvent.HERO_MOVING)) {
            log.info("Игрок {} прислал на Сервер данные {} ({})", playerName, readed.dataType(), readed.dataEvent());
        }

        lastType = readed.dataType();
        lastEvent = readed.dataEvent();

        switch (lastType) {
            case AUTH_DATA -> doPlayerAuth((EventPlayerAuth) readed.content()); // клиент прислал пароль
            case HERO_DATA -> saveConnectedHero(readed); // клиент прислал своего Героя
            case EVENT -> { // клиент прислал событие:
                switch (lastEvent) {
                    case PONG ->
                            log.debug("Клиент {} прислал PONG в знак того, что он еще жив.", client.getInetAddress());
                    case PING -> doPongAnswerToClient(((EventPingPong) readed.content()).worldUid());
                    case CLIENT_DIE ->
                            log.warn("Клиент {} сообщил о скорой смерти соединения.", client.getInetAddress());
                    case HERO_REGISTER -> saveConnectedHero(readed);
                    default -> Constants.getServer().broadcast(readed, this);
                }
            }
            default -> log.error("Неопознанный тип входящего пакета: {}", readed.dataType());
        }
    }

    private void sendFirstPongToClient() {
        push(ClientDataDto.builder()
                .dataType(NetDataType.EVENT)
                .dataEvent(NetDataEvent.PONG)
                .content(EventPingPong.builder()
                        .worldUid(gameControllerService.getWorldService().getCurrentWorld().getUid())
                        .build())
                .build());
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

    private void doPlayerAuth(EventPlayerAuth authEvent) {
        // здесь появляются uuid и имя Игрока:
        this.playerUid = authEvent.ownerUid();
        this.playerName = authEvent.playerName();

        // подготовка игрового мира, проверка пароля:
        WorldDto cw = gameControllerService.getWorldService().getCurrentWorld();
        if (!cw.getPassword().isBlank() && !cw.getPassword().equals(authEvent.password())) {
            log.error("Игрок {} ({}) ввёл не верный пароль. В доступе отказано! (пароль мира {} - пароль клиента {})",
                    playerName, playerUid, cw.getPassword(), authEvent.password());
            push(ClientDataDto.builder()
                    .dataType(NetDataType.AUTH_DENIED)
                    .content(EventDenied.builder().explanation("Не верный пароль").build()).build());
        } else {
            log.info("Игрок {} ({}) успешно авторизован", authEvent.playerName(), authEvent.ownerUid());

            // для создателя этот мир - Локальный, для удалённого игрока этот мир не может быть Локальным:
            cw.setLocal(playerUid.equals(cw.getCreatedBy())); // todo на кой это вообще?..

            isAuthorized.set(true);
            push(ClientDataDto.builder()
                    .dataType(NetDataType.AUTH_SUCCESS)
                    .content(EventWorldData.builder()
                            .worldUid(cw.getUid())
//                            .world(cw) // todo: подумать как лучше сделать иначе.
                            .build())
                    .build());
        }
    }

    public PlayCharacterDto getHero() {
        return this.connectedHero;
    }

    private void saveConnectedHero(ClientDataDto readed) {
        // смотрим, подключался ли ранее и есть ли уже у нас в базе герой:
        Optional<CharacterDto> charOpt = gameControllerService.getCharacterService().getByUid(readed.content().heroUid());

        PlayCharacterDto hero;
        if (charOpt.isPresent()) {
            hero = (PlayCharacterDto) charOpt.get();
            BeanUtils.copyProperties(readed, hero, "uid");
        } else {
            hero = gameControllerService.getCharacterService()
                    .justSaveAnyHero(gameControllerService.getClientDataMapper().clientDataToPlayCharacterDto(readed));
        }

        this.isAccepted.set(true);
        // поздравляем игрока с успешной проверкой и шлём ему остальных героев для синхронизации:
        push(ClientDataDto.builder()
                .dataType(NetDataType.HERO_ACCEPTED)
                .heroes(Constants.getServer().getAcceptedHeroes())
                .build());

        hero.setOnline(true);
        this.connectedHero = hero;

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
        log.warn("Destroy the client {} connection...", client.getInetAddress());

//        gameController.getPlayedHeroes().offlineSaveAndRemoveOtherHeroByPlayerUid(playerUid);

        if (!this.client.isClosed()) {
            try {
                // шлём подключенному Клиенту пожелание его смерти:
                push(ClientDataDto.builder().dataType(NetDataType.EVENT).dataEvent(NetDataEvent.CLIENT_DIE).build());
                if (!this.client.isOutputShutdown()) {
                    this.client.shutdownOutput();
                }
            } catch (Exception e) {
                log.warn("Push DIE-message error: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            try {
                if (!this.client.isInputShutdown()) {
                    this.client.shutdownInput();
                }
                this.client.close();
            } catch (Exception e) {
                log.warn("Server client {} closing error: {}", client.getInetAddress(), ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        this.isAccepted.set(false);
        this.isAuthorized.set(false);
        interrupt();
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
