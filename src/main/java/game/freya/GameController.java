package game.freya;

import com.fasterxml.jackson.core.JsonProcessingException;
import fox.FoxLogo;
import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.config.Media;
import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gl.Collider3D;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.Game;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.interfaces.iEnvironment;
import game.freya.items.prototypes.Environment;
import game.freya.mappers.WorldMapper;
import game.freya.net.ConnectedServerPlayer;
import game.freya.net.LocalSocketConnection;
import game.freya.net.PlayedHeroesService;
import game.freya.net.Server;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.data.NetConnectTemplate;
import game.freya.net.data.events.EventHeroMoving;
import game.freya.net.data.events.EventHeroOffline;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.net.data.events.EventPlayerAuth;
import game.freya.services.EventService;
import game.freya.services.HeroService;
import game.freya.services.PlayerService;
import game.freya.services.TextureService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController extends GameControllerBase {

    @Value("${app.appName}")
    private String appName;

    @Value("${app.appVersion}")
    private String appVersion;

    @Value("${app.appCompany}")
    private String appCompany;

    private final ArrayDeque<ClientDataDTO> deque = new ArrayDeque<>();

    private final PlayerService playerService;

    @Getter
    private final TextureService textureManager;

    private final HeroService heroService;

    private final WorldService worldService;

    private final EventService eventService;

    private final WorldMapper worldMapper;

    private final WindowManager windowManager;

    private final PlayedHeroesService playedHeroesService;

    private final Server server;

    private final float camZspeed = 0f;

    private final float pitchSpeed = 0.15f;

    private final float yawSpeed = 0.33f;

    @Getter
    private LocalSocketConnection localSocketConnection;

    private Thread pingThread;

    @Getter
    @Setter
    private volatile boolean isGameActive = false;

    @Getter
    private volatile boolean isRemoteHeroRequestSent = false;

    @Getter
    private boolean isAccelerated = false, isSneaked = false, isZoomed = false;

    @Getter
    @Setter
    private float heroHeight = -6;

    @Getter
    private float velocity = 0;

    @Getter
    private float cameraPitch = 30;

    @Getter
    private float cameraYaw = 0;

    private Thread sneakThread;

    private void loadAudio() {
        // TinySound:
        try {
            Media.add("jump", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/jump.wav")).getFile()));
            Media.add("landing", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/landing.wav")).getFile()));
            Media.add("lifelost", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/lifelost.wav")).getFile()));
            Media.add("touch", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/touch.wav")).getFile()));
            Media.add("win", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/win.wav")).getFile()));
            Media.add("gameover", new File(Objects.requireNonNull(GameController.class.getResource("/audio/sounds/gameover.wav")).getFile()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // FoxPlayer:
//        FoxPlayer.getVolumeConverter().setMinimum(-50);
//
////        voicePlayer.load(audioVoicesDir);
//
//        musicPlayer.load(audioMusicDir);
//        musicPlayer.mute(userConf.isMusicMuted());
//        musicPlayer.setVolume(userConf.getMusicVolume());
//
//        backgPlayer.load(audioBackgDir);
//        backgPlayer.mute(userConf.isBackgMuted());
//        backgPlayer.setVolume(userConf.getBackgVolume());
////        backgPlayer.setAudioBufDim(1024); // 4096
//
//        soundPlayer.load(audioSoundDir);
//        soundPlayer.setParallelPlayable(true);
//        soundPlayer.setLooped(false);
//        soundPlayer.mute(userConf.isSoundMuted());
//        soundPlayer.setVolume(userConf.getSoundVolume());
////        soundPlayer.setAudioBufDim(8192); // 8192
    }

    public void setCameraYaw(double yaw) {
        if (yaw != 0) {
            cameraYaw += yaw * yawSpeed;
            if (cameraYaw > 360) {
                cameraYaw = 0;
            }
            if (cameraYaw < 0) {
                cameraYaw = 360;
            }
        }
    }

    public void setCameraPitch(double pitch) {
        if (pitch != 0) {
            cameraPitch += pitch * pitchSpeed;
            if (cameraPitch < 0) {
                cameraPitch = 0;
            }
            if (cameraPitch > 180) {
                cameraPitch = 180;
            }
        }
    }

    @PostConstruct
    public void init() throws IOException {
        Constants.setAppName(appName);
        Constants.setAppCompany(appCompany);
        Constants.setAppVersion(appVersion);

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

            // UIManager.put("nimbusBase", new Color(...));
            // UIManager.put("nimbusBlueGrey", new Color(...));
            // UIManager.put("control", new Color(...));

            // UIManager.put("Button.font", FONT);
            // UIManager.put("Label.font", FONT);
            // UIManager.put("OptionPane.cancelButtonText", "nope");
            // UIManager.put("OptionPane.okButtonText", "yup");
            // UIManager.put("OptionPane.inputDialogTitle", "Введите свой никнейм:");

            // UIManager.put("FileChooser.saveButtonText", "Сохранить");
            // UIManager.put("FileChooser.cancelButtonText", "Отмена");
            // UIManager.put("FileChooser.openButtonText", "Выбрать");
            // UIManager.put("FileChooser.fileNameLabelText", "Наименование файла");
            // UIManager.put("FileChooser.filesOfTypeLabelText", "Типы файлов");
            // UIManager.put("FileChooser.lookInLabelText", "Директория");
            // UIManager.put("FileChooser.saveInLabelText", "Сохранить в директории");
            // UIManager.put("FileChooser.folderNameLabelText", "Путь директории");

        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(ex));
            }
        }

        // показываем лого:
        if (Constants.getUserConfig().isShowStartLogo()) {
            try (InputStream is = Constants.class.getResourceAsStream("/images/logo/logo_0.png");
                 InputStream is2 = Constants.class.getResourceAsStream("/images/logo/logo_1.png")
            ) {
                if (is != null && is2 != null) {
                    Constants.setLogo(new FoxLogo());
                    Constants.getLogo().start(appVersion,
                            Constants.getUserConfig().isFullscreen() ? FoxLogo.IMAGE_STYLE.FILL : FoxLogo.IMAGE_STYLE.DEFAULT,
                            FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is), ImageIO.read(is2));
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
            try (InputStream netResource = getClass().getResourceAsStream("/images/cursors/yellow.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("yellow", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/cursors/blue.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("blue", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/cursors/red.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("red", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/cursors/cross.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("cross", ImageIO.read(netResource));
            }
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        loadAudio();

        log.info("The game is started!");
        windowManager.appStart(this);
    }

    public void exitTheGame(WorldDTO world) {
        this.exitTheGame(world, -1);
    }

    public void exitTheGame(WorldDTO world, int errCode) {
//        try {
//            backgPlayer.stop();
//            musicPlayer.stop();
//            soundPlayer.stop();
//            voicePlayer.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            ModsLoaderEngine.stopMods();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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
        closeDataBaseConnection();
    }

    private void saveTheGame(WorldDTO world) {
        log.info("Saving the game...");
        playerService.updateCurrentPlayer();
        if (world != null) {
            worldService.save(world);
        }
        log.info("The game is saved.");
    }

    private void checkCurrentPlayerExists() {
        Optional<Player> curPlayer = playerService.findByUid(Constants.getUserConfig().getUserId());
        if (curPlayer.isEmpty()) {
            curPlayer = playerService.findByMail(Constants.getUserConfig().getUserMail());
            if (curPlayer.isPresent()) {
                log.warn("Не был найден в базе данных игрок по uuid {}, но он найден по почте {}.",
                        Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                Constants.getUserConfig().setUserId(curPlayer.get().getUid());
            }
        }

        // set current player:
        Player found = curPlayer.orElse(null);
        if (found == null) {
            found = playerService.createPlayer();
        }
        playerService.setCurrentPlayer(found);

        // если сменили никнейм в конфиге:
        playerService.getCurrentPlayer().setNickName(Constants.getUserConfig().getUserName());
    }

    public void loadScreen(ScreenType screenType) {
        log.info("Try to load screen {}...", screenType.name());
        windowManager.loadScreen(screenType);
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
                    Thread.sleep(Constants.getGameConfig().getServerBroadcastDelay());
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

    public void sendPacket(ClientDataDTO data) {
        this.deque.offer(data);
    }

    private String getCurrentHeroInventoryJson() {
        try {
            return playedHeroesService.getCurrentHeroInventoryJson();
        } catch (JsonProcessingException e) {
            log.error("Произошла ошибка при парсинге инвентаря героя {} ({})", getCurrentHeroName(), getCurrentHeroUid());
            return "{}";
        }
    }

    private String getCurrentHeroBuffsJson() {
        try {
            return playedHeroesService.getCurrentHeroBuffsJson();
        } catch (JsonProcessingException e) {
            log.error("Произошла ошибка при парсинге бафов героя {} ({})", getCurrentHeroName(), getCurrentHeroUid());
            return "{}";
        }
    }

    private int getCurrentHeroMaxOil() {
        return playedHeroesService.getCurrentHeroMaxOil();
    }

    private int getCurrentHeroOil() {
        return playedHeroesService.getCurrentHeroCurOil();
    }

    public HeroDTO saveNewHero(HeroDTO aNewHeroDto, boolean setAsCurrent) {
        if (!heroService.isHeroExist(aNewHeroDto.getCharacterUid())) {
            HeroDTO saved = heroService.saveHero(aNewHeroDto);
            if (setAsCurrent) {
                playedHeroesService.addCurrentHero(saved);
            } else {
                playedHeroesService.addHero(saved);
            }
            return saved;
        }
        return heroService.getByUid(aNewHeroDto.getCharacterUid());
    }

    public void saveNewRemoteHero(ClientDataDTO readed) {
        saveNewHero(cliToHero(readed), false);
    }

    public HeroDTO justSaveAnyHero(HeroDTO aNewHeroDto) {
        return heroService.saveHero(aNewHeroDto);
    }

    public void deleteWorld(UUID worldUid) {
        log.warn("Удаление Героев мира {}...", worldUid);
        heroService.findAllByWorldUuid(worldUid).forEach(hero -> heroService.deleteHeroByUuid(hero.getCharacterUid()));

        log.warn("Удаление мира {}...", worldUid);
        worldService.delete(worldUid);
    }

    public void setCurrentHeroOfflineAndSave(Duration gameDuration) {
        playedHeroesService.offlineSaveAndRemoveCurrentHero(gameDuration);
    }

    public void justSaveOnlineHero(Duration duration) {
        playedHeroesService.setCurrentHeroInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.saveHero(playedHeroesService.getCurrentHero());
    }

    public void doScreenShot(Point location, Rectangle canvasRect) {
        new Screenshoter().doScreenshot(new Rectangle(
                        location.x + 9 + canvasRect.getBounds().x,
                        location.y + 30 + canvasRect.getBounds().y,
                        canvasRect.getBounds().width, canvasRect.getBounds().height
                ),
                Constants.getWorldsImagesDir() + worldService.getCurrentWorld().getUid(), false);
    }

    /**
     * Метод отрисовки всех героев подключенных к миру и авторизованных игроков.
     *
     * @param v2D    хост для отрисовки.
     * @param canvas класс холста.
     */
    public void drawHeroes(Graphics2D v2D, Game canvas) {
        if (isCurrentWorldIsNetwork()) { // если игра по сети:
            for (HeroDTO hero : getConnectedHeroes()) {
                if (playedHeroesService.isCurrentHero(hero)) {
                    // если это текущий герой:
                    if (!Constants.isPaused()) {
                        moveHeroIfAvailable(); // узкое место!
                    }
                    hero.draw(v2D);
                }
//                else if (canvas.getViewPort().getBounds().contains(hero.getLocation())) {
//                    // если чужой герой в пределах видимости:
//                    hero.draw(v2D);
//                }
            }
        } else { // если не-сетевая игра:
            if (!Constants.isPaused()) {
                moveHeroIfAvailable(); // узкое место!
            }

            if (!playedHeroesService.isCurrentHeroNotNull()) {
                log.info("Потеряли текущего игрока. Что-то случилось? Выходим...");
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Окно игры не смогло получить текущего игрока для отрисовки");
            }
            playedHeroesService.getCurrentHero().draw(v2D);
        }
    }

    public boolean isHeroActive(HeroDTO hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getLocation()) && hero.isOnline();
    }

    private void moveHeroIfAvailable() {
        if (isPlayerMoving()) {
            Rectangle visibleRect = windowManager.getViewPortBounds();
            MovingVector vector = playedHeroesService.getCurrentHeroVector();
            Point2D.Double plPos = playedHeroesService.getCurrentHeroPosition();

            double hrc = (visibleRect.x + ((visibleRect.getWidth() - visibleRect.x) / 2d));
            boolean isViewMovableX = plPos.x > hrc - 30 && plPos.x < hrc + 30;

            double vrc = (visibleRect.y + ((visibleRect.getHeight() - visibleRect.y) / 2d));
            boolean isViewMovableY = plPos.y > vrc - 30 && plPos.y < vrc + 30;

            if (isPlayerMovingUp()) {
                vector = isPlayerMovingRight() ? MovingVector.UP_RIGHT : isPlayerMovingLeft() ? MovingVector.LEFT_UP : MovingVector.UP;
            } else if (isPlayerMovingDown()) {
                vector = isPlayerMovingRight() ? MovingVector.RIGHT_DOWN : isPlayerMovingLeft() ? MovingVector.DOWN_LEFT : MovingVector.DOWN;
            }

            if (isPlayerMovingRight()) {
                vector = isPlayerMovingUp() ? MovingVector.UP_RIGHT : isPlayerMovingDown() ? MovingVector.RIGHT_DOWN : MovingVector.RIGHT;
            } else if (isPlayerMovingLeft()) {
                vector = isPlayerMovingUp() ? MovingVector.LEFT_UP : isPlayerMovingDown() ? MovingVector.DOWN_LEFT : MovingVector.LEFT;
            }

            // move hero:
            int[] collisionMarker = hasCollision();
            playedHeroesService.setCurrentHeroVector(vector.mod(vector, collisionMarker));
            if (!playedHeroesService.getCurrentHeroVector().equals(MovingVector.NONE)) {
                // двигаемся по направлению вектора (взгляда):
                for (int i = 0; i < playedHeroesService.getCurrentHero().getSpeed(); i++) {
                    playedHeroesService.getCurrentHero().move();
                }
            } else {
                // тогда, стоя на месте, просто указываем направление вектора (взгляда):
                playedHeroesService.setCurrentHeroVector(vector);
            }

            // send moving data to Server:
            sendPacket(eventService.buildMove(playedHeroesService.getCurrentHero()));

            // move map:
            switch (vector) {
                case UP -> {
                    if (isViewMovableY) {
                        windowManager.dragDown(getCurrentHeroSpeed());
                    }
                }
                case UP_RIGHT -> {
                    if (isViewMovableY) {
                        windowManager.dragDown(getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        windowManager.dragLeft(getCurrentHeroSpeed());
                    }
                }
                case RIGHT -> {
                    if (isViewMovableX) {
                        windowManager.dragLeft(getCurrentHeroSpeed());
                    }
                }
                case RIGHT_DOWN -> {
                    if (isViewMovableX) {
                        windowManager.dragLeft(getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        windowManager.dragUp(getCurrentHeroSpeed());
                    }
                }
                case DOWN -> {
                    if (isViewMovableY) {
                        windowManager.dragUp(getCurrentHeroSpeed());
                    }
                }
                case DOWN_LEFT -> {
                    if (isViewMovableY) {
                        windowManager.dragUp(getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        windowManager.dragRight(getCurrentHeroSpeed());
                    }
                }
                case LEFT -> {
                    if (isViewMovableX) {
                        windowManager.dragRight(getCurrentHeroSpeed());
                    }
                }
                case LEFT_UP -> {
                    if (isViewMovableX) {
                        windowManager.dragRight(getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        windowManager.dragDown(getCurrentHeroSpeed());
                    }
                }
                default -> log.info("Обнаружено несанкционированное направление {}", vector);
            }
        }
    }

    private int[] hasCollision() {
        // если сущность - не призрак:
        if (playedHeroesService.getCurrentHero().hasCollision()) {
            Collider3D heroCollider = playedHeroesService.getCurrentHeroCollider();

            // проверка коллизии с краем мира:
            Area worldMapBorder = new Area(new Rectangle(-12, -12,
                    getCurrentWorldMap().getWidth() + 24, getCurrentWorldMap().getHeight() + 24));
            worldMapBorder.subtract(new Area(new Rectangle(0, 0, getCurrentWorldMap().getWidth(), getCurrentWorldMap().getHeight())));
            if (worldMapBorder.intersects(heroCollider.getFlatRectangle())) {
                return findVectorCorrection(worldMapBorder, heroCollider.getFlatRectangle());
            }

            // проверка коллизий с объектами:
            for (Environment env : worldService.getCurrentWorld().getEnvironments()) {
                if (env.hasCollision() && env.getCollider().intersects(heroCollider.getFlatRectangle())) {
                    return findVectorCorrection(env.getCollider().getShape(), heroCollider.getFlatRectangle());
                }
            }
        }

        return new int[]{0, 0};
    }

    private int[] findVectorCorrection(Shape envColl, Rectangle2D heroColl) {
        int[] result; // y, x

        final Point2D.Double upDotY01 = new Point2D.Double(heroColl.getX() + heroColl.getWidth() * 0.33d, heroColl.getY());
        final Point2D.Double upDotY02 = new Point2D.Double(heroColl.getX() + heroColl.getWidth() * 0.66d, heroColl.getY());
        final Point2D.Double downDotY01 = new Point2D.Double(heroColl.getX() + heroColl.getWidth() * 0.33d, heroColl.getY() + heroColl.getHeight());
        final Point2D.Double downDotY02 = new Point2D.Double(heroColl.getX() + heroColl.getWidth() * 0.66d, heroColl.getY() + heroColl.getHeight());

        final Point2D.Double leftDotX01 = new Point2D.Double(heroColl.getX(), heroColl.getY() + heroColl.getHeight() * 0.33d);
        final Point2D.Double leftDotX02 = new Point2D.Double(heroColl.getX(), heroColl.getY() + heroColl.getHeight() * 0.66d);
        final Point2D.Double rightDotX01 = new Point2D.Double(heroColl.getX() + heroColl.getWidth(), heroColl.getY() + heroColl.getHeight() * 0.33d);
        final Point2D.Double rightDotX02 = new Point2D.Double(heroColl.getX() + heroColl.getWidth(), heroColl.getY() + heroColl.getHeight() * 0.66d);

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
        heroService.deleteHeroByUuid(heroUid);
    }

    public BufferedImage getCurrentPlayerAvatar() {
        return playerService.getCurrentPlayer().getAvatar();
    }

    public String getCurrentPlayerNickName() {
        return playerService.getCurrentPlayer().getNickName();
    }

    public WorldDTO saveNewWorld(WorldDTO newWorld) {
        newWorld.setAuthor(getCurrentPlayerUid());
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

    public Set<iEnvironment> getWorldEnvironments(Rectangle rectangle) {
        return worldService.getEnvironmentsFromRectangle(rectangle);
    }

    public List<HeroDTO> findAllHeroesByWorldUid(UUID uid) {
        return heroService.findAllByWorldUuid(uid);
    }

    public List<HeroDTO> getMyCurrentWorldHeroes() {
        return heroService.findAllByWorldUidAndOwnerUid(worldService.getCurrentWorld().getUid(), playerService.getCurrentPlayer().getUid());
    }

    public String getCurrentWorldTitle() {
        return worldService.getCurrentWorld() == null ? null : worldService.getCurrentWorld().getTitle();
    }

    public VolatileImage getCurrentWorldMap() {
        return worldService.getCurrentWorld().getGameMap();
    }

    public void getDrawCurrentWorld(Graphics2D v2D) throws AWTException {
        worldService.getCurrentWorld().draw(v2D);
    }

    public void saveCurrentWorld() {
        worldService.saveCurrentWorld();
    }

    public long getCurrentHeroInGameTime() {
        if (playedHeroesService.getCurrentHero() != null) {
            return playedHeroesService.getCurrentHeroInGameTime();
        }
        return -1;
    }

    public UUID getCurrentHeroUid() {
        return playedHeroesService.getCurrentHeroUid();
    }

    public Point2D.Double getCurrentHeroPosition() {
        if (playedHeroesService.getCurrentHero() != null) {
            return playedHeroesService.getCurrentHeroPosition();
        }
        return null;
    }

    public float getCurrentHeroSpeed() {
        if (playedHeroesService.getCurrentHero() != null) {
            return playedHeroesService.getCurrentHeroSpeed();
        }
        return -1;
    }

    public List<WorldDTO> findAllWorldsByNetworkAvailable(boolean isNetworkAvailable) {
        return worldService.findAllByNetAvailable(isNetworkAvailable);
    }

    public boolean isCurrentWorldIsNetwork() {
        WorldDTO cw = worldService.getCurrentWorld();
        return cw != null && cw.isNetAvailable();
    }

    public HeroDTO findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return heroService.findHeroByNameAndWorld(heroName, worldUid);
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

    public Collection<HeroDTO> getConnectedHeroes() {
        return playedHeroesService.getHeroes();
    }

    public MovingVector getCurrentHeroVector() {
        return playedHeroesService.getCurrentHeroVector();
    }

    public void connectToServer(NetConnectTemplate connectionTemplate) {
//        getHeroesListPane().setVisible(false);

        windowManager.setConnectionAwait(true);
//        getNetworkListPane().repaint(); // костыль для отображения анимации

        if (connectionTemplate.address().isBlank()) {
            new FOptionPane().buildFOptionPane("Ошибка адреса:", "Адрес сервера не может быть пустым.", 10, true);
        }

        // 1) приходим сюда с host:port для подключения
        String address = connectionTemplate.address().trim();
        String h = address.contains(":") ? address.split(":")[0].trim() : address;
        Integer p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;
//        getNetworkListPane().repaint(); // костыль для отображения анимации
        try {
            // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
            if (connectToServer(h.trim(), p, connectionTemplate.passwordHash())) {
                // 3) проверка героя в этом мире:
                chooseOrCreateHeroForWorld(getCurrentWorldUid());
            } else {
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, getLocalSocketConnection().getLastExplanation());
            }
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            if (gse.getErrorCode().equals("ER07")) {
                new FOptionPane().buildFOptionPane("Не доступно:", gse.getMessage(), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            }
        } catch (IllegalThreadStateException tse) {
            log.error("Connection Thread state exception: {}", ExceptionUtils.getFullExceptionMessage(tse));
        } catch (Exception e) {
            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка подключения '%s'.\n"
                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10/13:13958)")
                    .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
        } finally {
            //gameController.closeSocket();
            windowManager.setConnectionAwait(false);
        }
    }

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
        localSocketConnection.toServer(ClientDataDTO.builder()
                .dataType(NetDataType.AUTH_REQUEST)
                .content(EventPlayerAuth.builder()
                        .playerUid(getCurrentPlayerUid())
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

    /**
     * После выбора мира - приходим сюда для создания нового героя или
     * выбора существующего, для игры в данном мире.
     *
     * @param worldUid uuid выбранного для игры мира.
     */
    public void chooseOrCreateHeroForWorld(UUID worldUid) {
//        getWorldsListPane().setVisible(false);
//        getWorldCreatingPane().setVisible(false);
//        getNetworkListPane().setVisible(false);
//        getNetworkCreatingPane().setVisible(false);

        setCurrentWorld(worldUid);
        if (getMyCurrentWorldHeroes().isEmpty()) {
//            getHeroCreatingPane().setVisible(true);
        } else {
//            getHeroesListPane().setVisible(true);
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

    public LocalDateTime getCurrentHeroCreateDate() {
        return playedHeroesService.getCurrentHeroCreateDate();
    }

    public float getCurrentHeroPower() {
        return playedHeroesService.getCurrentHeroPower();
    }

    public int getCurrentHeroMaxHp() {
        return playedHeroesService.getCurrentHeroMaxHealth();
    }

    public HurtLevel getCurrentHeroHurtLevel() {
        return playedHeroesService.getCurrentHeroHurtLevel();
    }

    public int getCurrentHeroHp() {
        return playedHeroesService.getCurrentHeroCurHealth();
    }

    public float getCurrentHeroExperience() {
        return playedHeroesService.getCurrentHeroExperience();
    }

    public int getCurrentHeroLevel() {
        return playedHeroesService.getCurrentHeroLevel();
    }

    public HeroType getCurrentHeroType() {
        return playedHeroesService.getCurrentHeroType();
    }

    public String getCurrentHeroName() {
        return playedHeroesService.getCurrentHeroName();
    }

    public World getCurrentWorld() {
        return worldMapper.toEntity(worldService.getCurrentWorld());
    }

    public void setCurrentWorld(UUID selectedWorldUuid) {
        Optional<World> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            setLastPlayedWorldUuid(selectedWorldUuid);
            worldService.setCurrentWorld(worldMapper.toDto(selected.get()));
            return;
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid);
    }

    public void setCurrentWorld(WorldDTO newWorld) {
        worldService.setCurrentWorld(newWorld);
    }

    public void saveServerWorldAndSetAsCurrent(World world) {
        Optional<World> wOpt = worldService.findByUid(world.getUid());
        if (wOpt.isPresent()) { // тут происходит подмена worldUuid?
            World old = wOpt.get();
            BeanUtils.copyProperties(world, old);
            worldService.setCurrentWorld(worldMapper.toDto(old));
            worldService.saveCurrentWorld();
        } else {
            worldService.setCurrentWorld(worldService.save(worldMapper.toDto(world)));
        }
    }

    public boolean isCurrentHeroOnline() {
        return playedHeroesService.getCurrentHero() != null && playedHeroesService.isCurrentHeroOnline();
    }

    public boolean registerCurrentHeroOnServer() {
        log.info("Отправка данных текущего героя на Сервер...");
        localSocketConnection.toServer(heroToCli(getCurrentHero(), playerService.getCurrentPlayer()));

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

    private short getCurrentHeroPeriferiaSize() {
        return playedHeroesService.getCurrentHeroPeriferiaSize();
    }

    private HeroPeriferiaType getCurrentHeroPeriferiaType() {
        return playedHeroesService.getCurrentHeroPeriferiaType();
    }

    private HeroCorpusType getCurrentHeroCorpusType() {
        return playedHeroesService.getCurrentHeroCorpusType();
    }

    private Color getCurrentHeroSecondColor() {
        return playedHeroesService.getCurrentHeroSecondColor();
    }

    private Color getCurrentHeroBaseColor() {
        return playedHeroesService.getCurrentHeroBaseColor();
    }

    public HeroDTO getCurrentHero() {
        return playedHeroesService.getCurrentHero();
    }

    public void setCurrentHero(HeroDTO hero) {
        if (playedHeroesService.isCurrentHeroNotNull()) {
            if (playedHeroesService.isCurrentHero(hero)) {
                playedHeroesService.setCurrentHeroOnline(true);
            } else if (playedHeroesService.isCurrentHeroOnline()) {
                // если online другой герой - снимаем:
                log.info("Снимаем он-лайн с Героя {} и передаём этот статус Герою {}...",
                        getCurrentHero().getCharacterName(), hero.getCharacterName());
                playedHeroesService.offlineSaveAndRemoveCurrentHero(null);
            }
        }
        log.info("Теперь активный Герой - {}", hero.getCharacterName());
        playedHeroesService.addCurrentHero(hero);
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
    public void syncServerDataWithCurrentWorld(@NotNull ClientDataDTO data) {
        log.debug("Получены данные для синхронизации {} игрока {} (герой {})", data.dataEvent(),
                data.content().playerUid(), playedHeroesService.getHeroByOwnerUid(data.content().playerUid()));

        HeroDTO aim = playedHeroesService.getHero(data.content().heroUid());
        if (aim == null) {
            log.warn("Герой {} не существует в БД. Отправляется запрос на его модель к Серверу, ожидается...", data.content().heroUid());
            requestHeroFromServer(data.content().heroUid());
            return;
        }

        if (data.dataEvent() == NetDataEvent.HERO_OFFLINE) {
            EventHeroOffline event = (EventHeroOffline) data.content();
            UUID offlinePlayerUid = event.playerUid();
            log.info("Игрок {} отключился от Сервера. Удаляем его из карты активных Героев...", offlinePlayerUid);
            offlineSaveAndRemoveOtherHeroByPlayerUid(offlinePlayerUid);
        }

        if (data.dataEvent() == NetDataEvent.HERO_MOVING) {
            EventHeroMoving event = (EventHeroMoving) data.content();
            aim.setLocation(event.positionX(), event.positionY());
            aim.setVector(event.vector());
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
        playedHeroesService.addHero(aim);
    }

    public boolean isWorldExist(UUID worldUid) {
        return worldService.isWorldExist(worldUid);
    }

    public PlayedHeroesService getPlayedHeroesService() {
        return playedHeroesService;
    }

    public void clearConnectedHeroes() {
        playedHeroesService.clear();
    }

    public boolean isHeroExist(UUID uuid) {
        return heroService.isHeroExist(uuid);
    }

    public HeroDTO getHeroByUid(UUID uuid) {
        return heroService.getByUid(uuid);
    }

    public void offlineSaveAndRemoveOtherHeroByPlayerUid(UUID clientUid) {
        playedHeroesService.offlineSaveAndRemoveOtherHeroByPlayerUid(clientUid);
    }

    public void requestHeroFromServer(UUID uuid) {
        localSocketConnection.toServer(ClientDataDTO.builder()
                .dataType(NetDataType.HERO_REMOTE_NEED)
                .content(EventHeroRegister.builder().heroUid(uuid).build())
                .build());
        setRemoteHeroRequestSent(true);
    }

    public ClientDataDTO heroToCli(HeroDTO found, PlayerDTO currentPlayer) {
        return heroService.heroToCli(found, currentPlayer);
    }

    public HeroDTO cliToHero(ClientDataDTO readed) {
        return heroService.cliToHero(readed);
    }

    public PlayerDTO getCurrentPlayer() {
        return playerService.getCurrentPlayer();
    }

    public void setRemoteHeroRequestSent(boolean b) {
        this.isRemoteHeroRequestSent = b;
    }

    public double getCurrentHeroCorpusHeight() {
        return playedHeroesService.getCurrentHeroCorpusHeight();
    }

    public WorldDTO getAnyWorld() {
        return worldService.findAnyWorld();
    }

    public void justSave() {
        justSaveOnlineHero(Constants.getDuration());
        saveCurrentWorld();
    }

    /**
     * Приходим сюда для создания нового героя для мира.
     *
     * @param newHeroTemplate модель нового героя для игры в новом мире.
     */
    public void saveNewHeroAndPlay(HeroCreatingPane newHeroTemplate) {
        // сохраняем нового героя и проставляем как текущего:
        HeroDTO aNewToSave = new HeroDTO();

        aNewToSave.setBaseColor(newHeroTemplate.getBaseColor());
        aNewToSave.setSecondColor(newHeroTemplate.getSecondColor());

        aNewToSave.setCorpusType(newHeroTemplate.getChosenCorpusType());
        aNewToSave.setPeriferiaType(newHeroTemplate.getChosenPeriferiaType());
        aNewToSave.setPeriferiaSize(newHeroTemplate.getPeriferiaSize());

        aNewToSave.setWorldUid(newHeroTemplate.getWorldUid());
        aNewToSave.setCharacterUid(UUID.randomUUID());
        aNewToSave.setCharacterName(newHeroTemplate.getHeroName());
        aNewToSave.setOwnerUid(getCurrentPlayerUid());
        aNewToSave.setCreateDate(LocalDateTime.now());

        saveNewHero(aNewToSave, true);

        // если подключение к Серверу уже закрылось пока мы собирались:
        if (isCurrentWorldIsNetwork() && !isServerIsOpen()) {
            log.warn("Сервер уже закрыт. Требуется повторное подключение.");
//            getHeroCreatingPane().setVisible(false);
//            getHeroesListPane().setVisible(false);
//            getNetworkListPane().setVisible(true);
            return;
        }

        playWithThisHero(getCurrentHero());
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(HeroDTO hero) {
        setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());
        setCurrentHero(hero);

        // если этот мир по сети:
        if (isCurrentWorldIsNetwork()) {
            // шлем на Сервер своего выбранного Героя:
            if (registerCurrentHeroOnServer()) {
                getPlayedHeroesService().addHero(getCurrentHero());
//                startGame();
            } else {
                log.error("Сервер не принял нашего Героя: {}", getLocalSocketConnection().getLastExplanation());
                setCurrentHeroOfflineAndSave(null);
//                getHeroCreatingPane().repaint();
//                getHeroesListPane().repaint();
            }
        } else {
            // иначе просто запускаем мир и играем локально:
//            startGame();
        }
    }

    public void setAcceleration(boolean b) {
        this.isAccelerated = b;
    }

    public void setSneak(boolean b) {
        this.isSneaked = b;
        if (sneakThread != null && sneakThread.isAlive()) {
            sneakThread.interrupt();
        }

        if (this.isSneaked) {
            sneakThread = new Thread(() -> {
                while (getHeroHeight() < -4 && !Thread.currentThread().isInterrupted()) {
                    try {
                        setHeroHeight(getHeroHeight() + 0.1f);
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            sneakThread = new Thread(() -> {
                while (getHeroHeight() > -6 && !Thread.currentThread().isInterrupted()) {
                    try {
                        setHeroHeight(getHeroHeight() - 0.1f);
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        sneakThread.start();
    }

    public void setZoom(boolean b) {
        this.isZoomed = b;
    }

    public void setVelocity(float v) {
        this.velocity = v;
    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        deleteWorld(worldUid);
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        deleteHero(heroUid);
    }

    /**
     * Когда создаём локальный, несетевой мир - идём сюда, для его сохранения и указания как текущий мир в контроллере.
     *
     * @param newWorld модель нового мира для сохранения.
     */
    public void saveNewLocalWorldAndCreateHero(WorldDTO newWorld) {
        setCurrentWorld(saveNewWorld(newWorld));
        chooseOrCreateHeroForWorld(getCurrentWorldUid());
    }

    // NETWORK game methods:
    public void serverUp(WorldDTO aNetworkWorld) {
//        getNetworkListPane().repaint(); // костыль для отображения анимации

        // Если игра по сети, но Сервер - мы, и ещё не запускался:
        setCurrentWorld(saveNewWorld(aNetworkWorld));

        // Открываем локальный Сервер:
        if (isCurrentWorldIsLocal() && isCurrentWorldIsNetwork() && !isServerIsOpen()) {
            if (openServer()) {
                log.info("Сервер сетевой игры успешно активирован на {}", getServerAddress());
            } else {
                log.warn("Что-то пошло не так при активации Сервера.");
                new FOptionPane().buildFOptionPane("Server error:", "Что-то пошло не так при активации Сервера.", 60, true);
                return;
            }
        }

        if (isSocketIsOpen()) {
            log.error("Socket should was closed here! Closing...");
            closeSocket();
        }

        // Подключаемся к локальному Серверу как новый Клиент:
        connectToServer(NetConnectTemplate.builder()
                .address(aNetworkWorld.getNetworkAddress())
                .passwordHash(aNetworkWorld.getPasswordHash())
                .worldUid(aNetworkWorld.getUid())
                .build());
    }

    public void openCreatingNewHeroPane(HeroDTO template) {
//        getHeroesListPane().setVisible(false);
//        getHeroCreatingPane().setVisible(true);
        if (template != null) {
//            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
        }
    }
}
