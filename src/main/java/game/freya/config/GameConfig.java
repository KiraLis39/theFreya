package game.freya.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "game", ignoreUnknownFields = false)
public class GameConfig {
    @Value("${game.dataBaseRootDir}")
    public String databaseRootDir;
    @Value("${game.gameTitle}")
    private String gameTitle;
    @Value("${game.gameVersion}")
    private String gameVersion;
    @Value("${game.gameOwner}")
    private String gameOwner;
    @Value("${spring.datasource.url}")
    private String connectionUrl;
}
