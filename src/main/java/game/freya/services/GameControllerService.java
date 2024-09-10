package game.freya.services;

import fox.FoxLogo;
import game.freya.WorldEngine;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.PlayerDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.entities.roots.World;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameFrame;
import game.freya.gui.panes.GameCanvas;
import game.freya.interfaces.iEnvironment;
import game.freya.net.ConnectedServerPlayer;
import game.freya.net.LocalSocketConnection;
import game.freya.net.Server;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroMoving;
import game.freya.net.data.events.EventHeroOffline;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.net.data.events.EventPlayerAuth;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameControllerService extends GameControllerBase {
    private final ArrayDeque<ClientDataDto> deque = new ArrayDeque<>();

    private final PlayerService playerService;

    private final CharacterService characterService;

    private final WorldService worldService;

    private final EventService eventService;
    private final WorldEngine worldEngine;

    private final GameFrame gameFrame;

    private final Server server;

    @Getter
    private final GameConfig gameConfig;

    @Getter
    private LocalSocketConnection localSocketConnection;

    private Thread pingThread;

    @Getter
    @Setter
    private volatile boolean isGameActive = false;

    @Getter
    private volatile boolean isRemoteHeroRequestSent = false;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

//            UIManager.put("nimbusBase", new Color(...));
//            UIManager.put("nimbusBlueGrey", new Color(...));
//            UIManager.put("control", new Color(...));

            // UIManager.put("Button.font", FONT);
            // UIManager.put("Label.font", FONT);
            // UIManager.put("OptionPane.cancelButtonText", "nope");
            // UIManager.put("OptionPane.okButtonText", "yup");
            // UIManager.put("OptionPane.inputDialogTitle", "Введите свой никнейм:");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(ex));
            }
        }

        // показываем лого:
        if (Constants.isShowStartLogo()) {
            try (InputStream is = Constants.class.getResourceAsStream("/images/logo.png")) {
                if (is != null) {
                    Constants.setLogo(new FoxLogo());
                    Constants.getLogo().start(getGameConfig().getAppVersion(),
                            Constants.getUserConfig().isFullscreen() ? FoxLogo.IMAGE_STYLE.FILL : FoxLogo.IMAGE_STYLE.DEFAULT,
                            FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is));
                }
            } catch (IOException e) {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/logo.png");
            }
        }

        // продолжаем подготовку к запуску игры пока лого отображается...
        if (Files.notExists(Path.of(Constants.getWorldsImagesDir()))) {
            Files.createDirectories(Path.of(Constants.getWorldsImagesDir()));
        }

        log.info("Check the current user in DB created...");
        checkCurrentPlayerExists();

        try {
            try (InputStream netResource = getClass().getResourceAsStream("/images/menu.png")) {
                assert netResource != null;
                Constants.CACHE.addIfAbsent("backMenuImage", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/menu_shadowed.png")) {
                assert netResource != null;
                Constants.CACHE.addIfAbsent("backMenuImageShadowed", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/green_arrow.png")) {
                assert netResource != null;
                Constants.CACHE.addIfAbsent("green_arrow", netResource.readAllBytes());
            }

            try (InputStream netResource = getClass().getResourceAsStream("/images/net.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("net", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/mock_01.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("mock_01", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/mock_02.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("mock_02", netResource.readAllBytes());
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/mock_03.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("mock_03", netResource.readAllBytes());
            }
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        log.info("The game is started!");
        this.gameFrame.showMainMenu(this);
    }

    public void exitTheGame(WorldDto world) {
        this.exitTheGame(world, -1);
    }

    public void exitTheGame(WorldDto world, int errCode) {
        saveTheGame(world);
        closeConnections();

        log.info("The game is finished!");
        System.exit(errCode > -1 ? errCode : 0);
    }

    private void closeConnections() {
        // закрываем Сервер:
        if (isServerIsOpen()) {
            localSocketConnection.setControlledExit(true);
            server.close();
        }

        // закрываем соединения:
        if (isSocketIsOpen()) {
            localSocketConnection.killSelf();
        }

        // закрываем БД:
        // Connect.close();
    }

    private void saveTheGame(WorldDto world) {
        log.info("Saving the game...");
        playerService.updateCurrentPlayer();
        if (world != null) {
            worldService.save(world);
        }
        log.info("The game is saved.");
    }

    private void checkCurrentPlayerExists() {
        // если сменили никнейм в конфиге:
        playerService.getCurrentPlayer()
                .setNickName(Constants.getUserConfig().getUserName());
    }

    public void loadScreen(ScreenType screenType) {
        log.info("Try to load screen {}...", screenType);
        switch (screenType) {
            case MENU_SCREEN -> gameFrame.loadMenuScreen();
            case GAME_SCREEN -> gameFrame.loadGameScreen();
            default -> log.error("Unknown screen failed to load: {}", screenType);
        }
    }

    public void startClientBroadcast() {
        log.info("Начало вещания на Сервер...");
        if (getNetDataTranslator() != null && !getNetDataTranslator().isInterrupted()) {
            getNetDataTranslator().interrupt();
        }

        // создаётся поток текущего состояния на Сервер:
        setNetDataTranslator(new Thread(() -> {
            while (!getNetDataTranslator().isInterrupted() && isSocketIsOpen() && isGameActive) {
                if (!deque.isEmpty()) {
                    while (!deque.isEmpty()) {
                        localSocketConnection.toServer(deque.pollFirst());
                    }
                }

                try {
                    Thread.sleep(Constants.SERVER_BROADCAST_DELAY);
                } catch (InterruptedException e) {
                    log.warn("Прерывание потока отправки данных на сервер!");
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Поток трансляции данных игры на Сервер остановлен.");
        }));
        getNetDataTranslator().setName("Game-to-Server data broadcast thread");
        getNetDataTranslator().setDaemon(true);
        getNetDataTranslator().start();
    }

    public void sendPacket(ClientDataDto data) {
        this.deque.offer(data);
    }

    public CharacterDto saveNewHero(PlayCharacterDto newHeroDto, boolean setAsCurrent) {
        if (!characterService.isHeroExist(newHeroDto.getUid())) {
            CharacterDto saved = characterService.saveHero(newHeroDto);
            if (setAsCurrent) {
//                playedHeroes.addCurrentHero(saved);
            } else {
//                playedHeroes.addHero(saved);
            }
            return saved;
        }
        return characterService.getByUid(newHeroDto.getUid());
    }

    public void saveNewRemoteHero(ClientDataDto readed) {
        saveNewHero((PlayCharacterDto) cliToHero(readed), false);
    }

    public CharacterDto justSaveAnyHero(CharacterDto aNewHeroDto) {
        return characterService.saveHero(aNewHeroDto);
    }

    public void deleteWorld(UUID worldUid) {
        log.warn("Удаление Героев мира {}...", worldUid);
        characterService.findAllByWorldUuid(worldUid).forEach(hero -> characterService.deleteHeroByUuid(hero.getUid()));

        log.warn("Удаление мира {}...", worldUid);
        worldService.delete(worldUid);
    }

    public void setCurrentHeroOfflineAndSave(Duration gameDuration) {
//        playedHeroes.offlineSaveAndRemoveCurrentHero(gameDuration);
    }

    public void justSaveOnlineHero(Duration duration) {
//        playedHeroes.setCurrentHeroInGameTime(duration == null ? 0 : duration.toMillis());
//        characterService.saveHero(playedHeroes.getCurrentHero());
    }

    public void doScreenShot(Point location, Rectangle canvasRect) {
        new Screenshoter().doScreenshot(new Rectangle(
                        location.x + 9 + canvasRect.getBounds().x,
                        location.y + 30 + canvasRect.getBounds().y,
                        canvasRect.getBounds().width, canvasRect.getBounds().height
                ),
                Constants.getWorldsImagesDir() + worldService.getCurrentWorld().getUid());
    }

    /**
     * Метод отрисовки всех героев подключенных к миру и авторизованных игроков.
     *
     * @param v2D    хост для отрисовки.
     * @param canvas класс холста.
     */
    public void drawHeroes(Graphics2D v2D, GameCanvas canvas) {
        if (isCurrentWorldIsNetwork()) { // если игра по сети:
//            for (CharacterDTO hero : getConnectedHeroes()) {
//                if (playedHeroes.isCurrentHero(hero)) {
//                    // если это текущий герой:
//                    if (!Constants.isPaused()) {
//                        moveHeroIfAvailable(canvas); // узкое место!
//                    }
//                    hero.draw(v2D);
//                } else if (canvas.getViewPort().getBounds().contains(hero.getLocation())) {
//                    // если чужой герой в пределах видимости:
//                    hero.draw(v2D);
//                }
//            }
        } else { // если не-сетевая игра:
            if (!Constants.isPaused()) {
                moveHeroIfAvailable(canvas); // узкое место!
            }

//            if (!playedHeroes.isCurrentHeroNotNull()) {
//                log.info("Потеряли текущего игрока. Что-то случилось? Выходим...");
//                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Окно игры не смогло получить текущего игрока для отрисовки");
//            }
//            playedHeroes.getCurrentHero().draw(v2D);
        }
    }

    public boolean isHeroActive(CharacterDto hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getLocation()) && hero.isOnline();
    }

    private void moveHeroIfAvailable(GameCanvas canvas) {
        if (isPlayerMoving()) {
            Rectangle visibleRect = canvas.getViewPort().getBounds();
//            MovingVector vector = playedHeroes.getCurrentHeroVector();
//            Point2D.Double plPos = playedHeroes.getCurrentHeroPosition();

            double hrc = (visibleRect.x + ((visibleRect.getWidth() - visibleRect.x) / 2d));
//            boolean isViewMovableX = plPos.x > hrc - 30 && plPos.x < hrc + 30;

            double vrc = (visibleRect.y + ((visibleRect.getHeight() - visibleRect.y) / 2d));
//            boolean isViewMovableY = plPos.y > vrc - 30 && plPos.y < vrc + 30;

            if (isPlayerMovingUp()) {
//                vector = isPlayerMovingRight() ? MovingVector.UP_RIGHT : isPlayerMovingLeft() ? MovingVector.LEFT_UP : MovingVector.UP;
            } else if (isPlayerMovingDown()) {
//                vector = isPlayerMovingRight() ? MovingVector.RIGHT_DOWN : isPlayerMovingLeft() ? MovingVector.DOWN_LEFT : MovingVector.DOWN;
            }

            if (isPlayerMovingRight()) {
//                vector = isPlayerMovingUp() ? MovingVector.UP_RIGHT : isPlayerMovingDown() ? MovingVector.RIGHT_DOWN : MovingVector.RIGHT;
            } else if (isPlayerMovingLeft()) {
//                vector = isPlayerMovingUp() ? MovingVector.LEFT_UP : isPlayerMovingDown() ? MovingVector.DOWN_LEFT : MovingVector.LEFT;
            }

            // перемещаем камеру к ГГ:
//            if (!visibleRect.contains(playedHeroes.getCurrentHero().getLocation())) {
//                canvas.moveViewToPlayer(0, 0);
//            }

            // move hero:
            int[] collisionMarker = hasCollision();
//            playedHeroes.setCurrentHeroVector(vector.mod(vector, collisionMarker));
//            if (!playedHeroes.getCurrentHeroVector().equals(MovingVector.NONE)) {
//                // двигаемся по направлению вектора (взгляда):
//                for (int i = 0; i < playedHeroes.getCurrentHero().getSpeed(); i++) {
//                    playedHeroes.getCurrentHero().move();
//                }
//            } else {
//                // тогда, стоя на месте, просто указываем направление вектора (взгляда):
//                playedHeroes.setCurrentHeroVector(vector);
//            }

            // send moving data to Server:
//            sendPacket(eventService.buildMove(playedHeroes.getCurrentHero()));

            // move map:
//            switch (vector) {
//                case UP -> {
//                    if (isViewMovableY) {
//                        canvas.dragDown(getCurrentHeroSpeed());
//                    }
//                }
//                case UP_RIGHT -> {
//                    if (isViewMovableY) {
//                        canvas.dragDown(getCurrentHeroSpeed());
//                    }
//                    if (isViewMovableX) {
//                        canvas.dragLeft(getCurrentHeroSpeed());
//                    }
//                }
//                case RIGHT -> {
//                    if (isViewMovableX) {
//                        canvas.dragLeft(getCurrentHeroSpeed());
//                    }
//                }
//                case RIGHT_DOWN -> {
//                    if (isViewMovableX) {
//                        canvas.dragLeft(getCurrentHeroSpeed());
//                    }
//                    if (isViewMovableY) {
//                        canvas.dragUp(getCurrentHeroSpeed());
//                    }
//                }
//                case DOWN -> {
//                    if (isViewMovableY) {
//                        canvas.dragUp(getCurrentHeroSpeed());
//                    }
//                }
//                case DOWN_LEFT -> {
//                    if (isViewMovableY) {
//                        canvas.dragUp(getCurrentHeroSpeed());
//                    }
//                    if (isViewMovableX) {
//                        canvas.dragRight(getCurrentHeroSpeed());
//                    }
//                }
//                case LEFT -> {
//                    if (isViewMovableX) {
//                        canvas.dragRight(getCurrentHeroSpeed());
//                    }
//                }
//                case LEFT_UP -> {
//                    if (isViewMovableX) {
//                        canvas.dragRight(getCurrentHeroSpeed());
//                    }
//                    if (isViewMovableY) {
//                        canvas.dragDown(getCurrentHeroSpeed());
//                    }
//                }
//                default -> log.info("Обнаружено несанкционированное направление {}", vector);
//            }
        }
    }

    private int[] hasCollision() {
        // если сущность - не призрак:
//        if (playedHeroes.getCurrentHero().hasCollision()) {
//            Rectangle heroCollider = playedHeroes.getCurrentHeroCollider();
//
//            // проверка коллизии с краем мира:
//            Area worldMapBorder = new Area(new Rectangle(-12, -12,
//                    getCurrentWorldMap().getWidth() + 24, getCurrentWorldMap().getHeight() + 24));
//            worldMapBorder.subtract(new Area(new Rectangle(0, 0, getCurrentWorldMap().getWidth(), getCurrentWorldMap().getHeight())));
//            if (worldMapBorder.intersects(heroCollider)) {
//                return findVectorCorrection(worldMapBorder, heroCollider);
//            }
//
//            // проверка коллизий с объектами:
//            for (EnvironmentDto env : worldService.getCurrentWorld().getEnvironments()) {
//                if (env.hasCollision() && env.getCollider().intersects(heroCollider)) {
//                    return findVectorCorrection(env.getCollider(), heroCollider);
//                }
//            }
//        }

        return new int[]{0, 0};
    }

    private int[] findVectorCorrection(Shape envColl, Rectangle heroColl) {
        int[] result; // y, x

        final Point upDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroColl.y);
        final Point upDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroColl.y);
        final Point downDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroColl.y + heroColl.height);
        final Point downDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroColl.y + heroColl.height);

        final Point leftDotX01 = new Point(heroColl.x, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point leftDotX02 = new Point(heroColl.x, (int) (heroColl.y + heroColl.height * 0.66d));
        final Point rightDotX01 = new Point(heroColl.x + heroColl.width, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point rightDotX02 = new Point(heroColl.x + heroColl.width, (int) (heroColl.y + heroColl.height * 0.66d));

        result = new int[]{
                // y
                envColl.contains(upDotY01) || envColl.contains(upDotY02) ? -1
                        : envColl.contains(downDotY01) || envColl.contains(downDotY02) ? 1 : 0,

                // x
                envColl.contains(leftDotX01) || envColl.contains(leftDotX02) ? -1
                        : envColl.contains(rightDotX01) || envColl.contains(rightDotX02) ? 1 : 0
        };
        return result;
    }

    public void deleteHero(UUID heroUid) {
        characterService.deleteHeroByUuid(heroUid);
    }

    public BufferedImage getCurrentPlayerAvatar() {
        return playerService.getCurrentPlayer().getAvatar();
    }

    public String getCurrentPlayerNickName() {
        return playerService.getCurrentPlayer().getNickName();
    }

    public WorldDto saveNewWorld(WorldDto newWorld) {
        newWorld.setCreatedBy(getCurrentPlayerUid());
        newWorld.generate();
        return worldService.save(newWorld);
    }

    public boolean openServer() {
        server.open(this);
        server.untilOpen(30_000);
        return server.isOpen();
    }

    public boolean closeServer() {
        localSocketConnection.setControlledExit(true);
        server.close();
        server.untilClose(6_000);
        return server.isClosed();
    }

    public void stopBroadcast() {
        if (getNetDataTranslator() != null && getNetDataTranslator().isAlive()) {
            getNetDataTranslator().interrupt();
        }
    }

    public void closeSocket() {
        localSocketConnection.killSelf();
    }

    public UUID getCurrentPlayerUid() {
        return playerService.getCurrentPlayer().getUid();
    }

    public UUID getCurrentWorldUid() {
        return worldService.getCurrentWorld().getUid();
    }

    public void setLastPlayedWorldUuid(UUID lastWorldUuid) {
        playerService.getCurrentPlayer().setLastPlayedWorldUid(lastWorldUuid);
    }

    public Set<iEnvironment> getWorldEnvironments(Rectangle2D.Double rectangle) {
        return worldService.getEnvironmentsFromRectangle(rectangle);
    }

    public List<CharacterDto> findAllHeroesByWorldUid(UUID uid) {
        return characterService.findAllByWorldUuid(uid);
    }

    public List<CharacterDto> getMyCurrentWorldHeroes() {
        return characterService.findAllByWorldUidAndOwnerUid(worldService.getCurrentWorld().getUid(), playerService.getCurrentPlayer().getUid());
    }

    public String getCurrentWorldTitle() {
        return worldService.getCurrentWorld() == null ? null : worldService.getCurrentWorld().getName();
    }

    public VolatileImage getCurrentWorldMap() {
        return worldEngine.getGameMap();
    }

    public void getDrawCurrentWorld(Graphics2D v2D) throws AWTException {
        worldService.getCurrentWorld().draw(v2D);
    }

    public void saveCurrentWorld() {
        worldService.saveCurrentWorld();
    }

    public void initCurrentWorld(GameCanvas gameCanvas) {
        worldService.getCurrentWorld().init(gameCanvas, this);
    }

    public long getCurrentHeroInGameTime() {
//        if (playedHeroes.getCurrentHero() != null) {
//            return playedHeroes.getCurrentHeroInGameTime();
//        }
        return -1;
    }

//    public UUID getCurrentHeroUid() {
//        return playedHeroes.getCurrentHeroUid();
//    }

    public Point2D.Double getCurrentHeroPosition() {
//        if (playedHeroes.getCurrentHero() != null) {
//            return playedHeroes.getCurrentHeroPosition();
//        }
        return null;
    }

    public byte getCurrentHeroSpeed() {
//        if (playedHeroes.getCurrentHero() != null) {
//            return playedHeroes.getCurrentHeroSpeed();
//        }
        return -1;
    }

    public List<WorldDto> findAllWorldsByNetworkAvailable(boolean isNetworkAvailable) {
        return worldService.findAllByNetAvailable(isNetworkAvailable);
    }

    public boolean isCurrentWorldIsNetwork() {
        return worldService.getCurrentWorld().isNetAvailable();
    }

    public CharacterDto findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return characterService.findHeroByNameAndWorld(heroName, worldUid);
    }

    public boolean isServerIsOpen() {
        return !server.isClosed();
    }

    public boolean isSocketIsOpen() {
        return localSocketConnection != null && localSocketConnection.isOpen();
    }

    public long getConnectedClientsCount() {
        return server.connected();
    }

    public Set<ConnectedServerPlayer> getConnectedPlayers() {
        return server.getPlayers();
    }

//    public Collection<CharacterDTO> getConnectedHeroes() {
//        return playedHeroes.getHeroes();
//    }

//    public MovingVector getCurrentHeroVector() {
//        return playedHeroes.getCurrentHeroVector();
//    }

    public boolean connectToServer(String host, Integer port, int passwordHash) {
        this.localSocketConnection = new LocalSocketConnection();

        // подключаемся к серверу:
        if (isSocketIsOpen() && localSocketConnection.getActiveHost().equals(host)) {
            // верно ли подобное поведение?
            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", localSocketConnection.getActiveHost());
        } else {
            localSocketConnection.openSocket(host, port, this, false);
        }

        if (!isSocketIsOpen()) {
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED,
                    "No reached socket connection to " + host + (port == null ? "" : ":" + port));
        } else if (!host.equals(getCurrentSocketHost())) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
        }

        // передаём свои данные для авторизации:
        localSocketConnection.toServer(ClientDataDto.builder()
                .dataType(NetDataType.AUTH_REQUEST)
                .content(EventPlayerAuth.builder()
                        .ownerUid(getCurrentPlayerUid())
                        .playerName(getCurrentPlayerNickName())
                        .passwordHash(passwordHash)
                        .build())
                .build());

        Thread authThread = Thread.startVirtualThread(() -> {
            while (!localSocketConnection.isAuthorized() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });
        try {
            authThread.join(9_000);
            if (authThread.isAlive()) {
                log.error("Так и не получили успешной авторизации от Сервера.");
                authThread.interrupt();
                localSocketConnection.killSelf();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            authThread.interrupt();
            localSocketConnection.killSelf();
            return false;
        }
    }

    private String getCurrentSocketHost() {
        return localSocketConnection.getActiveHost();
    }

    public boolean ping(String host, Integer port, UUID worldUid) {
        LocalSocketConnection pingConn = null;
        try (LocalSocketConnection conn = new LocalSocketConnection()) {
            pingConn = conn;
            // подключаемся к серверу:
            conn.openSocket(host, port, this, true);

            // ждём пока получим ответ PONG от Сервера:
            pingThread = Thread.startVirtualThread(() -> {
                long was = System.currentTimeMillis();
                while (conn.isAlive() && !conn.isPongReceived() && System.currentTimeMillis() - was < 15_000) {
                    Thread.yield();
                }
            });
            pingThread.join();

            // проверяем получен ли ответ:
            if (!conn.isPongReceived()) {
                pingThread.interrupt();
                log.warn("Пинг к Серверу {}:{} не прошел (1): {}", host, port, conn.getLastExplanation());
                return false;
            } else if (conn.getLastExplanation() != null && conn.getLastExplanation().equals(worldUid.toString())) {
                log.warn("Пинг к Серверу {}:{} прошел успешно!", host, port);
                return true;
            }
        } catch (InterruptedException e) {
            pingThread.interrupt();
            log.warn("Пинг к Серверу {}:{} не прошел (2): {}", host, port, pingConn.getLastExplanation());
        } catch (GlobalServiceException gse) {
            log.warn("Пинг к Серверу {}:{} не прошел (3): {} ({})",
                    host, port, gse.getMessage(), pingConn != null ? pingConn.getLastExplanation() : null);
        } catch (Exception e) {
            log.warn("Пинг к Серверу {}:{} не прошел (4): {}", host, port, ExceptionUtils.getFullExceptionMessage(e));
        }
        return false;
    }

    public void breakPing() {
        if (pingThread != null) {
            pingThread.interrupt();
        }
    }

//    public String getCurrentHeroName() {
//        return getCurrentHero().getName();
//    }

//    public World getCurrentWorld() {
//        WorldDTO currentWorld = worldService.getCurrentWorld();
//        if (currentWorld == null) {
//            return null;
//        }
//        return worldMapper.toEntity(currentWorld);
//    }

    public void setCurrentWorld(UUID selectedWorldUuid) {
        Optional<World> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            setLastPlayedWorldUuid(selectedWorldUuid);
//            worldService.setCurrentWorld(worldMapper.toDto(selected.get()));
            return;
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid.toString());
    }

    public void setCurrentWorld(WorldDto newWorld) {
        worldService.setCurrentWorld(newWorld);
    }

    public void saveServerWorldAndSetAsCurrent(World world) {
        Optional<World> wOpt = worldService.findByUid(world.getUid());
        if (wOpt.isPresent()) { // тут происходит подмена worldUuid?
            World old = wOpt.get();
            BeanUtils.copyProperties(world, old);
//            worldService.setCurrentWorld(worldMapper.toDto(old));
            worldService.saveCurrentWorld();
        } else {
//            worldService.setCurrentWorld(worldService.save(worldMapper.toDto(world)));
        }
    }

