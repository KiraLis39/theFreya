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
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
public final class Constants {
    // libraries objects:
    public static final VideoMonitor MON = new VideoMonitor();
    public static final FoxFontBuilder FFB = new FoxFontBuilder();
    public static final FoxRender RENDER = new FoxRender();
    public static final MediaCache CACHE = MediaCache.getInstance();
    public static final InputAction INPUT_ACTION = new InputAction();
    public static final FoxSpritesCombiner SPRITES_COMBINER = new FoxSpritesCombiner();
    // public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.of("ru"));
    public static final DateTimeFormatter DATE_FORMAT_2 = DateTimeFormatter.ofPattern("День dd (HH:mm)", Locale.of("ru"));
    // fonts:
    public static final Font DEBUG_FONT = FFB.setFoxFont(FONT.ARIAL, 16, true, MON.getEnvironment());
    public static final Font INFO_FONT = FFB.setFoxFont(FONT.ARIAL_NARROW, 14, false, MON.getEnvironment());
    public static final Font MENU_BUTTONS_FONT = FFB.setFoxFont(FONT.CANDARA, 24, true, MON.getEnvironment());
    public static final Font GAME_FONT_01 = FFB.setFoxFont(FONT.ARIAL_NARROW, 12, true, MON.getEnvironment());
    public static final Font GAME_FONT_02 = FFB.setFoxFont(FONT.BAHNSCHRIFT, 26, true, MON.getEnvironment());

    // other:
    public static final int MAX_ZOOM_OUT_CELLS = 23; // максимум отдаление карты ячеек.
    public static final int MIN_ZOOM_OUT_CELLS = 8; // максимум отдаление карты ячеек.
    public static final int MAP_CELL_DIM = 64;
    public static final int SERVER_PORT = 13958;
    public static final int SOCKET_BUFFER_SIZE = 10240; // 65536
    public static final String DEFAULT_AVATAR_URL = "/images/defaultAvatar.png";

    // project:
    @Getter
    private static final String gameName = "Freya the game";
    @Getter
    private static final String gameVersion = "0.0.1";
    @Getter
    private static final String gameAuthor = "KiraLis39";
    @Getter
    private static final Path databaseRootDir = FileSystems.getDefault().getPath("./db/freya.db").toAbsolutePath();
    @Getter
    private static final String imageExtension = ""; // .png
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
    @Getter
    public static Color mainMenuBackgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.75f);
    @Getter
    @Setter
    public static long gameStartedIn;
    @Getter
    public static String notRealizedString = "Не реализовано ещё";
    @Getter
    private static Cursor defaultCursor;
    // user config:
    @Getter
    @Setter
    private static UserConfig userConfig;
    // dynamic game booleans:
    @Getter
    @Setter
    private static boolean isDebugInfoVisible = true;
    @Getter
    @Setter
    private static boolean isFpsInfoVisible = true;
    @Getter
    @Setter
    private static boolean isPaused = false;
    @Getter
    private static boolean isShowStartLogo = false;
    @Getter
    private static volatile long screenDiscreteLimitMem;
    @Getter
    private static long delay = -1;
    @Getter
    private static int realFreshRate = 0;

    static {
        try {
            InputStream is = Constants.class.getResourceAsStream("/cursors/default.png");
            if (is != null) {
                defaultCursor = FoxCursor.createCursor(new ImageIcon(ImageIO.read(is)), "default");
            }
        } catch (IOException e) {
            log.error("Init icon read error: {}", ExceptionUtils.getFullExceptionMessage(e));
            defaultCursor = Cursor.getDefaultCursor();
        }
    }

    private Constants() {
    }

    public static GraphicsConfiguration getGraphicsConfiguration() {
        return MON.getConfiguration();
    }

    public static boolean isFrameLimited() {
        return Constants.getUserConfig().getScreenDiscreteLimit() > 0;
    }

    public static long getDiscreteDelay() {
        if (delay < 0 || screenDiscreteLimitMem != Constants.getUserConfig().getScreenDiscreteLimit()) {
            screenDiscreteLimitMem = Constants.getUserConfig().getScreenDiscreteLimit();
            delay = Math.floorDiv(1000L, screenDiscreteLimitMem);
        }
        return delay;
    }

    public static void setRealFreshRate(int framesPerSecond) {
        realFreshRate = framesPerSecond;
        if (!isPaused()) {
            // повышаем fps только если не в паузе (к чему высокий fps у свёрнутого приложения?):
            if (realFreshRate < screenDiscreteLimitMem - 3) {
                if (delay > 1) {
                    log.info("Decrease delay (increase fps)");
                    delay--;
                } else {
                    log.warn("Не удастся сократить задержку отрисовки для увеличения fps т.к. delay уже равен 1!");
                }
            }
        }

        if (realFreshRate > screenDiscreteLimitMem + 3) {
            if (delay < 250) {
                log.info("Increase delay (decrease fps)");
                delay++;
            } else {
                log.warn("Не удастся повысить задержку отрисовки для снижения fps т.к. delay уже равен 250!");
            }
        }
    }
}
