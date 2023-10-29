package game.freya.config;

import fox.FoxFontBuilder;
import lombok.Getter;

public final class Constants {

    @Getter
    private static final FoxFontBuilder FFB = new FoxFontBuilder();
    @Getter
    private static boolean isPaused = false;
    @Getter
    private static int screenDiscreteValue = 75;
    @Getter
    private static boolean isUseSmoothing = false;
    @Getter
    private static boolean isUseBicubic = false;

    private Constants() {
    }
}
