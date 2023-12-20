package game.freya.config;

import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Connect {
    private static final String connectionUrl = "jdbc:sqlite:";

    private SQLiteConnection conn;

    @Bean
    public SQLiteConnection getConnection() {
        if (conn == null) {
            try {
                Path dataBasePath = Path.of(Constants.getGameConfig().getDatabaseRootDirUrl());
                if (Files.notExists(dataBasePath.getParent())) {
                    Files.createDirectory(dataBasePath.getParent());
                }
            } catch (IOException e) {
                log.error("Init database creation error: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            try {
                log.info("Connection to SQLite {}...", connectionUrl + Constants.getGameConfig().getDatabaseRootDirUrl());
                this.conn = (SQLiteConnection) DriverManager.getConnection(connectionUrl + Constants.getGameConfig().getDatabaseRootDirUrl());
                this.conn.setAutoCommit(false);
                log.info("Connection to SQLite has been established.");
            } catch (SQLException e) {
                log.error("Database connection fail: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }
        return conn;
    }

    @Bean(autowireCandidate = false)
    public void closeConnection() {
        if (conn != null) {
            try {
                this.conn.close();
                log.info("Connection to SQLite was closed.");
            } catch (SQLException e) {
                log.error("Исключение при закрытии подключения к базе данных: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }
    }
}
