package game.freya;

import game.freya.config.GameConfig;
import game.freya.gui.MainMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameStarter {
    private final ApplicationContext context;
    private final GameConfig config;
    private SQLiteConnection conn;

    @Autowired
    public void setConn(@Lazy SQLiteConnection conn) {
        this.conn = conn;
    }

    @PostConstruct
    public void init() throws IOException {
        Path dataBasePath = Path.of(config.getDatabaseRootDir());
        if (Files.notExists(dataBasePath)) {
            Files.createDirectory(dataBasePath);
        }

        new MainMenu(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration(), context.getBean(GameConfig.class), this);
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
}
