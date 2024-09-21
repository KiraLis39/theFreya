package game.freya;

import fox.utils.FoxSystemInfoUtil;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
//@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class Launcher {

    public static void main(String[] args) {
        if (!FoxSystemInfoUtil.OS.osName.startsWith("Windows")) {
            throw new GlobalServiceException(ErrorMessages.OS_NOT_SUPPORTED, SystemUtils.OS_NAME);
        }

        globalPreInitialization();

        SpringApplication app = new SpringApplication(Launcher.class);
        app.setHeadless(true);

        logApplicationStartup(app.run(args).getEnvironment());
    }

    private static void globalPreInitialization() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));

        try {
            Path dataBasePath = Constants.getDatabase();
            if (Files.notExists(dataBasePath.getParent())) {
                Files.createDirectory(dataBasePath.getParent());
            }
        } catch (IOException e) {
            log.error("Init error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store"))
                .map(_ -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank).orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
                """

                            ----------------------------------------------------------
                            \tApplication '{} v.{}' is running! Access URLs:
                            \tLocal: \t\t{}://localhost:{}{}
                            \tExternal: \t{}://{}:{}{}
                            \tSwagger: \t{}://localhost:{}{}{}
                            \tProfile(s): {}
                            ----------------------------------------------------------
                        """,
                env.getProperty("spring.application.name"), env.getProperty("spring.application.version"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath, "/swagger-ui/index.html",
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }
}
