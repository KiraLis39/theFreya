package game.freya.utils;

import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.glReadPixels;

@Slf4j
public final class Screenshoter {

    public void doScreenshot(Rectangle bounds, String fileToWrite, boolean isGl) {
        try {
            BufferedImage toWrite = isGl ? getScreenshotGLImage(bounds) : getScreenshotImage(bounds);
            if (toWrite == null) {
                throw new GlobalServiceException(ErrorMessages.SCREENSHOT_FAILED, "toWrite is NULL");
            }
            Path aimFile = Path.of(fileToWrite + Constants.getImageExtension());
            if (!ImageIO.write(toWrite, Constants.getImageExtension().replace(".", ""), aimFile.toFile())) {
                log.warn("Проблема при сохранении миниатюры мира: {}", "no appropriate writer is found");
            }
        } catch (IOException e) {
            log.error("Ошибка сриншотера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    private BufferedImage getScreenshotImage(Rectangle bounds) {
        try {
            Robot robot = new Robot(Constants.MON.getDevice());
            return robot.createScreenCapture(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
        } catch (AWTException e) {
            log.error("Ошибка сриншотера: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
        return null;
    }

    private BufferedImage getScreenshotGLImage(Rectangle bounds) {
        FloatBuffer imageData = BufferUtils.createFloatBuffer(bounds.width * bounds.height * 3);
        glReadPixels(0, 0, bounds.width, bounds.height, GL_RGB, GL_FLOAT, imageData);
        imageData.rewind();

        int[] rgbArray = new int[bounds.width * bounds.height];
        for (int y = 0; y < bounds.height; ++y) {
            for (int x = 0; x < bounds.width; ++x) {
                int r = (int) (imageData.get() * 255) << 16;
                int g = (int) (imageData.get() * 255) << 8;
                int b = (int) (imageData.get() * 255);
                int i = ((bounds.height - 1) - y) * bounds.width + x;
                rgbArray[i] = r + g + b;
            }
        }

        BufferedImage image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, bounds.width, bounds.height, rgbArray, 0, bounds.width);
        return image;
    }
}
