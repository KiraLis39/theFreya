package game.freya.services;

import fox.FoxLogo;
import game.freya.WorldEngine;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.EnvironmentDto;
import game.freya.dto.roots.WorldDto;
import game.freya.entities.PlayCharacter;
import game.freya.entities.roots.prototypes.Character;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.enums.player.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameWindowController;
import game.freya.gui.panes.GamePaneRunnable;
import game.freya.interfaces.subroot.iEnvironment;
import game.freya.net.PingService;
import game.freya.net.Server;
import game.freya.net.SocketConnection;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroMoving;
import game.freya.net.data.events.EventHeroOffline;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.net.data.events.EventPlayerAuth;
import game.freya.utils.BcryptUtil;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class GameControllerService extends GameControllerBase {
    private final ApplicationProperties applicationProperties;
    private final BcryptUtil bcryptUtil;
    private final GameConfigService gameConfigService;
    private final WorldEngine worldEngine;
    private final EventService eventService;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final WorldService worldService;
    private final PingService pingService;

    private Server server;

    private SocketConnection localSocketConnection;

    private GameWindowController gameFrameController;

    @Setter
    private volatile boolean isGameActive = false;

    /**
     * Отсюда начинается выполнение основного кода игры.
     * В этом методе же вызывается метод, отображающий первое игровое окно - меню игры.
     */
    @Autowired
    public void startGameAndFirstUiShow(@Lazy GameWindowController gameFrameController) {
        this.gameFrameController = gameFrameController;

        setLookAndFeel();

        // показываем лого:
        showLogoIfEnabled();

        // продолжаем подготовку к запуску игры пока лого отображается...
        log.info("Check the current user in DB created...");

        // создаём если не было, и обновляем если ему сменили никнейм через конфиг:
        playerService.getCurrentPlayer()
                .setNickName(Constants.getUserConfig().getUserName());

        loadNecessaryResources();

        gameFrameController.showMainMenu(this, characterService);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
//            UIManager.put("nimbusBase", new Color(...));
//            UIManager.put("nimbusBlueGrey", new Color(...));
//            UIManager.put("control", new Color(...));
//            UIManager.put("Button.font", FONT);
//            UIManager.put("Label.font", FONT);
//            UIManager.put("OptionPane.cancelButtonText", "nope");
//            UIManager.put("OptionPane.okButtonText", "yup");
//            UIManager.put("OptionPane.inputDialogTitle", "Введите свой никнейм:");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(ex));
            }
        }
    }

    private void showLogoIfEnabled() {
        if (Constants.getGameConfig().isShowStartLogo()) {
            try (InputStream is = Constants.class.getResourceAsStream(Constants.getLogoImageUrl())) {
                if (is != null) {
                    Constants.setLogo(new FoxLogo());
                    Constants.getLogo().start(applicationProperties.getAppVersion(),
                            Constants.getUserConfig().isFullscreen() ? FoxLogo.IMAGE_STYLE.FILL : FoxLogo.IMAGE_STYLE.DEFAULT,
                            FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is));
                }
            } catch (IOException e) {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/logo.png");
            }
        }
    }

    private void loadNecessaryResources() {
        if (Files.notExists(Path.of(Constants.getGameConfig().getWorldsImagesDir()))) {
            try {
                Files.createDirectories(Path.of(Constants.getGameConfig().getWorldsImagesDir()));
            } catch (IOException e) {
                log.error("Не удалось создать директорию для миниатюр миров. Нет прав на папку игры? Так играть не выйдет.");
                throw new RuntimeException(e);
            }
        }

        try {
            URL necUrl = getClass().getResource("/images/necessary/");
            assert necUrl != null;
            Constants.CACHE.addAllFrom(necUrl);
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void exitTheGame(Duration duration, int errCode) {
        saveTheGame(duration);
        closeConnections();

        log.info("The game is finished with code {}!", errCode);
        System.exit(errCode);
    }

    public void saveTheGame(Duration duration) {
        log.info("Saving the game...");

        // сохраняем героя:
        if (characterService.getCurrentHero() != null) {
            characterService.getCurrentHero().setOnline(false);
            characterService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis()); // or .getSeconds()
            characterService.saveCurrent();
        }

        // сохраняем мир:
        worldService.saveCurrent();

        // сохраняем игрока (и его UserConfig конфиг):
        playerService.saveCurrent();

        // сохранение GameConfig программы:
        gameConfigService.createOrSaveGameConfig();

        log.info("The game is saved.");
    }

    private void closeConnections() {
        // закрываем Сервер:
        if (server != null && server.isOpen()) {
            if (localSocketConnection != null) {
                localSocketConnection.setHandledExit(true);
            }
            server.close();
        }

        // закрываем соединения:
        if (localSocketConnection != null && localSocketConnection.isOpen()) {
            localSocketConnection.setHandledExit(true);
            localSocketConnection.close();
        }
    }

    public void doScreenShot(Point location, Rectangle canvasRect) {
        new Screenshoter().doScreenshot(new Rectangle(
                        location.x + 9 + canvasRect.getBounds().x,
                        location.y + 30 + canvasRect.getBounds().y,
                        canvasRect.getBounds().width, canvasRect.getBounds().height
                ),
                Constants.getGameConfig().getWorldsImagesDir() + worldService.getCurrentWorld().getUid());
    }

    /**
     * Метод отрисовки всех героев подключенных к миру и авторизованных игроков.
     *
     * @param v2D    хост для отрисовки.
     * @param canvas класс холста.
     */
    public void drawHeroes(Graphics2D v2D, GamePaneRunnable canvas) {
        if (worldService.getCurrentWorld().isNetAvailable()) { // если игра по сети:
            for (PlayCharacterDto hero : server.getConnectedHeroes()) {
                if (characterService.getCurrentHero().equals(hero)) {
                    // если это текущий герой:
                    if (!Constants.isPaused()) {
                        moveHeroIfAvailable(canvas); // узкое место!
                    }
                    hero.draw(v2D);
                } else if (canvas.getViewPort().getBounds().contains(hero.getLocation())) {
                    // если чужой герой в пределах видимости:
                    hero.draw(v2D);
                }
            }
        } else { // если не-сетевая игра:
            if (!Constants.isPaused()) {
                moveHeroIfAvailable(canvas); // узкое место!
            }

            if (characterService.getCurrentHero() == null) {
                log.info("Потеряли текущего игрока. Что-то случилось? Выходим...");
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Окно игры не смогло получить текущего игрока для отрисовки");
            }
            characterService.getCurrentHero().draw(v2D);
        }
    }

    private void moveHeroIfAvailable(GamePaneRunnable canvas) {
        if (isPlayerMoving()) {
            Rectangle visibleRect = canvas.getViewPort().getBounds();
            MovingVector vector = characterService.getCurrentHero().getVector();
            Point2D.Double plLocation = getCurrentHeroLocation();

            double hrc = (visibleRect.x + ((visibleRect.getWidth() - visibleRect.x) / 2d));
            boolean isViewMovableX = plLocation.x > hrc - 30 && plLocation.x < hrc + 30;

            double vrc = (visibleRect.y + ((visibleRect.getHeight() - visibleRect.y) / 2d));
            boolean isViewMovableY = plLocation.y > vrc - 30 && plLocation.y < vrc + 30;

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

            // перемещаем камеру к ГГ:
            if (!visibleRect.contains(characterService.getCurrentHero().getLocation())) {
                canvas.moveViewToPlayer(0, 0);
            }

            // move hero:
            int[] collisionMarker = hasCollision();
            characterService.getCurrentHero().setVector(vector.mod(vector, collisionMarker));
            if (!characterService.getCurrentHero().getVector().equals(MovingVector.NONE)) {
                // двигаемся по направлению вектора (взгляда):
                for (int i = 0; i < characterService.getCurrentHero().getSpeed(); i++) {
                    characterService.getCurrentHero().move();
                }
            } else {
                // тогда, стоя на месте, просто указываем направление вектора (взгляда):
                characterService.getCurrentHero().setVector(vector);
            }

            // send moving data to Server:
            localSocketConnection.sendPacket(eventService.buildMove(characterService.getCurrentHero()));

            // move map:
            switch (vector) {
                case UP -> {
                    if (isViewMovableY) {
                        canvas.dragDown(getCurrentHeroSpeed());
                    }
                }
                case UP_RIGHT -> {
                    if (isViewMovableY) {
                        canvas.dragDown(getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        canvas.dragLeft(getCurrentHeroSpeed());
                    }
                }
                case RIGHT -> {
                    if (isViewMovableX) {
                        canvas.dragLeft(getCurrentHeroSpeed());
                    }
                }
                case RIGHT_DOWN -> {
                    if (isViewMovableX) {
                        canvas.dragLeft(getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        canvas.dragUp(getCurrentHeroSpeed());
                    }
                }
                case DOWN -> {
                    if (isViewMovableY) {
                        canvas.dragUp(getCurrentHeroSpeed());
                    }
                }
                case DOWN_LEFT -> {
                    if (isViewMovableY) {
                        canvas.dragUp(getCurrentHeroSpeed());
                    }
                    if (isViewMovableX) {
                        canvas.dragRight(getCurrentHeroSpeed());
                    }
                }
                case LEFT -> {
                    if (isViewMovableX) {
                        canvas.dragRight(getCurrentHeroSpeed());
                    }
                }
                case LEFT_UP -> {
                    if (isViewMovableX) {
                        canvas.dragRight(getCurrentHeroSpeed());
                    }
                    if (isViewMovableY) {
                        canvas.dragDown(getCurrentHeroSpeed());
                    }
                }
                default -> log.info("Обнаружено несанкционированное направление {}", vector);
            }
        }
    }

    private int[] hasCollision() {
        // если сущность - не призрак:
        if (characterService.getCurrentHero().hasCollision()) {
            Rectangle2D.Double heroCollider = characterService.getCurrentHero().getCollider();

            // проверка коллизии с краем мира:
            Area worldMapBorder = new Area(new Rectangle(-12, -12,
                    worldEngine.getGameMap().getWidth() + 24, worldEngine.getGameMap().getHeight() + 24));
            worldMapBorder.subtract(new Area(new Rectangle(0, 0, worldEngine.getGameMap().getWidth(), worldEngine.getGameMap().getHeight())));
            if (worldMapBorder.intersects(heroCollider)) {
                return findVectorCorrection(worldMapBorder, heroCollider);
            }

            // проверка коллизий с объектами:
            for (EnvironmentDto env : worldService.getCurrentWorld().getEnvironments()) {
                if (env.hasCollision() && env.getCollider().intersects(heroCollider)) {
                    return findVectorCorrection(env.getCollider(), heroCollider);
                }
            }
        }

        return new int[]{0, 0};
    }

    private int[] findVectorCorrection(Shape envColl, Rectangle2D.Double heroColl) {
        int[] result; // y, x
        Rectangle heroBounds = heroColl.getBounds();

        final Point upDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroBounds.y);
        final Point upDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroBounds.y);
        final Point downDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroBounds.y + heroBounds.height);
        final Point downDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroBounds.y + heroBounds.height);

        final Point leftDotX01 = new Point(heroBounds.x, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point leftDotX02 = new Point(heroBounds.x, (int) (heroColl.y + heroColl.height * 0.66d));
        final Point rightDotX01 = new Point(heroBounds.x + heroBounds.width, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point rightDotX02 = new Point(heroBounds.x + heroBounds.width, (int) (heroColl.y + heroColl.height * 0.66d));

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

    public WorldDto saveNewWorld(WorldDto newWorld) {
        newWorld.setCreatedBy(playerService.getCurrentPlayer().getUid());
        newWorld.generate();
        return worldService.saveOrUpdate(newWorld);
    }

    public boolean openServer() {
        server = new Server(this);
        server.start();
        server.untilOpen(Constants.getGameConfig().getServerOpenTimeAwait());
        return server.isOpen();
    }

    public boolean closeServer() {
        localSocketConnection.setHandledExit(true);
        server.close();
        server.untilClose(6_000);
        return server.isClosed();
    }

    public Set<iEnvironment> getWorldEnvironments(Rectangle2D.Double rectangle) {
        return worldService.getEnvironmentsFromRectangle(rectangle);
    }

    public Set<PlayCharacterDto> findAllHeroesByWorldUid(UUID uid) {
        return characterService.findAllByWorldUuid(uid);
    }

    public List<PlayCharacterDto> getMyCurrentWorldHeroes() {
        return characterService.findAllByWorldUidAndOwnerUid(worldService.getCurrentWorld().getUid(), playerService.getCurrentPlayer().getUid());
    }

    public String getCurrentWorldTitle() {
        return worldService.getCurrentWorld() == null ? null : worldService.getCurrentWorld().getName();
    }

    public void getDrawCurrentWorld(Graphics2D v2D) throws AWTException {
        worldService.getCurrentWorld().draw(v2D);
    }

    public void initCurrentWorld(GamePaneRunnable gameCanvas) {
        worldService.getCurrentWorld().init(gameCanvas, this);
    }

    public long getCurrentHeroInGameTime() {
        if (characterService.getCurrentHero() != null) {
            return characterService.getCurrentHero().getInGameTime();
        }
        return -1;
    }

    public Point2D.Double getCurrentHeroLocation() {
        if (characterService.getCurrentHero() != null) {
            return characterService.getCurrentHero().getLocation();
        }
        return null;
    }

    public byte getCurrentHeroSpeed() {
        if (characterService.getCurrentHero() != null) {
            return characterService.getCurrentHero().getSpeed();
        }
        return -1;
    }

    public List<WorldDto> findAllWorldsByNetworkAvailable(boolean isNetworkAvailable) {
        return worldService.findAllByNetAvailable(isNetworkAvailable);
    }

    public CharacterDto findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return characterService.findHeroByNameAndWorld(heroName, worldUid);
    }

    public boolean connectToServer(String host, Integer port, String password) {
        this.localSocketConnection = new SocketConnection();

        // подключаемся к серверу:
        if (localSocketConnection.isOpen() && localSocketConnection.getHost().equals(host)) {
            // верно ли подобное поведение?
            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", localSocketConnection.getHost());
        } else {
            localSocketConnection.openSocket(host, port, this, false);
        }

        if (!localSocketConnection.isOpen()) {
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED,
                    "No reached socket connection to " + host + (port == null ? "" : ":" + port));
        } else if (!host.equals(localSocketConnection.getHost())) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
        }

        // передаём свои данные для авторизации:
        localSocketConnection.toServer(ClientDataDto.builder()
                .dataType(NetDataType.AUTH_REQUEST)
                .content(EventPlayerAuth.builder()
                        .ownerUid(playerService.getCurrentPlayer().getUid())
                        .playerName(playerService.getCurrentPlayer().getNickName())
                        .password(password)
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
                localSocketConnection.close();
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            authThread.interrupt();
            localSocketConnection.close();
            return false;
        }
    }

    public void setCurrentWorld(UUID selectedWorldUuid) {
        Optional<WorldDto> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            playerService.getCurrentPlayer().setLastPlayedWorldUid(selectedWorldUuid);
            worldService.setCurrentWorld(selected.get());
            return;
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid.toString());
    }

    public void saveServerWorldAndSetAsCurrent(WorldDto worldDto) {
        Optional<WorldDto> wOpt = worldService.findByUid(worldDto.getUid());
        if (wOpt.isPresent()) { // тут происходит подмена worldUuid?
            WorldDto old = wOpt.get();
            BeanUtils.copyProperties(worldDto, old);
            worldService.setCurrentWorld(old);
            worldService.saveCurrent();
        } else {
            worldService.setCurrentWorld(worldService.saveOrUpdate(worldDto));
        }
    }

    public void setCurrentHero(PlayCharacterDto hero) {
        if (characterService.getCurrentHero() != null) {
            if (characterService.getCurrentHero().equals(hero)) {
                characterService.getCurrentHero().setOnline(true);
            } else if (characterService.getCurrentHero().isOnline()) {
                // если online другой герой - снимаем:
                log.info("Снимаем он-лайн с Героя {} и передаём этот статус Герою {}...", characterService.getCurrentHero().getName(), hero.getName());
                characterService.getCurrentHero().setOnline(false);
                characterService.saveCurrent();
            }
        }
        log.info("Теперь активный Герой - {}", hero.getName());
        characterService.setCurrentHero(hero);
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

        Optional<PlayCharacter> aimOpt = characterService.findByUid(data.content().heroUid());
        if (aimOpt.isEmpty()) {
            log.warn("Герой {} не существует в БД. Отправляется запрос на его модель к Серверу, ожидается...", data.content().heroUid());
            requestHeroFromServer(data.content().heroUid());
            return;
        }
        Character aim = aimOpt.get();

        if (data.dataEvent() == NetDataEvent.HERO_OFFLINE) {
            EventHeroOffline event = (EventHeroOffline) data.content();
            UUID offlinePlayerUid = event.ownerUid();
            log.info("Игрок {} отключился от Сервера. Удаляем его из карты активных Героев...", offlinePlayerUid);
            offlineSaveAndRemoveOtherHeroByPlayerUid(offlinePlayerUid);
        }

        if (data.dataEvent() == NetDataEvent.HERO_MOVING) {
            EventHeroMoving event = (EventHeroMoving) data.content();
            aim.setLocation(event.location());
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
    }

    public void offlineSaveAndRemoveOtherHeroByPlayerUid(UUID clientUid) {
        Optional<CharacterDto> charDtoOpt = characterService.getByUid(clientUid);
        if (charDtoOpt.isPresent()) {
            PlayCharacterDto charDto = (PlayCharacterDto) charDtoOpt.get();
            charDto.setOnline(false);
            characterService.justSaveAnyHero(charDto);
        }
    }

    public void requestHeroFromServer(UUID uid) {
        localSocketConnection.toServer(ClientDataDto.builder()
                .dataType(NetDataType.HERO_REMOTE_NEED)
                .content(EventHeroRegister.builder().heroUid(uid).build())
                .build());
    }
}
