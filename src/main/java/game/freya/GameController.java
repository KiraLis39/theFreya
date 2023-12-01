package game.freya;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import game.freya.items.interfaces.iEnvironment;
import game.freya.mappers.WorldMapper;
import game.freya.net.ConnectedServerPlayer;
import game.freya.net.LocalSocketConnection;
import game.freya.net.PlayedHeroesService;
import game.freya.net.Server;
import game.freya.net.data.ClientDataDTO;
import game.freya.services.HeroService;
import game.freya.services.PlayerService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
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
    private final PlayerService playerService;
    private final HeroService heroService;
    private final WorldService worldService;
    private final WorldMapper worldMapper;
    private final GameFrame gameFrame;

    private final PlayedHeroesService playedHeroesService;
    private final Server server;

    @Getter
    private final GameConfig gameConfig;

    @Getter
    private LocalSocketConnection localSocketConnection;

    private Thread pingThread;
    @Getter
    @Setter
    private volatile boolean isGameActive = false;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
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
//        Player found = curPlayer.orElse(null);
//        if (found == null) {
//            found = playerService.createPlayer();
//        }
        playerService.setCurrentPlayer(curPlayer.orElse(playerService.createPlayer()));

        // если сменили никнейм в конфиге:
        playerService.getCurrentPlayer().setNickName(Constants.getUserConfig().getUserName());
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
                localSocketConnection.toServer(buildNewDataPackage());

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

    private ClientDataDTO buildNewDataPackage() {
        // собираем пакет данных для сервера и других игроков:
        return ClientDataDTO.builder()
                .id(UUID.randomUUID())
                .type(NetDataType.SYNC)

                .playerUid(getCurrentPlayerUid())
                .playerName(getCurrentPlayerNickName())
                .worldUid(getCurrentWorldUid())

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
                .positionX(getCurrentHeroPosition().x)
                .positionY(getCurrentHeroPosition().y)
                .experience(getCurrentHeroExperience())
                .level(getCurrentHeroLevel())
                .hurtLevel(getCurrentHeroHurtLevel())
                .buffsJson(getCurrentHeroBuffsJson())
                .inventoryJson(getCurrentHeroInventoryJson())

                .chatMessage(null)

                .isOnline(isCurrentHeroOnline())
                .build();
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

    private short getCurrentHeroMaxOil() {
        return playedHeroesService.getCurrentHeroMaxOil();
    }

    private short getCurrentHeroOil() {
        return playedHeroesService.getCurrentHeroCurOil();
    }

    public void saveNewHero(HeroDTO aNewHeroDto) {
        playedHeroesService.addCurrentHero(heroService.saveHero(aNewHeroDto));
    }

    public void deleteWorld(UUID worldUid) {
        log.warn("Удаление Героев мира {}...", worldUid);
        heroService.findAllByWorldUuid(worldUid).forEach(hero -> heroService.deleteHeroByUuid(hero.getUid()));

        log.warn("Удаление мира {}...", worldUid);
        worldService.delete(worldUid);
    }

    public void setHeroOfflineAndSave(Duration gameDuration) {
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
                Constants.getWorldsImagesDir() + worldService.getCurrentWorld().getUid());
    }

    /**
     * Метод отрисовки всех героев подключенных к миру и авторизованных игроков.
     *
     * @param g2D    хост для отрисовки.
     * @param canvas класс холста.
     */
    public void drawHeroes(Graphics2D g2D, GameCanvas canvas) {
        if (isCurrentHeroOnline()) { // если игра по сети:
            for (HeroDTO hero : getConnectedHeroes()) {
                if (playedHeroesService.isCurrentHero(hero)) {
                    // если это текущий герой:
                    if (!Constants.isPaused()) {
                        moveHeroIfAvailable(canvas); // узкое место!
                    }
                    hero.draw(g2D);
                } else if (canvas.getViewPort().getBounds().contains(hero.getPosition())) {
                    // если чужой герой в пределах видимости:
                    hero.draw(g2D);
                }
            }
        } else { // если не-сетевая игра:
            if (!Constants.isPaused()) {
                moveHeroIfAvailable(canvas); // узкое место!
            }

            if (!playedHeroesService.isCurrentHeroNotNull()) {
                log.info("Потеряли текущего игрока. Что-то случилось? Выходим...");
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Окно игры не смогло получить текущего игрока для отрисовки");
            }
            playedHeroesService.getCurrentHero().draw(g2D);
        }
    }

    public boolean isHeroActive(HeroDTO hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getPosition()) && (!isCurrentHeroOnline() || (isCurrentHeroOnline() && hero.isOnline()));
    }

    private void moveHeroIfAvailable(GameCanvas canvas) {
        if (isPlayerMoving()) {
            Rectangle visibleRect = canvas.getViewPort().getBounds();
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

            // set hero vector:
            playedHeroesService.setCurrentHeroVector(vector);
            if (isPlayerCanGo(visibleRect, vector, canvas)) {
                playedHeroesService.getCurrentHero().move();
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
        Point2D.Double pos = playedHeroesService.getCurrentHeroPosition();

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
        server.untilOpen(30_000);
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

    public byte getCurrentHeroSpeed() {
        if (playedHeroesService.getCurrentHero() != null) {
            return playedHeroesService.getCurrentHeroSpeed();
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
        return heroService.findHeroByNameAndWorld(heroName, worldUid);
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

    public Collection<HeroDTO> getConnectedHeroes() {
        return playedHeroesService.getHeroes();
    }

    public MovingVector getCurrentHeroVector() {
        return playedHeroesService.getCurrentHeroVector();
    }

    public boolean connectToServer(String host, Integer port, int passwordHash) {
        // подключаемся к серверу:
        if (isSocketIsOpen() && localSocketConnection.getActiveHost().equals(host)) {
            // верно ли подобное поведение?
            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", localSocketConnection.getActiveHost());
        } else {
            localSocketConnection.openSocket(host, port, this);
        }

        // ждём пока сокет откроется и будет готов к работе:
        if (!isSocketIsOpen()) {
            long was = System.currentTimeMillis();
            while (!isSocketIsOpen() && System.currentTimeMillis() - was < 9_000) {
                Thread.yield();
            }
            if (!isSocketIsOpen()) {
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED,
                        "No reached socket connection to " + host + (port == null ? "" : ":" + port));
            }
        }

        if (isSocketIsOpen() && !host.equals(getCurrentSocketHost())) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
        }

        // передаём свои данные для авторизации:
        localSocketConnection.toServer(ClientDataDTO.builder()
                .id(UUID.randomUUID())
                .type(NetDataType.AUTH_REQUEST)

                .playerUid(getCurrentPlayerUid())
                .playerName(getCurrentPlayerNickName())
                .passwordHash(passwordHash)

                .isOnline(true)
                .build());

        Thread authThread = new Thread(() -> {
            while (!localSocketConnection.isAuthorized() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });
        authThread.setName("Auth awaits thread");
        authThread.setDaemon(true);
        authThread.start();

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

    public boolean ping(String host, Integer port, UUID requestWorldUid) {
        try {
            // подключаемся к серверу:
            localSocketConnection.openSocket(host, port, this);

            // пингуемся:
            localSocketConnection.toServer(ClientDataDTO.builder()
                    .type(NetDataType.PING)
                    .worldUid(requestWorldUid)
                    .build());

            // ждём пока получим ответ PONG от Сервера:
            pingThread = new Thread(() -> {
                long was = System.currentTimeMillis();
                while (localSocketConnection.isOpen()
                        && !localSocketConnection.isPongReceived()
                        && !pingThread.isInterrupted()
                        && System.currentTimeMillis() - was < 9_000
                ) {
                    Thread.yield();
                }
            });
            pingThread.start();
            pingThread.join();

            // проверяем получен ли ответ:
            if (!localSocketConnection.isPongReceived()) {
                pingThread.interrupt();
                log.warn("Пинг к Серверу {}:{} не прошел (1): {}", host, port, localSocketConnection.getLastExplanation());
                return false;
            } else {
                log.warn("Пинг к Серверу {}:{} прошел успешно!", host, port);
                localSocketConnection.resetPong();
                return true;
            }
        } catch (InterruptedException e) {
            pingThread.interrupt();
            log.warn("Пинг к Серверу {}:{} не прошел (2): {}", host, port, localSocketConnection.getLastExplanation());
            return false;
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            localSocketConnection.killSelf();
            log.warn("Пинг к Серверу {}:{} не прошел (3): {}", host, port, localSocketConnection.getLastExplanation());
            return false;
        } finally {
            localSocketConnection.killSelf();
        }
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

    public short getCurrentHeroMaxHp() {
        return playedHeroesService.getCurrentHeroMaxHealth();
    }

    public HurtLevel getCurrentHeroHurtLevel() {
        return playedHeroesService.getCurrentHeroHurtLevel();
    }

    public short getCurrentHeroHp() {
        return playedHeroesService.getCurrentHeroCurHealth();
    }

    public float getCurrentHeroExperience() {
        return playedHeroesService.getCurrentHeroExperience();
    }

    public short getCurrentHeroLevel() {
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
        localSocketConnection.toServer(ClientDataDTO.builder()
                .type(NetDataType.HERO_REQUEST)
                .playerUid(getCurrentPlayerUid())

                .heroUuid(getCurrentHeroUid())
                .heroName(getCurrentHeroName())
                .heroType(getCurrentHeroType())
                .hurtLevel(getCurrentHeroHurtLevel())
                .hp(getCurrentHeroHp())
                .maxHp(getCurrentHeroMaxHp())
                .oil(getCurrentHeroOil())
                .maxOil(getCurrentHeroMaxOil())
                .power(getCurrentHeroPower())
                .speed(getCurrentHeroSpeed())
                .vector(getCurrentHeroVector())
                .positionX(getCurrentHeroPosition().x)
                .positionY(getCurrentHeroPosition().y)
                .experience(getCurrentHeroExperience())
                .level(getCurrentHeroLevel())
                .createDate(getCurrentHeroCreateDate())
                .buffsJson(getCurrentHeroBuffsJson())
                .inventoryJson(getCurrentHeroInventoryJson())
//                .inGameTime(readed.inGameTime())
                .isOnline(true)
                .build());

        Thread heroCheckThread = new Thread(() -> {
            while (!localSocketConnection.isAccepted() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });
        heroCheckThread.setName("Hero accept await thread");
        heroCheckThread.setDaemon(true);
        heroCheckThread.start();

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

    public HeroDTO getCurrentHero() {
        return playedHeroesService.getCurrentHero();
    }

    public void setCurrentHero(HeroDTO hero) {
        if (playedHeroesService.isCurrentHeroNotNull()) {
            if (playedHeroesService.isCurrentHero(hero)) {
                playedHeroesService.setCurrentHeroOnline(true);
            } else if (playedHeroesService.isCurrentHeroOnline()) {
                // если online другой герой - снимаем:
                playedHeroesService.offlineSaveAndRemoveCurrentHero(null);
            }
        }
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
        log.info("Получены данные для синхронизации мира {} игрока {}'s (герой {})", data.worldUid(), data.playerName(), data.heroName());

        HeroDTO aim = playedHeroesService.getHero(data);
        if (aim == null) {
            throw new GlobalServiceException(ErrorMessages.HERO_NOT_FOUND, data.heroUuid());
        }

        // Обновляем позицию другого игрока:
        aim.setPosition(new Point2D.Double(data.positionX(), data.positionY()));
        aim.setVector(data.vector());

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

    public void exitToMenu(Duration gameDuration) {
        // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
        if (isGameActive) {
            isGameActive = false;

            saveCurrentWorld();

            if (gameDuration != null && playedHeroesService.isCurrentHeroNotNull()) {
                setHeroOfflineAndSave(gameDuration);
            }

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
}
