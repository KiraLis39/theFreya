package game.freya;

import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.dto.WorldDTO;
import game.freya.mappers.WorldMapper;
import game.freya.services.UserConfigService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameController {
    private final GameConfig config;
    private final SQLiteConnection conn;
    private final UserConfigService userConfigService;
    private final WorldService worldService;
    private final WorldMapper worldMapper;

    @PostConstruct
    public void init() throws IOException {
        log.info("The game is started!");

        Path dataBasePath = Path.of(config.getDatabaseRootDir());
        if (Files.notExists(dataBasePath)) {
            Files.createDirectory(dataBasePath);
        }

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        userConfigService.load(Path.of(Constants.getUserSave()));
    }

    public void closeConnections() {
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

    public void exitTheGame() {
        log.info("The game is finished!");
        System.exit(0);
    }

    public void saveTheGame(WorldDTO world) {
        log.info("Saving the game...");
        userConfigService.save();
        worldService.save(worldMapper.toEntity(world));
        log.info("The game is saved.");
    }
}
