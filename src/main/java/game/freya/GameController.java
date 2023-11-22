package game.freya;

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
import game.freya.items.interfaces.iEntity;
import game.freya.mappers.HeroMapper;
import game.freya.mappers.WorldMapper;
import game.freya.net.ClientDataDTO;
import game.freya.net.ClientHandler;
import game.freya.net.SocketService;
import game.freya.services.HeroService;
import game.freya.services.PlayerService;
import game.freya.services.UserConfigService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController {
    private final PlayerService playerService;
    private final HeroService heroService;
    private final HeroMapper heroMapper;
    private final SQLiteConnection conn;
    private final UserConfigService userConfigService;
    private final WorldService worldService;
    private final WorldMapper worldMapper;
    private final SocketService socketService;
    private final GameFrame gameFrame;
    @Getter
    private final GameConfig gameConfig;
    private Thread netDataTranslator;
    @Getter
    @Setter
    private boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        if (Files.notExists(Path.of(Constants.getWorldsImagesDir()))) {
            Files.createDirectories(Path.of(Constants.getWorldsImagesDir()));
        }

        userConfigService.load(Path.of(Constants.getUserSave()));

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
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

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

    private void closeConnections() {
        // закрываем Сервер:
        if (socketService.isServerOpen()) {
            socketService.close();
        }

        // закрываем БД:
        try {
            if (conn != null) {
                log.info("Connection to SQLite is closing...");
                conn.close();
                log.info("Connection to SQLite was closed successfully.");
            } else {
                log.warn("Connection is NULL and can`t be closed now.");
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    public void exitTheGame(WorldDTO world) {
        socketService.close();
        saveTheGame(world);
        stopBroadcast();
        closeConnections();

        log.info("The game is finished!");
        System.exit(0);
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

        // network check:
        if (screenType.equals(ScreenType.GAME_SCREEN) && worldService.getCurrentWorld().isNetAvailable()) {
            log.info("Начинается трансляция данных в сеть...");
            if (socketService.isServerOpen()) {
                startServerBroadcast(); // если мы - Сервер.
            } else {
                startClientBroadcast(); // если мы - клиент.
            }
        }
    }

    /**
     * Транслятор сервера.
     * Не работает, когда выступаем в роли клиента?
     */
    private void startServerBroadcast() {
        if (netDataTranslator == null) {
            netDataTranslator = new Thread(() -> {
                while (!netDataTranslator.isInterrupted()) {
                    socketService.broadcast(buildNewDataPackage());

                    try {
                        Thread.sleep(Constants.NETWORK_DATA_TRANSLATE_DELAY);
                    } catch (InterruptedException e) {
                        log.warn("Прерывание потока бродкаста данных клиентам!");
                    }
                }
            });
            netDataTranslator.start();
        } else if (!netDataTranslator.isAlive()) {
            netDataTranslator.start();
        } else {
            log.error("Нельзя повторно запустить ещё живой поток!");
        }
    }

    private void startClientBroadcast() {
        if (netDataTranslator == null) {
            netDataTranslator = new Thread(() -> {
                while (!netDataTranslator.isInterrupted()) {
                    socketService.toServer(buildNewDataPackage());

                    try {
                        Thread.sleep(Constants.NETWORK_DATA_TRANSLATE_DELAY);
                    } catch (InterruptedException e) {
                        log.warn("Прерывание потока отправки данных на сервер!");
                    }
                }
            });
            netDataTranslator.start();
        } else if (!netDataTranslator.isAlive()) {
            netDataTranslator.start();
        } else {
            log.error("Нельзя повторно запустить ещё живой поток!");
        }
    }

    public void stopBroadcast() {
        if (netDataTranslator != null && netDataTranslator.isAlive()) {
            try {
                netDataTranslator.interrupt();
                netDataTranslator.join(1_000);
            } catch (InterruptedException e) {
                netDataTranslator.interrupt();
            }
            log.info("Транслятор данных остановлен: {}", netDataTranslator.isAlive());
        }
    }

    private ClientDataDTO buildNewDataPackage() {
        // собираем пакет данных для сервера и других игроков:
        return ClientDataDTO.builder()
                // ...
                .build();
    }

    public WorldDTO updateWorld(WorldDTO worldDTO) {
        return worldService.save(worldDTO);
    }

    public HeroDTO saveNewHero(HeroDTO aNewHeroDto) {
        return heroMapper.toDto(heroService.save(heroMapper.toEntity(aNewHeroDto)));
    }

    public void deleteWorld(UUID worldUid) {
        worldService.delete(worldUid);
    }

    public void setHeroOfflineAndSave(Duration duration) {
        heroService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.offlineHero();
    }

    public void justSaveOnlineHero(Duration duration) {
        heroService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.saveCurrentHero();
    }

    public void doScreenShot(Point location, Rectangle canvasRect) {
        new Screenshoter().doScreenshot(new Rectangle(
                location.x + 9 + canvasRect.getBounds().x,
                location.y + 30 + canvasRect.getBounds().y,
                canvasRect.getBounds().width, canvasRect.getBounds().height
        ), Constants.getWorldsImagesDir() + worldService.getCurrentWorld().getUid());
    }

    public void drawHeroes(Graphics2D g2D, Rectangle visibleRect, GameCanvas canvas) {
        for (HeroDTO hero : heroService.getCurrentHeroes()) {
            if (heroService.getCurrentHero() != null && heroService.isCurrentHero(hero)) {
                // если это мой текущий герой:
                if (!Constants.isPaused()) {
                    moveHeroIfAvailable(visibleRect, canvas);
                }
                heroService.getCurrentHero().draw(g2D);
            } else if (isHeroActive(hero, visibleRect)) {
                // если чужой герой (on-line и в пределах видимости):
                hero.draw(g2D);
            }
        }
    }

    public boolean isHeroActive(HeroDTO hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getPosition()) && hero.isOnline();
    }

    private void moveHeroIfAvailable(Rectangle visibleRect, GameCanvas canvas) {
        if (isPlayerMoving()) {
            MovingVector vector = heroService.getCurrentHero().getVector();
            Point2D.Double plPos = heroService.getCurrentHero().getPosition();
            boolean isViewMovableX = plPos.x > visibleRect.getWidth() / 2d
                    && plPos.x < worldService.getCurrentWorld().getGameMap().getWidth() - (visibleRect.width - visibleRect.x) / 2d;
            boolean isViewMovableY = plPos.y > visibleRect.getHeight() / 2d
                    && plPos.y < worldService.getCurrentWorld().getGameMap().getHeight() - (visibleRect.height - visibleRect.y) / 2d;

            if (isPlayerMovingUp() && isPlayerCanGo(visibleRect, MovingVector.UP, canvas)) {
                vector = isPlayerMovingRight() ? MovingVector.UP_RIGHT : isPlayerMovingLeft() ? MovingVector.LEFT_UP : MovingVector.UP;
            } else if (isPlayerMovingDown() && isPlayerCanGo(visibleRect, MovingVector.DOWN, canvas)) {
                vector = isPlayerMovingRight() ? MovingVector.RIGHT_DOWN : isPlayerMovingLeft() ? MovingVector.DOWN_LEFT : MovingVector.DOWN;
            }

            if (isPlayerMovingRight() && isPlayerCanGo(visibleRect, MovingVector.RIGHT, canvas)) {
                vector = isPlayerMovingUp() ? MovingVector.UP_RIGHT : isPlayerMovingDown() ? MovingVector.RIGHT_DOWN : MovingVector.RIGHT;
            } else if (isPlayerMovingLeft() && isPlayerCanGo(visibleRect, MovingVector.LEFT, canvas)) {
                vector = isPlayerMovingUp() ? MovingVector.LEFT_UP : isPlayerMovingDown() ? MovingVector.DOWN_LEFT : MovingVector.LEFT;
            }

            // set hero vector:
            heroService.getCurrentHero().setVector(vector);
            heroService.getCurrentHero().move();

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

    private boolean isPlayerMoving() {
        return isPlayerMovingUp || isPlayerMovingDown || isPlayerMovingRight || isPlayerMovingLeft;
    }

    private boolean isPlayerCanGo(Rectangle visibleRect, MovingVector vector, GameCanvas canvas) {
        Point2D.Double pos = heroService.getCurrentHero().getPosition();
        if (!visibleRect.contains(pos)) {
            canvas.moveViewToPlayer(0, 0);
        }
        BufferedImage gMap = worldService.getCurrentWorld().getGameMap();
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

    public void setCurrentPlayerLastPlayedWorldUid(UUID uid) {
        playerService.getCurrentPlayer().setLastPlayedWorldUid(uid);
    }

    public void setCurrentHero(HeroDTO hero) {
        // если был активен другой герой - снимаем с него метку онлайн:
        Optional<HeroDTO> onLineHeroOpt = heroService.getCurrentHeroes().stream().filter(HeroDTO::isOnline).findAny();
        onLineHeroOpt.ifPresent(heroDTO -> heroDTO.setOnline(false));

        // ставим метку онлайн на нового героя:
        hero.setOnline(true);
        if (heroService.getCurrentHeroes().contains(hero)) {
            heroService.getCurrentHeroes().stream()
                    .filter(h -> h.getUid().equals(hero.getUid())).findFirst()
                    .orElseThrow().setOnline(true);
        } else {
            hero.setOnline(true);
            heroService.addToCurrentHeroes(hero);
        }
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

    public boolean openNet() {
        return socketService.openServer(this);
    }

    public boolean closeNet() {
        return socketService.close();
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

    public Set<HeroDTO> getCurrentWorldHeroes() {
        return heroService.getCurrentHeroes();
    }

    public Set<iEntity> getWorldEntities(Rectangle rectangle) {
        return worldService.getEntitiesFromRectangle(rectangle);
    }

    public List<HeroDTO> findAllHeroesByWorldUid(UUID uid) {
        return heroService.findAllByWorldUuid(uid);
    }

    public String getCurrentWorldTitle() {
        return worldService.getCurrentWorld().getTitle();
    }

    public BufferedImage getCurrentWorldMap() {
        return worldService.getCurrentWorld().getGameMap();
    }

    public void getDrawCurrentWorld(Graphics2D g2D) {
        worldService.getCurrentWorld().draw(g2D);
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
        return socketService.isServerOpen();
    }

    public long getConnectedPlayersCount() {
        return socketService.getPlayersCount();
    }

    public Set<ClientHandler> getConnectedPlayers() {
        return socketService.getPlayers();
    }

    public MovingVector getCurrentHeroVector() {
        return heroService.getCurrentHero().getVector();
    }

    public boolean connectToServer(String host, Integer port, String password) throws IOException {
        // подключаемся к серверу:
        ClientHandler connection = socketService.openSocket(host, port, this);

        // передаём свои данные для авторизации:
        connection.push(ClientDataDTO.builder()
                .id(UUID.randomUUID())
                .type(NetDataType.AUTH)
                .playerUid(getCurrentPlayerUid()) // not null ?
                .playerName(getCurrentPlayerNickName())
                .passwordHash(password.isBlank() ? -1 : password.trim().hashCode())
//                .puid(getCurrentHeroUid())
//                .heroName(getCurrentHeroName())
//                .heroType(getCurrentHeroType())
//                .level(getCurrentHeroLevel())
//                .experience(getCurrentHeroExperience())
//                .hp(getCurrentHeroHp())
//                .hurtLevel(getCurrentHeroHurtLevel())
//                .maxHp(getCurrentHeroMaxHp())
//                .position(getCurrentHeroPosition())
//                .vector(getCurrentHeroVector())
//                .speed(getCurrentHeroSpeed())
//                .power(getCurrentHeroPower())
//                .buffs(readed.buffs())
//                .inventory(readed.inventory())
//                .inGameTime(readed.inGameTime())
//                .ownerUid(readed.ownerUid())
//                .worldUid(readed.wUid())
//                .createDate(readed.created())
                .isOnline(true)
                .build());

        Thread authThread = new Thread(() -> {
            while (!connection.isAutorized()) {
                Thread.yield();
            }
        });
        authThread.start();
        try {
            authThread.join(9_000);
        } catch (InterruptedException e) {
            authThread.interrupt();
            return false;
        }

        // если отправка прошла хорошо:
        return true;
    }

    private float getCurrentHeroPower() {
        return heroService.getCurrentHero().getCurrentAttackPower();
    }

    private short getCurrentHeroMaxHp() {
        return heroService.getCurrentHero().getMaxHealth();
    }

    private HurtLevel getCurrentHeroHurtLevel() {
        return heroService.getCurrentHero().getHurtLevel();
    }

    private short getCurrentHeroHp() {
        return heroService.getCurrentHero().getHealth();
    }

    private float getCurrentHeroExperience() {
        return heroService.getCurrentHero().getExperience();
    }

    private short getCurrentHeroLevel() {
        return heroService.getCurrentHero().getLevel();
    }

    private HeroType getCurrentHeroType() {
        return heroService.getCurrentHero().getType();
    }

    private String getCurrentHeroName() {
        return heroService.getCurrentHero().getHeroName();
    }

    public World getCurrentWorld() {
        return worldMapper.toEntity(worldService.getCurrentWorld());
    }

    public void setCurrentWorld(WorldDTO newWorld) {
        worldService.setCurrentWorld(newWorld);
    }

    public WorldDTO setCurrentWorld(World world) {
        return worldService.setCurrentWorld(worldService.save(worldMapper.toDto(world)));
    }
}
