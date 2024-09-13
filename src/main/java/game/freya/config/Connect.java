package game.freya.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import game.freya.services.GameConfigService;
import game.freya.utils.ExceptionUtils;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Connect {
    private final GameConfigService gameConfigService;
    private HikariDataSource ds;
    private Connection conn;

    @Bean
    public DataSource dataSource() {
        if (ds == null) {
            buildConn();
        }
        return ds;
    }

    @PostConstruct
    private void buildConn() {
        try {
            if (Constants.getGameConfig() == null) {
                gameConfigService.load();
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Constants.getConnectionUrl());
            config.setUsername(Constants.getConnectionUser());
            config.setPassword(Constants.getConnectionPassword());
            config.addDataSourceProperty("cachePrepStmts", Constants.getGameConfig().isCachePreparedStatements());
            config.addDataSourceProperty("prepStmtCacheSize", Constants.getGameConfig().getPrepStmtCacheSize());
            config.addDataSourceProperty("prepStmtCacheSqlLimit", Constants.getGameConfig().getPrepStmtCacheSqlLimit());
            config.setAutoCommit(Constants.isConnectionAutoCommit());
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            log.error("Database connection fail: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Bean(destroyMethod = "close")
    public Connection getConnection() throws SQLException {
        if (conn == null) {
            buildConn();
            conn = ds.getConnection();
            log.info("Connection to SQLite has been established.");
        }
        return conn;
    }
}
