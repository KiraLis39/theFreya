package game.freya;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConnection;

import java.sql.SQLException;

@Slf4j
@Getter
@Setter
public class GameControllerBase {
    private SQLiteConnection conn;
    private Thread netDataTranslator;
    private boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;


    public void closeDataBaseConnection() {
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

    public boolean isPlayerMoving() {
        return isPlayerMovingUp || isPlayerMovingDown || isPlayerMovingRight || isPlayerMovingLeft;
    }
}
