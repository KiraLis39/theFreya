package game.freya;

import game.freya.config.GameConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
//@EnableAsync
@AllArgsConstructor
@SpringBootApplication
@EnableConfigurationProperties({GameConfig.class})
public class Launcher {
    private final ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Launcher.class);
//        app.setDefaultProperties(Map.of("spring.profiles.default", "dev"));
        app.setHeadless(false);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        log.info("The game is started!");

        new GameTest().open(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration());
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store"))
                .map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
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

    /*

    При подключенных:

    <!--        <dependency>-->
    <!--            <groupId>org.slf4j</groupId>-->
    <!--            <artifactId>slf4j-api</artifactId>-->
    <!--            <version>2.0.5</version>-->
    <!--        </dependency>-->
    <!--        <dependency>-->
    <!--            <groupId>org.slf4j</groupId>-->
    <!--            <artifactId>slf4j-log4j12</artifactId>-->
    <!--            <version>2.0.5</version>-->
    <!--        </dependency>-->

    private static void initSimpleLogSystem() {
        BasicConfigurator.configure();
    }

    private static void initCustomLogSystem() {
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "TRACE, stdout, MyFile");
        properties.setProperty("log4j.rootCategory", "TRACE");

        properties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        properties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{dd.MM.yy HH:mm:ss.SSS} [%p] %c{1}:%L.%t() %m%n");

        properties.setProperty("log4j.appender.MyFile", "org.apache.log4j.RollingFileAppender");
        properties.setProperty("log4j.appender.MyFile.File", "log/log.log");
        properties.setProperty("log4j.appender.MyFile.MaxFileSize", "1024KB");
        properties.setProperty("log4j.appender.MyFile.MaxBackupIndex", "3");
        properties.setProperty("log4j.appender.MyFile.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.MyFile.layout.ConversionPattern", "%d{dd.MM.yy HH:mm:ss.SSS} %5p %c{1}:%L (%t) %m%n");

        PropertyConfigurator.configure(properties);
    }*/
}
