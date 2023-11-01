package game.freya;

import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.gui.GameFrame;
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
    private final GameFrame gameFrame;
    private final SQLiteConnection conn;
    private final UserConfigService userConfigService;
    private final WorldService worldService;

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
}
