package game.freya;

import game.freya.config.Constants;
import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.gui.GameFrame;
import game.freya.items.containers.Backpack;
import game.freya.mappers.PlayerMapper;
import game.freya.net.SocketService;
import game.freya.services.PlayerService;
import game.freya.services.UserConfigService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController {
    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final SQLiteConnection conn;
    private final UserConfigService userConfigService;
    private final WorldService worldService;
    private final SocketService socketService;
    private final GameFrame gameFrame;

    @Getter
    private PlayerDTO currentPlayer;

    @PostConstruct
    public void init() throws IOException {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        userConfigService.load(Path.of(Constants.getUserSave()));

        log.info("Check the current user in DB created...");
        checkCurrentUserExists();

        log.info("The game is started!");
        this.gameFrame.showMainMenu(this);
    }

    private void checkCurrentUserExists() {
        Optional<Player> curPlayer = playerService.findByUid(Constants.getUserConfig().getUserId());
        if (curPlayer.isEmpty()) {
            curPlayer = playerService.findByMail(Constants.getUserConfig().getUserMail());
        }
        if (curPlayer.isEmpty()) {
            log.error("Не был найден в базе данных игрок с uuid {}.", Constants.getUserConfig().getUserId());
            PlayerDTO aNewbie = PlayerDTO.builder()
                    .uid(Constants.getUserConfig().getUserId())
                    .nickName(Constants.getUserConfig().getUserName())
                    .email(Constants.getUserConfig().getUserMail())
//                    .position(new Point2D.Double(this.worldDTO.getDimension().getWidth() * Constants.MAP_CELL_DIM / 2,
//                            this.worldDTO.getDimension().getHeight() * Constants.MAP_CELL_DIM / 2))
                    .position(new Point2D.Double(128, 128))
                    .buffs(List.of())
                    .inventory(new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack")))
                    .build();
            try {
                InputStream ris = getClass().getResourceAsStream(Constants.DEFAULT_AVATAR_URL);
                if (ris != null) {
                    aNewbie.setAvatar(ImageIO.read(ris));
                } else {
                    throw new IOException();
                }
            } catch (IOException e) {
                log.error("Can`t set the avatar to player {} by url '{}'", aNewbie.getNickName(), Constants.DEFAULT_AVATAR_URL);
            }
            curPlayer = Optional.of(playerService.save(playerMapper.toEntity(aNewbie)));
        } else {
            log.warn("Не был найден в базе данных игрок по uuid {}, но он найден по почте {}. Странно, но его uuid будет перезаписан в файле сохранения...",
                    Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
            Constants.getUserConfig().setUserId(curPlayer.get().getUid());
        }
        this.currentPlayer = playerMapper.toDto(curPlayer.get());
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

    public WorldDTO saveWorld(WorldDTO worldDTO) {
        return worldService.save(worldDTO);
    }
}
