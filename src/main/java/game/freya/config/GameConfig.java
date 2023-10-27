package game.freya.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "game", ignoreUnknownFields = false)
public class GameConfig {
    @Value("${game.gameTitle}")
    private String gameTitle;

    @Value("${game.gameVersion}")
    private String gameVersion;

    @Value("${game.gameOwner}")
    private String gameOwner;
}
