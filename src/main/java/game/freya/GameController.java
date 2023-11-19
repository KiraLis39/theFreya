package game.freya;

import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.MovingVector;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameFrame;
import game.freya.gui.panes.GameCanvas;
import game.freya.mappers.HeroMapper;
import game.freya.mappers.PlayerMapper;
import game.freya.mappers.WorldMapper;
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
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController {
    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
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

    public void loadScreen(ScreenType screenType, WorldDTO worldDTO) {
        log.info("Try to load screen {}...", screenType);
        switch (screenType) {
            case MENU_SCREEN -> gameFrame.loadMenuScreen();
            case GAME_SCREEN -> gameFrame.loadGameScreen(worldDTO);
            default -> log.error("Unknown screen failed to load: {}", screenType);
        }
    }

    public WorldDTO updateWorld(WorldDTO worldDTO) {
        return worldService.save(worldDTO);
    }

    public List<WorldDTO> getExistingWorlds() {
        return worldService.findAll();
    }

    public HeroDTO saveNewHero(HeroDTO aNewHeroDto) {
        return heroMapper.toDto(heroService.save(heroMapper.toEntity(aNewHeroDto)));
    }

    public void deleteWorld(UUID worldUid) {
        worldService.delete(worldUid);
    }

    public void setCurrentHeroOnline(boolean isOnline, Duration duration) {
        heroService.getCurrentHero().setOnline(isOnline);
        heroService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis());
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
            if (heroService.isCurrentHero(hero)) {
                // если это мой текущий герой:
                if (!Constants.isPaused()) {
                    moveHeroIfAvailable(heroService.getCurrentHero(), visibleRect, canvas);
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

    private void moveHeroIfAvailable(HeroDTO currentHeroDto, Rectangle visibleRect, GameCanvas canvas) {
        Point2D.Double plPos = currentHeroDto.getPosition();
        boolean isViewMovableX = plPos.x > visibleRect.getWidth() / 2d
                && plPos.x < worldService.getCurrentWorld().getGameMap().getWidth() - (visibleRect.width - visibleRect.x) / 2d;
        boolean isViewMovableY = plPos.y > visibleRect.getHeight() / 2d
                && plPos.y < worldService.getCurrentWorld().getGameMap().getHeight() - (visibleRect.height - visibleRect.y) / 2d;

        if (isPlayerMovingUp()) {
            if (isPlayerCanGo(visibleRect, MovingVector.UP, canvas)) {
                currentHeroDto.setVector(MovingVector.UP);
                currentHeroDto.move();
            }

            if (isViewMovableY) {
                canvas.dragDown((double) currentHeroDto.getSpeed());
            }
        } else if (isPlayerMovingDown()) {
            if (isPlayerCanGo(visibleRect, MovingVector.DOWN, canvas)) {
                currentHeroDto.setVector(MovingVector.DOWN);
                currentHeroDto.move();
            }

            if (isViewMovableY) {
                canvas.dragUp((double) currentHeroDto.getSpeed());
            }
        }

        if (isPlayerMovingRight()) {
            if (isPlayerCanGo(visibleRect, MovingVector.RIGHT, canvas)) {
                currentHeroDto.setVector(MovingVector.RIGHT);
                currentHeroDto.move();
            }

            if (isViewMovableX) {
                canvas.dragLeft((double) currentHeroDto.getSpeed());
            }
        } else if (isPlayerMovingLeft()) {
            if (isPlayerCanGo(visibleRect, MovingVector.LEFT, canvas)) {
                currentHeroDto.setVector(MovingVector.LEFT);
                currentHeroDto.move();
            }

            if (isViewMovableX) {
                canvas.dragRight((double) currentHeroDto.getSpeed());
            }
        }
    }

    private boolean isPlayerCanGo(Rectangle visibleRect, MovingVector vector, GameCanvas canvas) {
        Point2D.Double pos = heroService.getCurrentHero().getPosition();
        if (!visibleRect.contains(pos)) {
            canvas.moveViewToPlayer(0, 0);
        }
        return switch (vector) {
            case UP -> pos.y > 0;
            case DOWN -> pos.y < worldService.getCurrentWorld().getGameMap().getHeight();
            case LEFT -> pos.x > 0;
            case RIGHT -> pos.x < worldService.getCurrentWorld().getGameMap().getWidth();
            case NONE -> false;
        };
    }

    public void deleteHero(UUID heroUid) {
        heroService.deleteHeroByUuid(heroUid);
    }

    public void setCurrentPlayerLastPlayedWorldUid(UUID uid) {
        playerService.getCurrentPlayer().setLastPlayedWorldUid(uid);
    }

    public HeroDTO getCurrentHero() {
        return heroService.getCurrentHero();
    }

    public void setCurrentHero(HeroDTO hero) {
        // если был активен другой герой - снимаем с него метку онлайн:
        Optional<HeroDTO> onLineHeroOpt = heroService.getCurrentHeroes().stream().filter(HeroDTO::isOnline).findAny();
        onLineHeroOpt.ifPresent(heroDTO -> heroDTO.setOnline(false));

        // ставим метку онлайн на нового героя:
        hero.setOnline(true);
        heroService.getCurrentHeroes().add(hero);
    }

    public BufferedImage getCurrentPlayerAvatar() {
        return playerService.getCurrentPlayer().getAvatar();
    }

    public String getCurrentPlayerNickName() {
        return playerService.getCurrentPlayer().getNickName();
    }

    public WorldDTO saveNewWorld(WorldDTO newWorld) {
        return worldService.save(newWorld);
    }

    public UUID getCurrentPlayerUid() {
        return playerService.getCurrentPlayer().getUid();
    }

    public void setCurrentWorld(WorldDTO newWorld) {
        worldService.setCurrentWorld(newWorld);
    }

    public WorldDTO setCurrentWorld(UUID selectedWorldUuid) {
        Optional<World> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            setLastPlayedWorldUuid(selectedWorldUuid);
            return worldMapper.toDto(selected.get());
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid);
    }

    public void setLastPlayedWorldUuid(UUID lastWorldUuid) {
        playerService.getCurrentPlayer().setLastPlayedWorldUid(lastWorldUuid);
    }

    public UUID getCurrentWorldUid() {
        return worldService.getCurrentWorld().getUid();
    }

    public Set<HeroDTO> getCurrentWorldHeroes() {
        return heroService.getCurrentHeroes();
    }

    public List<HeroDTO> findAllHeroesByWorldUid(UUID uid) {
        return heroService.findAllByWorldUuid(uid);
    }
}
