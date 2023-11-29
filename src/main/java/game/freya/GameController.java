package game.freya;

import fox.FoxLogo;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.HeroType;
import game.freya.enums.HurtLevel;
import game.freya.enums.MovingVector;
import game.freya.enums.NetDataType;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameFrame;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.items.interfaces.iEnvironment;
import game.freya.mappers.HeroMapper;
import game.freya.mappers.WorldMapper;
import game.freya.net.ConnectedServerPlayer;
import game.freya.net.LocalSocketConnection;
import game.freya.net.Server;
import game.freya.net.data.ClientDataDTO;
import game.freya.services.HeroService;
import game.freya.services.PlayerService;
import game.freya.services.UserConfigService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController extends GameControllerBase {
    private final PlayerService playerService;
    private final HeroService heroService;
    private final HeroMapper heroMapper;
    private final UserConfigService userConfigService;
    private final WorldService worldService;
    private final WorldMapper worldMapper;
    private final GameFrame gameFrame;
    private final Server server;
    @Getter
    private final GameConfig gameConfig;
    private LocalSocketConnection localSocketConnection;
    private Thread pingThread;
    private volatile boolean isGameActive = false;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        // загружаем конфигурационный файл с настройками игры текущего пользователя из папки с настройками:
        userConfigService.load(Path.of(Constants.getUserSave()));

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
            Constants.CACHE.addIfAbsent("backMenuImage",
                    ImageIO.read(new File("./resources/images/menu.png")));
            Constants.CACHE.addIfAbsent("backMenuImageShadowed",
                    ImageIO.read(new File("./resources/images/menu_shadowed.png")));
            Constants.CACHE.addIfAbsent("green_arrow",
                    ImageIO.read(new File("./resources/images/green_arrow.png")));

            try (InputStream netResource = getClass().getResourceAsStream("/images/net.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("net", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player_0.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player_0", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player_1.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player_1", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player_2.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player_2", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player_3.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player_3", ImageIO.read(netResource));
            }
            try (InputStream netResource = getClass().getResourceAsStream("/images/player_sh.png")) {
                Objects.requireNonNull(netResource);
                Constants.CACHE.addIfAbsent("player_sh", ImageIO.read(netResource));
            }
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        this.localSocketConnection = new LocalSocketConnection();

        log.info("The game is started!");
        this.gameFrame.showMainMenu(this);
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

        // set current world:
        Optional<World> wOpt = worldService.findByUid(playerService.getCurrentPlayer().getLastPlayedWorldUid());
        worldService.setCurrentWorld(worldMapper.toDto(wOpt.orElse(null)));

        // если последний мир был удалён:
        if (worldService.getCurrentWorld() == null && playerService.getCurrentPlayer().getLastPlayedWorldUid() != null) {
            playerService.getCurrentPlayer().setLastPlayedWorldUid(null);
        }

        // если сменили никнейм в конфиге:
        playerService.getCurrentPlayer().setNickName(Constants.getUserConfig().getUserName());
    }

    public void exitTheGame(WorldDTO world) {
        saveTheGame(world);
        closeConnections();

        log.info("The game is finished!");
        System.exit(0);
    }

    private void closeConnections() {
        // закрываем Сервер:
        if (isServerIsOpen()) {
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
        userConfigService.save();
        playerService.updateCurrentPlayer();
        if (world != null) {
            worldService.save(world);
        }
        log.info("The game is saved.");
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
            while (!getNetDataTranslator().isInterrupted() && isSocketIsOpen()) {
                localSocketConnection.toServer(buildNewDataPackage());

                try {
                    Thread.sleep(Constants.SERVER_BROADCAST_DELAY);
                } catch (InterruptedException e) {
                    log.warn("Прерывание потока отправки данных на сервер!");
                    Thread.currentThread().interrupt();
                }
            }
        }));
        getNetDataTranslator().start();
    }

    private ClientDataDTO buildNewDataPackage() {
        // собираем пакет данных для сервера и других игроков:
        return ClientDataDTO.builder()
                .id(UUID.randomUUID())
                .type(NetDataType.SYNC)

                .playerUid(getCurrentPlayerUid())
                .playerName(getCurrentPlayerNickName())

                .heroUuid(getCurrentHeroUid())
                .heroName(getCurrentHeroName())
                .heroType(getCurrentHeroType())
                .hp(getCurrentHeroHp())
                .maxHp(getCurrentHeroMaxHp())
                .oil(getCurrentHeroOil())
                .maxOil(getCurrentHeroMaxOil())
                .power(getCurrentHeroPower())
                .speed(getCurrentHeroSpeed())
                .vector(getCurrentHeroVector())
                .position(getCurrentHeroPosition())
                .experience(getCurrentHeroExperience())
                .level(getCurrentHeroLevel())
                .hurtLevel(getCurrentHeroHurtLevel())

                .chatMessage(null)

                .isOnline(isCurrentHeroOnline())
                .build();
    }

    private short getCurrentHeroMaxOil() {
        return heroService.getCurrentHero().getMaxOil();
    }

    private short getCurrentHeroOil() {
        return heroService.getCurrentHero().getCurOil();
    }

    public HeroDTO saveNewHero(HeroDTO aNewHeroDto) {
        return heroMapper.toDto(heroService.save(heroMapper.toEntity(aNewHeroDto)));
    }

    public void deleteWorld(UUID worldUid) {
        log.warn("Удаление Героев мира {}...", worldUid);
        heroService.findAllByWorldUuid(worldUid).forEach(hero -> heroService.deleteHeroByUuid(hero.getUid()));

        log.warn("Удаление мира {}...", worldUid);
        worldService.delete(worldUid);
    }

    public void setHeroOfflineAndSave(Duration duration) {
        heroService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.offlineHero();
    }

    public void justSaveOnlineHero(Duration duration) {
        heroService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.saveCurrentHero(heroService.getCurrentHero());
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
     * @param g2D         хост для отрисовки.
     * @param visibleRect объектив камеры холста.
     * @param canvas      класс холста.
     */
    public void drawHeroes(Graphics2D g2D, Rectangle visibleRect, GameCanvas canvas) {
        if (isCurrentHeroOnline()) { // если игра по сети:
            for (HeroDTO hero : server.getHeroes()) {
                if (heroService.getCurrentHero() != null && heroService.isCurrentHero(hero)) {
                    // если это текущий герой:
                    if (!Constants.isPaused()) {
                        moveHeroIfAvailable(visibleRect, canvas); // todo: узкое место!
                    }
                    heroService.getCurrentHero().draw(g2D);
                } else if (visibleRect.contains(hero.getPosition())) {
                    // если чужой герой в пределах видимости:
                    hero.draw(g2D);
                }
            }
        } else { // если не-сетевая игра:
            if (!Constants.isPaused()) {
                moveHeroIfAvailable(visibleRect, canvas); // todo: узкое место!
            }
            heroService.getCurrentHero().draw(g2D);
        }
    }

    public boolean isHeroActive(HeroDTO hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getPosition()) && (!isCurrentHeroOnline() || (isCurrentHeroOnline() && hero.isOnline()));
    }

    private void moveHeroIfAvailable(Rectangle visibleRect, GameCanvas canvas) {
        if (isPlayerMoving()) {
            MovingVector vector = heroService.getCurrentHero().getVector();
            Point2D.Double plPos = heroService.getCurrentHero().getPosition();
            boolean isViewMovableX = plPos.x > visibleRect.getWidth() / 2d
                    && plPos.x < worldService.getCurrentWorld().getGameMap().getWidth() - (visibleRect.width - visibleRect.x) / 2d;
            boolean isViewMovableY = plPos.y > visibleRect.getHeight() / 2d
                    && plPos.y < worldService.getCurrentWorld().getGameMap().getHeight() - (visibleRect.height - visibleRect.y) / 2d;

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

            // set hero vector:
            heroService.getCurrentHero().setVector(vector);
            if (isPlayerCanGo(visibleRect, vector, canvas)) {
                heroService.getCurrentHero().move();
            }

            // move map:
            switch (vector) {
                case UP -> {
                    if (isViewMovableY) {
                        canvas.dragDown((double) getCurrentHeroSpeed());
                    }
                }
                case UP_RIGHT -> {
                    if (isViewMovableY) {
                        canvas.dragDown((double) getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        canvas.dragLeft((double) getCurrentHeroSpeed());
                    }
                }
                case RIGHT -> {
                    if (isViewMovableX) {
                        canvas.dragLeft((double) getCurrentHeroSpeed());
                    }
                }
                case RIGHT_DOWN -> {
                    if (isViewMovableX) {
                        canvas.dragLeft((double) getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        canvas.dragUp((double) getCurrentHeroSpeed());
                    }
                }
                case DOWN -> {
                    if (isViewMovableY) {
                        canvas.dragUp((double) getCurrentHeroSpeed());
                    }
                }
                case DOWN_LEFT -> {
                    if (isViewMovableY) {
                        canvas.dragUp((double) getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        canvas.dragRight((double) getCurrentHeroSpeed());
                    }
                }
                case LEFT -> {
                    if (isViewMovableX) {
                        canvas.dragRight((double) getCurrentHeroSpeed());
                    }
                }
                case LEFT_UP -> {
                    if (isViewMovableX) {
                        canvas.dragRight((double) getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        canvas.dragDown((double) getCurrentHeroSpeed());
                    }
                }
                default -> log.info("Обнаружено несанкционированное направление {}", vector);
            }
        }
    }

    private boolean isPlayerCanGo(Rectangle visibleRect, MovingVector vector, GameCanvas canvas) {
        Point2D.Double pos = heroService.getCurrentHero().getPosition();

        // перемещаем камеру к ГГ:
        if (!visibleRect.contains(pos)) {
            canvas.moveViewToPlayer(0, 0);
        }

        VolatileImage gMap = worldService.getCurrentWorld().getGameMap();
        return switch (vector) {
            case UP -> pos.y > 0;
            case UP_RIGHT -> pos.y > 0 && pos.x < gMap.getWidth();
            case RIGHT -> pos.x < gMap.getWidth();
            case RIGHT_DOWN -> pos.x < gMap.getWidth() && pos.y < gMap.getHeight();
            case DOWN -> pos.y < gMap.getHeight();
            case DOWN_LEFT -> pos.y < gMap.getHeight() && pos.x > 0;
            case LEFT -> pos.x > 0;
            case LEFT_UP -> pos.x > 0 && pos.y > 0;
        };
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
        return worldService.save(newWorld);
    }

    public boolean openServer() {
        server.open(this);
        server.untilOpen(6_000);
        return server.isOpen();
    }

    public boolean closeServer() {
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

    public WorldDTO setCurrentWorld(UUID selectedWorldUuid) {
        Optional<World> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            setLastPlayedWorldUuid(selectedWorldUuid);
            return worldService.setCurrentWorld(worldMapper.toDto(selected.get()));
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid);
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

    public List<HeroDTO> getCurrentWorldHeroes() {
        return heroService.findAllByWorldUuid(worldService.getCurrentWorld().getUid());
    }

    public String getCurrentWorldTitle() {
        return worldService.getCurrentWorld().getTitle();
    }

    public VolatileImage getCurrentWorldMap() {
        return worldService.getCurrentWorld().getGameMap();
    }

    public void getDrawCurrentWorld(Graphics2D v2D) {
        worldService.getCurrentWorld().draw(v2D);
    }

    public void saveCurrentWorld() {
        worldService.saveCurrentWorld();
    }

    public void initCurrentWorld(GameCanvas gameCanvas) {
        worldService.getCurrentWorld().init(gameCanvas, this);
    }

    public long getCurrentHeroInGameTime() {
        if (heroService.getCurrentHero() != null) {
            return heroService.getCurrentHero().getInGameTime();
        }
        return -1;
    }

    public UUID getCurrentHeroUid() {
        if (heroService.getCurrentHero() != null) {
            return heroService.getCurrentHero().getUid();
        }
        return null;
    }

    public Point2D.Double getCurrentHeroPosition() {
        if (heroService.getCurrentHero() != null) {
            return heroService.getCurrentHero().getPosition();
        }
        return null;
    }

    public byte getCurrentHeroSpeed() {
        if (heroService.getCurrentHero() != null) {
            return heroService.getCurrentHero().getSpeed();
        }
        return -1;
    }

    public List<WorldDTO> findAllWorldsByNetworkAvailable(boolean isNetworkAvailable) {
        return worldService.findAllByNetAvailable(isNetworkAvailable);
    }

    public boolean isCurrentWorldIsNetwork() {
        return worldService.getCurrentWorld().isNetAvailable();
    }

    public HeroDTO findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return heroMapper.toDto(heroService.findHeroByNameAndWorld(heroName, worldUid).orElse(null));
    }

    public boolean isServerIsOpen() {
        return !server.isClosed();
    }

    public boolean isSocketIsOpen() {
        return localSocketConnection.isOpen();
    }

    public long getConnectedClientsCount() {
        return server.connected();
    }

    public Set<ConnectedServerPlayer> getConnectedPlayers() {
        return server.getPlayers();
    }

    public List<HeroDTO> getConnectedHeroes() {
        return server.getPlayers().stream().map(ConnectedServerPlayer::getHeroDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public MovingVector getCurrentHeroVector() {
        return heroService.getCurrentHero().getVector();
    }

    public boolean connectToServer(String host, Integer port, int passwordHash) {
        // подключаемся к серверу:
        if (isSocketIsOpen() && localSocketConnection.getActiveHost().equals(host)) {
            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", localSocketConnection.getActiveHost());
        } else {
            localSocketConnection.openSocket(host, port, this);
        }

        if (isSocketIsOpen() && !host.equals(getCurrentSocketHost())) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
        }

        // ждём пока сокет откроется и будет готов к работе:
        if (!isSocketIsOpen()) {
            long was = System.currentTimeMillis();
            while (!isSocketIsOpen() && System.currentTimeMillis() - was < 30_000) {
                Thread.yield();
            }
            if (!isSocketIsOpen()) {
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, "No reached this socket connection");
            }
        }

        // передаём свои данные для авторизации:
        localSocketConnection.toServer(ClientDataDTO.builder()
                .id(UUID.randomUUID())
                .type(NetDataType.AUTH_REQUEST)

                .playerUid(getCurrentPlayerUid()) // not null ?
                .playerName(getCurrentPlayerNickName())
                .passwordHash(passwordHash)

                .isOnline(true)
                .build());

        Thread authThread = new Thread(() -> {
            while (!localSocketConnection.isAuthorized() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });
        authThread.start();

        try {
            authThread.join(15_000);
            if (authThread.isAlive()) {
                log.error("Так и не получили успешной авторизации от Сервера.");
                authThread.interrupt();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            authThread.interrupt();
            return false;
        }
    }

    private String getCurrentSocketHost() {
        return localSocketConnection.getActiveHost();
    }

    public boolean ping(String host, Integer port, UUID requestWorldUid) {
        // подключаемся к серверу:
        try {
            localSocketConnection.openSocket(host, port, this);

            // пингуемся:
            localSocketConnection.toServer(ClientDataDTO.builder()
                    .type(NetDataType.PING)
                    .worldUid(requestWorldUid)
                    .build());

            pingThread = new Thread(() -> {
                while (!localSocketConnection.isPongReceived()) {
                    Thread.yield();
                }
                localSocketConnection.resetPong();
            });
            pingThread.start();

            pingThread.join(6_000);
            if (pingThread.isAlive()) {
                pingThread.interrupt();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            pingThread.interrupt();
            return false;
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            localSocketConnection.killSelf();
            return false;
        }
    }

    public LocalDateTime getCurrentHeroCreateDate() {
        return heroService.getCurrentHero().getCreateDate();
    }

    public float getCurrentHeroPower() {
        return heroService.getCurrentHero().getPower();
    }

    public short getCurrentHeroMaxHp() {
        return heroService.getCurrentHero().getMaxHealth();
    }

    public HurtLevel getCurrentHeroHurtLevel() {
        return heroService.getCurrentHero().getHurtLevel();
    }

    public short getCurrentHeroHp() {
        return heroService.getCurrentHero().getCurHealth();
    }

    public float getCurrentHeroExperience() {
        return heroService.getCurrentHero().getExperience();
    }

    public short getCurrentHeroLevel() {
        return heroService.getCurrentHero().getLevel();
    }

    public HeroType getCurrentHeroType() {
        return heroService.getCurrentHero().getType();
    }

    public String getCurrentHeroName() {
        return heroService.getCurrentHero().getHeroName();
    }

    public World getCurrentWorld() {
        return worldMapper.toEntity(worldService.getCurrentWorld());
    }

    public void setCurrentWorld(WorldDTO newWorld) {
        worldService.setCurrentWorld(newWorld);
    }

    public void setCurrentWorld(World world) {
        worldService.setCurrentWorld(worldService.save(worldMapper.toDto(world)));
    }

    public boolean isCurrentHeroOnline() {
        return heroService.getCurrentHero() != null && heroService.getCurrentHero().isOnline();
    }

    public boolean registerCurrentHeroOnServer() {
        log.info("Отправка данных текущего героя на Сервер...");
        localSocketConnection.toServer(ClientDataDTO.builder()
                .type(NetDataType.HERO_REQUEST)
                .playerUid(getCurrentPlayerUid())

                .heroUuid(getCurrentHeroUid())
                .heroName(getCurrentHeroName())
                .heroType(getCurrentHeroType())
                .hurtLevel(getCurrentHeroHurtLevel())
                .hp(getCurrentHeroHp())
                .maxHp(getCurrentHeroMaxHp())
                .power(getCurrentHeroPower())
                .speed(getCurrentHeroSpeed())
                .vector(getCurrentHeroVector())
                .position(getCurrentHeroPosition())
                .experience(getCurrentHeroExperience())
                .level(getCurrentHeroLevel())
                .createDate(getCurrentHeroCreateDate())
//                .buffs(readed.buffs())
//                .inventory(readed.inventory())
//                .inGameTime(readed.inGameTime())

                .isOnline(isCurrentHeroOnline())
                .build());

        Thread heroCheckThread = new Thread(() -> {
            while (!localSocketConnection.isAccepted() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });
        heroCheckThread.start();

        try {
            heroCheckThread.join(9_000);
            if (heroCheckThread.isAlive()) {
                log.error("Так и не получили разрешения на Героя от Сервера.");
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

    public HeroDTO getCurrentHero() {
        return heroService.getCurrentHero();
    }

    public void setCurrentHero(HeroDTO hero) {
        // если был активен другой герой - снимаем с него метку онлайн:
        HeroDTO onLineHero = heroService.getCurrentHero();
        if (onLineHero != null && onLineHero.getUid().equals(hero.getUid()) && onLineHero.isOnline()) {
            onLineHero.setOnline(false);
        }

        // ставим метку онлайн на нового героя:
        hero.setOnline(true);
        heroService.saveCurrentHero(hero);
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
        playerService.getCurrentPlayer().setLastPlayedWorldUid(uid);
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
        log.info("Получены данные для синхронизации мира от {}'s {} ({})", data.playerName(), data.heroName(), data.playerUid());
        // Обновляем позицию другого игрока:
        server.getHero(data.heroUuid()).setPosition(data.position());
        server.getHero(data.heroUuid()).setVector(data.vector());

        // Обновляем здоровье, максимальное здоровье, силу, бафы-дебафы, текущий инструмент в руках и т.п. другого игрока:
        // ...

        // Обновляем окружение, выросшие-срубленные деревья, снесенные, построенные постройки, их характеристики и т.п.:
        // ...

        // Обновляем данные квестов, задач, групп, союзов, обменов и т.п.:
        // ...

        // Обновляем статусы он-лайн, ветхость, таймауты и прочее...
        // ...
    }

    public void exitToMenu(JFrame parentFrame, FoxCanvas canvas, Duration duration) {
        if (parentFrame != null && canvas != null) {
            boolean paused = Constants.isPaused();
            boolean debug = Constants.isDebugInfoVisible();

            Constants.setPaused(false);
            Constants.setDebugInfoVisible(false);
            doScreenShot(parentFrame.getLocation(), canvas.getBounds());
            Constants.setPaused(paused);
            Constants.setDebugInfoVisible(debug);
        }

        if (duration != null) {
            setHeroOfflineAndSave(duration);
        }

        // если локальная игра сетевая - останавливаем сервер при выходе из игры:
        if (isCurrentWorldIsNetwork()) {
            if (isCurrentWorldIsLocal()) {
                if (closeServer()) {
                    log.info("Сервер успешно остановлен");
                } else {
                    log.warn("Возникла ошибка при закрытии сервера.");
                }
            } else if (isSocketIsOpen()) {
                closeSocket();
            }
        }

        saveCurrentWorld();

        if (isGameActive) {
            isGameActive = false;
            loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    public boolean isGameIsActive() {
        return this.isGameActive;
    }

    public void setGameIsActive(boolean isGameActive) {
        this.isGameActive = isGameActive;
    }

    public boolean isWorldExist(UUID worldUid) {
        return worldService.isWorldExist(worldUid);
    }
}
