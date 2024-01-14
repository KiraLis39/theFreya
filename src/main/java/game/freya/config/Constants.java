package game.freya.config;

import fox.FoxFontBuilder;
import fox.FoxFontBuilder.FONT;
import fox.FoxLogo;
import fox.FoxRender;
import fox.VideoMonitor;
import fox.components.FOptionPane;
import fox.images.FoxCursor;
import fox.images.FoxSpritesCombiner;
import fox.player.FoxPlayer;
import fox.utils.InputAction;
import fox.utils.MediaCache;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public final class Constants {
    // other:
    public static final int MAX_ZOOM_OUT_CELLS = 23; // максимум отдаление карты ячеек.

    public static final int MIN_ZOOM_OUT_CELLS = 8; // максимум отдаление карты ячеек.

    public static final int MAP_CELL_DIM = 64;

    public static final String DEFAULT_AVATAR_URL = "/images/player/defaultAvatar.png";

    public static final double ONE_TURN_PI = Math.PI / 4d;

    // libraries objects:
    public static final VideoMonitor MON = new VideoMonitor();

    public static final FoxFontBuilder FFB = new FoxFontBuilder();

    public static final FoxRender RENDER = new FoxRender();

    public static final MediaCache CACHE = MediaCache.getInstance();

    public static final InputAction INPUT_ACTION = new InputAction();

    public static final FoxSpritesCombiner SPRITES_COMBINER = new FoxSpritesCombiner();

    // public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.of("ru"));
    public static final DateTimeFormatter DATE_FORMAT_2 = DateTimeFormatter.ofPattern("День dd (HH:mm)", Locale.of("ru"));

    public static final DateTimeFormatter DATE_FORMAT_3 = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.of("ru"));

    // fonts:
    public static final Font DEBUG_FONT = FFB.setFoxFont(FONT.ARIAL, 16, true, MON.getEnvironment());

    public static final Font INFO_FONT = FFB.setFoxFont(FONT.ARIAL_NARROW, 14, false, MON.getEnvironment());

    public static final Font GAME_FONT_01 = FFB.setFoxFont(FONT.ARIAL_NARROW, 12, true, MON.getEnvironment());

    public static final Font GAME_FONT_02 = FFB.setFoxFont(FONT.BAHNSCHRIFT, 26, true, MON.getEnvironment());

    public static final Font GAME_FONT_03 = FFB.setFoxFont(FONT.BAHNSCHRIFT, 32, true, MON.getEnvironment());

    // public static final FoxExperience EXP = new FoxExperience();

    public static final Font MENU_BUTTONS_BIG_FONT;

    public static final Font LITTLE_UNICODE_FONT;

    public static final Font MENU_BUTTONS_FONT;

    public static final Font PROPAGANDA_FONT;

    public static final Font PROPAGANDA_BIG_FONT;

    public static final String FONT_VERTEX_FILE = "./shaders/fontVertex.txt";

    public static final String FONT_FRAGMENT_FILE = "./shaders/fontFragment.txt";

    @Getter
    private static final String appVersion = "0.2.1";

    @Getter
    private static final String appName = "Freya the Game";

    @Getter
    private static final String authorName = "KiraLis39";

    @Getter
    private static final String appCompany = "Multiverse-39 Group, 2023";

    @Getter
    private static final double dragSpeed = 12D;

    @Getter
    private static final double scrollSpeed = 20D;

    @Getter
    private static final double gravity = 9.800000190734863D;

    // project:
    @Getter
    private static final String imageExtension = ".png"; // .png

    @Getter
    private static final String audioExtension = ".ogg"; // .ogg | .mp3 | .wav

    // audio:
    @Getter
    private static final FoxPlayer soundPlayer = new FoxPlayer("soundPlayer");

    @Getter
    private static final FoxPlayer musicPlayer = new FoxPlayer("musicPlayer");

    @Getter
    private static final String userSaveUrl = "./saves/".concat(SystemUtils.getUserName()).concat("/save.json");

    @Getter
    private static final String gameConfigUrl = "./game_config.json";

    @Getter
    private static final String logoImageUrl = "./resources/images/logo.png";

    private static final int FPS_UPDATE_DELAY_SECONDS = 2;

    @Getter
    private static final String gameIconPath = "/images/icons/0.png";

    @Getter
    private static volatile boolean altControlMode = false;

    @Getter
    @Setter
    private static DisplayMode defaultDisplayMode;

    @Getter
    @Setter
    private static FoxLogo logo;

    @Getter
    private static Color mainMenuBackgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.85f);

    @Getter
    private static Color mainMenuBackgroundColor2 = new Color(0.0f, 0.0f, 0.0f, 0.45f);

    @Getter
    @Setter
    private static long gameStartedIn;

    @Getter
    private static String notRealizedString = "Не реализовано ещё";

    @Getter
    private static Cursor defaultCursor;

    // configs:
    @Getter
    @Setter
    private static UserConfig userConfig;

    @Getter
    @Setter
    private static GameConfig gameConfig;

    // dynamic game booleans:
    @Getter
    @Setter
    private static boolean isDebugInfoVisible = false;

    @Getter
    @Setter
    private static boolean isFpsInfoVisible = true;

    @Getter
    private static boolean isPaused = false;

    @Getter
    @Setter
    private static boolean isMinimapShowed = true;

    @Getter
    private static String worldsImagesDir = "./worlds/img/";

    private static long realFreshRate = 0;

    @Getter
    @Setter
    private static Duration duration;

    static {
        try (InputStream is = Constants.class.getResourceAsStream("/cursors/default.png")) {
            if (is != null) {
                BufferedImage curImage = ImageIO.read(is);
                BufferedImage curResult = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D c2D = curResult.createGraphics();
                RENDER.setRender(c2D, FoxRender.RENDER.ULTRA);
                c2D.drawImage(curImage, 0, 0, 64, 64, null);
                c2D.dispose();
                defaultCursor = FoxCursor.createCursor(curResult, "default");
            }
        } catch (IOException e) {
            log.error("Init icon read error: {}", ExceptionUtils.getFullExceptionMessage(e));
            defaultCursor = Cursor.getDefaultCursor();
        }

        int ruf = FFB.addNewFont("Lucida Sans Unicode");
        LITTLE_UNICODE_FONT = FFB.setFoxFont(ruf, 18, false, MON.getEnvironment());
        MENU_BUTTONS_FONT = FFB.setFoxFont(ruf, 20, true, MON.getEnvironment());
        MENU_BUTTONS_BIG_FONT = FFB.setFoxFont(ruf, 28, true, MON.getEnvironment());

        ruf = FFB.addNewFont("Propaganda");
        PROPAGANDA_FONT = FFB.setFoxFont(ruf, 26, true, MON.getEnvironment());
        PROPAGANDA_BIG_FONT = FFB.setFoxFont(ruf, 30, true, MON.getEnvironment());
    }

    private Constants() {
    }

    public static void showNFP() {
        new FOptionPane().buildFOptionPane("Не реализовано:",
                "Приносим свои извинения! Данный функционал ещё находится в разработке.",
                FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
    }

    public static void setPaused(boolean _isPaused) {
        isPaused = _isPaused;
        log.info("Paused: {}", isPaused);
    }

    public static int getMaxConnectionWasteTime() {
        return gameConfig.getSocketConnectionAwaitTimeout() - gameConfig.getSocketPingAwaitTimeout();
    }

    public static boolean isFpsLimited() {
        return userConfig.getFpsLimit() > 0;
    }

    public static long getRealFreshRate() {
        return realFreshRate;
    }

    public static void setRealFreshRate(long fps) {
        realFreshRate = fps;
    }

    public static long getFpsDelaySeconds() {
        return FPS_UPDATE_DELAY_SECONDS;
    }

    public static void setAltControlMode(boolean altMode, long window) {
        altControlMode = altMode;
        if (altControlMode) {
            glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwSetCursor(window, glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR));
        } else {
            glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            glfwSetCursor(window, NULL);
        }
    }

//        Out.Print("\nДанная программа использует " +
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 +
//                "мб из " + Runtime.getRuntime().totalMemory() / 1048576 +
//                "мб выделенных под неё. \nСпасибо за использование утилиты компании MultiVerse39 Group!");
}
