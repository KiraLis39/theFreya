package game.freya.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public final class Connect {
    private HikariDataSource ds;
    private Connection conn;

    private Connect() {
    }

    @Bean
    public DataSource dataSource() {
        if (ds == null) {
            buildConn();
        }
        return ds;
    }

    private void buildConn() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Constants.getConnectionUrl());
            config.setUsername(Constants.getConnectionUser());
            config.setPassword(Constants.getConnectionPassword());
            config.addDataSourceProperty("cachePrepStmts", Constants.getCachePrepStmts());
            config.addDataSourceProperty("prepStmtCacheSize", Constants.getPrepStmtCacheSize());
            config.addDataSourceProperty("prepStmtCacheSqlLimit", Constants.getPrepStmtCacheSqlLimit());
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
