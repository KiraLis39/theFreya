package game.freya.config;

import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Connect {
    private final GameConfig config;

    private SQLiteConnection conn;

    @Bean(name = "SQLiteConnection")
    public SQLiteConnection getConnection() {
        if (conn == null) {
            try {
                this.conn = (SQLiteConnection) DriverManager.getConnection(config.getConnectionUrl());
                this.conn.setAutoCommit(false);
                log.info("Connection to SQLite has been established.");
            } catch (SQLException e) {
                log.error("Database connection fail: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }
        return conn;
    }

    @Bean(name = "ConnectionCloser", autowireCandidate = false)
    public void closeConnection() {
        if (conn != null) {
            try {
                this.conn.close();
                log.info("Connection to SQLite was closed.");
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }
}
