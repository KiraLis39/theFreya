package game.freya;

import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
//@EnableConfigurationProperties({GameConfig.class})
public class Launcher {
    public static void main(String[] args) {
        log.info("Setting up the system properties...");
//        System.setProperty("sun.java2d.opengl", "True");

        // System.setProperty("sun.java2d.d3d", "false");
        // System.setProperty("sun.java2d.d3dtexbpp", "16");

        // System.setProperty("sun.java2d.ddoffscreen", "false");
        // System.setProperty("sun.java2d.ddforcevram", "false");

        // System.setProperty("sun.java2d.accthreshold", "0");
        // System.setProperty("sun.java2d.translaccel", "true");
        // System.setProperty("sun.java2d.xrender", "true");
        // System.setProperty("sun.java2d.noddraw", "true");

        // System.setProperty("sun.awt.noerasebackground", "true");

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));

        globalPreInitialization();

        SpringApplication app = new SpringApplication(Launcher.class);
        app.setHeadless(false);

        logApplicationStartup(app.run(args).getEnvironment());
    }

    private static void globalPreInitialization() {
        try {
            if (Files.notExists(Path.of("./db/"))) {
                Files.createDirectory(Path.of("./db/"));
            }
        } catch (IOException e) {
            log.error("Init database creation error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store"))
                .map(key -> "https").orElse("http");
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
