package game.freya;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Launcher {

    public static void main(String[] args) {
        log.info("The game is started!");
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
