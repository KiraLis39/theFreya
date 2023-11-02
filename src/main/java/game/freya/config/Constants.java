package game.freya.config;

import fox.FoxFontBuilder;
import fox.FoxFontBuilder.FONT;
import fox.FoxRender;
import fox.VideoMonitor;
import fox.images.FoxCursor;
import fox.images.FoxSpritesCombiner;
import fox.player.FoxPlayer;
import fox.utils.InputAction;
import fox.utils.MediaCache;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public final class Constants {

    public static final int MAP_CELL_DIM = 64;

    public static final boolean PAUSE_ON_HIDDEN = true;

    public static final VideoMonitor MON = new VideoMonitor();

    public static final FoxFontBuilder FFB = new FoxFontBuilder();

    public static final FoxRender RENDER = new FoxRender();

    public static final MediaCache CACHE = MediaCache.getInstance();

    public static final InputAction INPUT_ACTION = new InputAction();

    public static final Font DEBUG_FONT = FFB.setFoxFont(FONT.ARIAL, 16, true, MON.getEnvironment());
    public static final Font INFO_FONT = FFB.setFoxFont(FONT.ARIAL_NARROW, 16, false, MON.getEnvironment());
    public static final Font MENU_BUTTONS_FONT = FFB.setFoxFont(FONT.CANDARA, 24, true, MON.getEnvironment());

    public static final FoxSpritesCombiner SPRITES_COMBINER = new FoxSpritesCombiner();
    public static final String IMAGE_EXTENSION = ""; // .png
    public static final String AUDIO_EXTENSION = ".wav"; // .ogg | .mp3
    public static final Cursor DEFAULT_CUR;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    @Getter
    private static final FoxPlayer soundPlayer = new FoxPlayer("soundPlayer");
    @Getter
    private static final FoxPlayer musicPlayer = new FoxPlayer("musicPlayer");
    private static final ImageIcon defaultIcon;
    @Getter
    private static final String userSave = "./saves/".concat(SystemUtils.getUserName()).concat("/save.json");
    @Getter
    private static final String logoImageUrl = "./resources/images/logo.png";
    @Getter
    private static final String gameName = "Freya the game";
    @Getter
    private static final String gameVersion = "0.0.1";
    @Getter
    private static final String gameAuthor = "KiraLis39";
    @Getter
    private static boolean isDebugInfoVisible = true;
    @Getter
    @Setter
    private static UserConfig userConfig;
    @Getter
    @Setter
    private static boolean isPaused = false;
    @Getter
    private static long screenDiscreteLimit = 30L;
    @Getter
    @Setter
    private static int realFreshRate = 0;
    @Getter
    private static boolean isUseSmoothing = true;
    @Getter
    private static boolean isUseBicubic = false;
    @Getter
    private static long delay = -1;
    @Getter
    private static long screenDiscreteLimitMem = screenDiscreteLimit;

    static {
        try {
            InputStream is = Constants.class.getResourceAsStream("/cursors/default.png");
            defaultIcon = new ImageIcon(ImageIO.read(is));
            DEFAULT_CUR = FoxCursor.createCursor(defaultIcon, "default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Constants() {
    }

    public static GraphicsConfiguration getGraphicsConfiguration() {
        return MON.getConfiguration();
    }

    public static boolean isFrameLimited() {
        return getScreenDiscreteLimit() > 0;
    }

    public static long getDiscreteDelay() {
        if (delay < 0 || screenDiscreteLimitMem != screenDiscreteLimit) {
            screenDiscreteLimitMem = screenDiscreteLimit;
            delay = Math.floorDiv(1000L, screenDiscreteLimit) - 2L;
        }
        return delay;
    }
}
