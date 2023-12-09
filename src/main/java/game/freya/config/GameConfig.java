package game.freya.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public class GameConfig {
    @Value("${spring.datasource.url}")
    private String connectionUrl;

    @Value("${spring.application.version}")
    private String appVersion;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.company}")
    private String appCompany;
}
