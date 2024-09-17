package game.freya.utils;

import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@UtilityClass
public final class Screenshoter {

    public BufferedImage doScreenshot(Rectangle bounds) {
        BufferedImage capture = null;
        try {
            Robot robot = new Robot(FoxVideoMonitorUtil.getDevice());
            capture = robot.createScreenCapture(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
        } catch (AWTException e) {
            log.error("Ошибка сриншотера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

//        Out.Print("\nДанная программа использует " +
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 +
//                "мб из " + Runtime.getRuntime().totalMemory() / 1048576 +
//                "мб выделенных под неё. \nСпасибо за использование утилиты компании MultyVerse39 Group!");

        return capture;
    }

    public void doScreenshot(Rectangle bounds, String fileToWrite) {
        try {
            BufferedImage toWrite = doScreenshot(bounds);
            Path aimFile = Path.of(fileToWrite + Constants.getImageExtension());
            if (!ImageIO.write(toWrite, Constants.getImageExtension().replace(".", ""), aimFile.toFile())) {
                log.warn("Проблема при сохранении миниатюры мира: {}", "no appropriate writer is found");
            }
        } catch (IOException e) {
            log.error("Ошибка сриншотера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }
}
