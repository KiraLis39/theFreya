package game.freya;

import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameFrame;
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
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
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
    private PlayerDTO currentPlayer;

    @Getter
    private HeroDTO currentHero;

    @Getter
    private WorldDTO currentWorld;

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
        if (curPlayer.isEmpty()) {
            log.error("Не был найден в базе данных игрок с uuid {}. Создание нового...", Constants.getUserConfig().getUserId());
            curPlayer = Optional.of(playerService.createPlayer());
        }

        this.currentPlayer = playerMapper.toDto(curPlayer.get());
        Optional<World> wOpt = worldService.findByUid(currentPlayer.getLastPlayedWorldUid());
        this.currentWorld = worldMapper.toDto(wOpt.orElse(null));

        // если мир был удалён:
        if (this.currentWorld != null && !worldService.existsByUuid(this.currentWorld.getUid())) {
            this.currentWorld = null;
            currentPlayer.setLastPlayedWorldUid(null);
        }

        // если сменили никнейм в конфиге:
        if (!this.currentPlayer.getNickName().equals(Constants.getUserConfig().getUserName())) {
            playerService.updateNickName(this.currentPlayer.getUid(), Constants.getUserConfig().getUserName());
            this.currentPlayer = playerMapper.toDto(playerService.findByUid(this.currentPlayer.getUid()).get());
        }
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
        playerService.updatePlayer(currentPlayer);
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

    public void updateCurrentPlayer() {
        currentPlayer.setCurrentActiveHero(currentHero);
        playerService.updatePlayer(currentPlayer);
    }

    public WorldDTO updateWorld(WorldDTO worldDTO) {
        return worldService.save(worldDTO);
    }

    public List<WorldDTO> getExistingWorlds() {
        return worldService.findAll();
    }

    public WorldDTO saveNewWorld(WorldDTO aNewWorld) {
        aNewWorld.setAuthor(currentPlayer.getUid());
        WorldDTO saved = worldService.save(aNewWorld);
        this.currentWorld = saved;
        return saved;
    }

    public void setCurrentWorld(WorldDTO currentWorld) {
        this.currentWorld = currentWorld;
    }

    public void setLastPlayedWorldUuid(UUID selectedWorldUuid) {
        Optional<World> wOpt = worldService.findByUid(selectedWorldUuid);
        if (wOpt.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid);
        }
        this.currentWorld = worldMapper.toDto(wOpt.get());
    }

    public void deleteCurrentPlayerHero(UUID heroUid) {
        heroService.deleteHeroByUuid(heroUid);
        if (this.currentHero != null && this.currentHero.getUid().equals(heroUid)) {
            this.currentHero = null;
        }
        Optional<Player> plOpt = playerService.findByUid(this.currentPlayer.getUid());
        setCurrentPlayer(playerMapper.toDto(plOpt.get()));
    }

    public void setCurrentHero(HeroDTO hero) {
        this.currentHero = hero;
    }

    public void setCurrentHero(UUID uid) {
        this.currentHero = heroMapper.toDto(heroService.findByUid(uid).orElseThrow());
    }

    public HeroDTO saveNewHero(HeroDTO aNewHeroDto) {
        aNewHeroDto.setWorldUid(currentWorld.getUid());
        aNewHeroDto.setOwnedPlayer(this.currentPlayer);
        return heroMapper.toDto(heroService.save(heroMapper.toEntity(aNewHeroDto)));
    }

    public void deleteWorld(UUID worldUid) {
        worldService.delete(worldUid);
    }

    public void updateCurrentHero(Duration duration) {
        this.currentHero.setInGameTime(duration == null ? 0 : duration.toMillis());
        heroService.save(heroMapper.toEntity(this.currentHero));
    }

    public void doScreenShot(Point location, Rectangle canvasRect) {
        new Screenshoter().doScreenshot(new Rectangle(
                location.x + 9 + canvasRect.getBounds().x,
                location.y + 30 + canvasRect.getBounds().y,
                canvasRect.getBounds().width, canvasRect.getBounds().height
        ), Constants.getWorldsImagesDir() + getCurrentWorld().getUid());
    }
}
