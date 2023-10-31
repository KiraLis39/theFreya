package game.freya.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public class GameConfig {
    @Value("${game.dataBaseRootDir}")
    private String databaseRootDir;

    @Value("${game.doubleBuffered}")
    private boolean isDoubleBuffered;

    @Value("${spring.datasource.url}")
    private String connectionUrl;
}