//    public boolean isCurrentHeroOnline() {
//        return getCurrentHero() != null && getCurrentHero().isOnline();
//    }

    public boolean registerCurrentHeroOnServer() {
        log.info("Отправка данных текущего героя на Сервер...");
//        localSocketConnection.toServer(heroToCli(getCurrentHero(), playerService.getCurrentPlayer()));

        Thread heroCheckThread = Thread.startVirtualThread(() -> {
            while (!localSocketConnection.isAccepted() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });

        try {
            heroCheckThread.join(9_000);
            if (heroCheckThread.isAlive()) {
                log.error("Не получили разрешения на Героя от Сервера.");
                heroCheckThread.interrupt();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            heroCheckThread.interrupt();
            return false;
        }
    }

    public void setCurrentHero(CharacterDto hero) {
//        if (playedHeroes.isCurrentHeroNotNull()) {
//            if (playedHeroes.isCurrentHero(hero)) {
//                playedHeroes.setCurrentHeroOnline(true);
//            } else if (playedHeroes.isCurrentHeroOnline()) {
//                // если online другой герой - снимаем:
//                log.info("Снимаем он-лайн с Героя {} и передаём этот статус Герою {}...", getCurrentHero().getName(), hero.getName());
//                playedHeroes.offlineSaveAndRemoveCurrentHero(null);
//            }
//        }
        log.info("Теперь активный Герой - {}", hero.getName());
//        playedHeroes.addCurrentHero(hero);
    }

    public String getCurrentWorldAddress() {
        return worldService.getCurrentWorld().getNetworkAddress();
    }

    public int getCurrentWorldPassword() {
        return worldService.getCurrentWorld().getPasswordHash();
    }

    public boolean isCurrentWorldIsLocal() {
        return worldService.getCurrentWorld().isLocalWorld();
    }

    public UUID getCurrentPlayerLastPlayedWorldUid() {
        return playerService.getCurrentPlayer().getLastPlayedWorldUid();
    }

    public void setCurrentPlayerLastPlayedWorldUid(UUID uid) {
        playerService.setCurrentPlayerLastPlayedWorldUid(uid);
    }

    public String getServerAddress() {
        return server.getAddress();
    }

    /**
     * В этот метод приходят данные обновлений сетевого мира (Сервера).
     * Здесь собираются все изменения, движения игроков, атаки, лечения, взаимодействия и т.п. для
     * мержа с мирами других сетевых участников.
     *
     * @param data модель обновлений для сетевого мира от другого участника игры.
     */
    public void syncServerDataWithCurrentWorld(@NotNull ClientDataDto data) {
        log.debug("Получены данные для синхронизации {} игрока {} (герой {})",
                data.dataEvent(), data.content().ownerUid(), data.content().heroUid());

//        CharacterDTO aim = playedHeroes.getHero(data.content().heroUid());
//        if (aim == null) {
//            log.warn("Герой {} не существует в БД. Отправляется запрос на его модель к Серверу, ожидается...", data.content().heroUid());
//            requestHeroFromServer(data.content().heroUid());
//            return;
//        }

        if (data.dataEvent() == NetDataEvent.HERO_OFFLINE) {
            EventHeroOffline event = (EventHeroOffline) data.content();
            UUID offlinePlayerUid = event.ownerUid();
            log.info("Игрок {} отключился от Сервера. Удаляем его из карты активных Героев...", offlinePlayerUid);
            offlineSaveAndRemoveOtherHeroByPlayerUid(offlinePlayerUid);
        }

        if (data.dataEvent() == NetDataEvent.HERO_MOVING) {
            EventHeroMoving event = (EventHeroMoving) data.content();
//            aim.setLocation(event.positionX(), event.positionY());
//            aim.setVector(event.vector());
        }

        // Обновляем здоровье, максимальное здоровье, силу, бафы-дебафы, текущий инструмент в руках и т.п. другого игрока:
        // ...

        // Обновляем окружение, выросшие-срубленные деревья, снесенные, построенные постройки, их характеристики и т.п.:
        // ...

        // Обновляем данные квестов, задач, групп, союзов, обменов и т.п.:
        // ...

        // Обновляем статусы он-лайн, ветхость, таймауты и прочее...
        // ...

        // Кладём назад обновленного:
//        currentHero = aim;
    }

    public void exitToMenu(Duration gameDuration) {
        // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
        if (isGameActive) {
            isGameActive = false;

            saveCurrentWorld();

//            if (gameDuration != null && playedHeroes.isCurrentHeroNotNull()) {
//                setCurrentHeroOfflineAndSave(gameDuration);
//            }

            // если игра сетевая и локальная - останавливаем сервер при выходе из игры:
            if (isCurrentWorldIsNetwork()) {
                if (isCurrentWorldIsLocal()) {
                    if (closeServer()) {
                        log.info("Сервер успешно остановлен");
                    } else {
                        log.warn("Возникла ошибка при закрытии сервера.");
                    }
                }
                if (isSocketIsOpen()) {
                    closeSocket();
                }
            }


            loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    public boolean isWorldExist(UUID worldUid) {
        return worldService.isWorldExist(worldUid);
    }

    public void clearConnectedHeroes() {
//        playedHeroes.clear();
    }

    public boolean isHeroExist(UUID uid) {
        return characterService.isHeroExist(uid);
    }

    public CharacterDto getHeroByUid(UUID uid) {
        return characterService.getByUid(uid);
    }

    public void offlineSaveAndRemoveOtherHeroByPlayerUid(UUID clientUid) {
//        playedHeroes.offlineSaveAndRemoveOtherHeroByPlayerUid(clientUid);
    }

    public void requestHeroFromServer(UUID uid) {
        localSocketConnection.toServer(ClientDataDto.builder()
                .dataType(NetDataType.HERO_REMOTE_NEED)
                .content(EventHeroRegister.builder().heroUid(uid).build())
                .build());
        setRemoteHeroRequestSent(true);
    }

    public ClientDataDto heroToCli(CharacterDto found, PlayerDto currentPlayer) {
        return characterService.heroToCli(found, currentPlayer);
    }

    public CharacterDto cliToHero(ClientDataDto readed) {
        return characterService.cliToHero(readed);
    }

    // проброска currentPlayer в класс-Thread, который не может быть @Component:
    public PlayerDto getCurrentPlayer() {
        return playerService.getCurrentPlayer();
    }

    public void setRemoteHeroRequestSent(boolean b) {
        this.isRemoteHeroRequestSent = b;
    }
}
