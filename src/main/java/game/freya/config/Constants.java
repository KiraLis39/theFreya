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

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public final class Constants {
    // net:
    public static final int SERVER_PORT = 13958;
    public static final int SOCKET_BUFFER_SIZE = 10240; // 65536
    public static final long NETWORK_DATA_TRANSLATE_DELAY = 5_000L; // сколько миллисекунд ждать между отправками данных на сервер
    public static final int SERVER_CONNECTION_AWAIT_TIMEOUT = 600_000; // сколько миллисекунд сервер ждёт подключений


    // other:
    public static final int MAX_ZOOM_OUT_CELLS = 23; // максимум отдаление карты ячеек.
    public static final int MIN_ZOOM_OUT_CELLS = 8; // максимум отдаление карты ячеек.
    public static final int MAP_CELL_DIM = 64;
    public static final String DEFAULT_AVATAR_URL = "/images/defaultAvatar.png";
    // libraries objects:
    public static final VideoMonitor MON = new VideoMonitor();
    public static final FoxFontBuilder FFB = new FoxFontBuilder();
    public static final FoxRender RENDER = new FoxRender();
    public static final MediaCache CACHE = MediaCache.getInstance();
    public static final InputAction INPUT_ACTION = new InputAction();
    public static final FoxSpritesCombiner COMBINER = new FoxSpritesCombiner();
    // public static final FoxExperience EXP = new FoxExperience();
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
    public static final Font MENU_BUTTONS_BIG_FONT;
    public static final Font LITTLE_UNICODE_FONT;
    public static final Font MENU_BUTTONS_FONT;
    public static final Font PROPAGANDA_FONT;
    public static final Font PROPAGANDA_BIG_FONT;
    // project:
    @Getter
    private static final String gameAuthor = "KiraLis39";
    @Getter
    private static final Path databaseRootDir = FileSystems.getDefault().getPath("./db/freya.db").toAbsolutePath();
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
    private static final double dragSpeed = 12D;
    @Getter
    private static final double scrollSpeed = 20D;
    @Getter
    private static final String userSave = "./saves/".concat(SystemUtils.getUserName()).concat("/save.json");
    @Getter
    private static final String logoImageUrl = "./resources/images/logo.png";
    private static final int SHIFT_FPS_ALLOWED = 2; // допустимый разброс FPS.
    private static final int MAX_FPS_ALLOWED = 120; // больше этой частоты нет смысла гнать точно.
    private static final AtomicInteger realFreshRate = new AtomicInteger(0);
    private static final AtomicLong delay = new AtomicLong(15L);
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
    // user config:
    @Getter
    @Setter
    private static UserConfig userConfig;
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
    private static boolean isShowStartLogo = true;
    @Getter
    private static volatile int fpsLimit;
    @Getter
    private static String worldsImagesDir = "./worlds/img/";

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

    public static GraphicsConfiguration getGraphicsConfiguration() {
        return MON.getConfiguration();
    }

    public static boolean isFpsLimited() {
        return Constants.getUserConfig().getFpsLimit() > 0;
    }

    public static long getDelay() {
        if (fpsLimit != Constants.getUserConfig().getFpsLimit()) {
            // если значение в настройках изменилось:
            fpsLimit = Constants.getUserConfig().getFpsLimit();
            delay.set(Math.floorDiv(1000L, fpsLimit));
        }
        return delay.get() - 1;
    }

    public static void setCurrentFreshRate(int framesPerSecond) {
        realFreshRate.set(framesPerSecond);

        if (isFpsLimited()) {
            if (!isPaused() && isCurrentFpsIsLessThanLimit()) {
                riseFps();
            }

            if (isLimitFpsIsLessThanCurrentFps() || isCurrentFpsIsGreaterThanAllowed() || isMaxMonitorRateOver()) {
                coolFps();
            }
        }
    }

    private static boolean isMaxMonitorRateOver() {
        return getFpsLimit() == MON.getRefreshRate() && realFreshRate.get() > MON.getRefreshRate();
    }

    private static void coolFps() {
        if (delay.get() < 500) {
            log.debug("Increase delay (decrease fps)");
            delay.incrementAndGet();
        } else {
            log.warn("Не удастся повысить задержку отрисовки для снижения fps - delay уже равен 500! (fps limited: {})", isFpsLimited());
        }
    }

    private static void riseFps() {
        if (delay.get() > 1) {
            log.debug("Decrease delay (increase fps)");
            delay.decrementAndGet();
        } else {
            log.warn("Не удастся сократить задержку отрисовки для увеличения fps - delay уже равен 1! (fps limited: {})", isFpsLimited());
        }
    }

    private static boolean isCurrentFpsIsGreaterThanAllowed() {
        return realFreshRate.get() > MAX_FPS_ALLOWED;
    }

    private static boolean isLimitFpsIsLessThanCurrentFps() {
        return fpsLimit + SHIFT_FPS_ALLOWED < realFreshRate.get();
    }

    private static boolean isCurrentFpsIsLessThanLimit() {
        return realFreshRate.get() < fpsLimit - SHIFT_FPS_ALLOWED;
    }

    private static boolean isCurrentFpsIsLessThanMonitorRate() {
        return realFreshRate.get() < MON.getRefreshRate();
    }

    private static boolean isCurrentFpsIsGreaterThanMonitorRate() {
        return realFreshRate.get() > MON.getRefreshRate();
    }

    public static int getRealFreshRate() {
        return realFreshRate.get();
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

    public static boolean isLowFpsAlarm() {
        return isFpsLimited() && delay.get() <= 3;
    }
}
