package game.freya;

import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.Player;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.gui.GameFrame;
import game.freya.mappers.PlayerMapper;
import game.freya.mappers.WorldMapper;
import game.freya.net.SocketService;
import game.freya.services.PlayerService;
import game.freya.services.UserConfigService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import javax.annotation.PostConstruct;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController {
    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final SQLiteConnection conn;
    private final UserConfigService userConfigService;
    private final WorldService worldService;
    private final WorldMapper worldMapper;
    private final SocketService socketService;
    private final GameFrame gameFrame;

    @Getter
    private final GameConfig gameConfig;

    @Getter
    private PlayerDTO currentPlayer;

    @Getter
    private HeroDTO currentHero;

    @Getter
    @Setter
    private UUID lastPlayedWorldUuid;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
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
        this.lastPlayedWorldUuid = currentPlayer.getLastPlayedWorld();

        // если мир был удалён:
        if (this.lastPlayedWorldUuid != null && !worldService.existsByUuid(this.lastPlayedWorldUuid)) {
            this.lastPlayedWorldUuid = null;
            currentPlayer.setLastPlayedWorld(null);
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

    public void updateCurrentPlayer(Duration duration) {
        currentPlayer.setInGameTime(duration.toMillis());
        playerService.updatePlayer(currentPlayer);
    }

    public void updateWorld(WorldDTO worldDTO) {
        worldService.save(worldDTO);
    }

//    public void resetCurrentPlayer() {
//        log.info("Удаление старого персонажа, создание нового...");
//        playerService.delete(currentPlayer);
//        this.currentPlayer = playerMapper.toDto(playerService.createPlayer());
//    }

    public WorldDTO getLastPlayedWorld() {
        if (lastPlayedWorldUuid == null) {
            return null;
        } else if (worldService.existsByUuid(lastPlayedWorldUuid) && worldService.findByUid(lastPlayedWorldUuid).isPresent()) {
            return worldMapper.toDto(worldService.findByUid(lastPlayedWorldUuid).get());
        }
        return null;
    }

    public void createTheWorldIfAbsent() {
        if (lastPlayedWorldUuid == null || !worldService.existsByUuid(lastPlayedWorldUuid)) {
            // create a new world:
            WorldDTO createdWorld = worldService.createDefaultWorld(this.currentPlayer);
            this.lastPlayedWorldUuid = createdWorld.getUid();
        }
    }

    /*
        if (worldService.count() > 0) {
            List<WorldDTO> worldDtos = worldService.findAll();
            // выводить список доступных миров:
            log.debug("Not realized");
        } else {
            // создаём первый, дефолтный мир:
            worldDto = worldService.save(new WorldDTO("Demo world"));
            worldDto = worldService.addPlayerToWorld(worldDto, gameController.getCurrentPlayer());
        }
     */
}
