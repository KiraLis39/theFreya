package game.freya.config;

import fox.FoxFontBuilder;
import fox.FoxFontBuilder.FONT;
import fox.FoxLogo;
import fox.FoxRender;
import fox.components.FOptionPane;
import fox.images.FoxCursor;
import fox.images.FoxSpritesCombiner;
import fox.player.FoxPlayer;
import fox.utils.FoxVideoMonitorUtil;
import fox.utils.InputAction;
import fox.utils.MediaCache;
import game.freya.gui.panes.GameWindowJME;
import game.freya.net.SocketConnection;
import game.freya.net.server.Server;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class Constants {
    // other:
    @Getter
    private static Random random = new Random();
    public static final char SCOBE = ')';
    public static final int MAX_ZOOM_OUT_CELLS = 23; // максимум отдаление карты ячеек.

    public static final int MIN_ZOOM_OUT_CELLS = 8; // максимум отдаление карты ячеек.

    public static final int MAP_CELL_DIM = 64;

    public static final String DEFAULT_AVATAR_URL = "/images/defaultAvatar.png";

    public static final double ONE_TURN_PI = Math.PI / 4d;


    // libraries objects:
    public static final FoxFontBuilder FFB = new FoxFontBuilder();

    public static final FoxRender RENDER = new FoxRender();

    public static final MediaCache CACHE = MediaCache.getInstance();

    public static final InputAction INPUT_ACTION = new InputAction();

    public static final FoxSpritesCombiner SPRITES_COMBINER = new FoxSpritesCombiner();

    // public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.of("ru"));
    // public static final DateTimeFormatter DATE_FORMAT_2 = DateTimeFormatter.ofPattern("День dd (HH:mm)", Locale.of("ru"));
    public static final DateTimeFormatter DATE_FORMAT_3 = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.of("ru"));


    // fonts:
    public static final Font DEBUG_FONT = FFB.setFoxFont(FONT.ARIAL, 16, true, FoxVideoMonitorUtil.getEnvironment());

    public static final Font INFO_FONT = FFB.setFoxFont(FONT.ARIAL_NARROW, 14, false, FoxVideoMonitorUtil.getEnvironment());

    public static final Font GAME_FONT_01 = FFB.setFoxFont(FONT.ARIAL_NARROW, 12, true, FoxVideoMonitorUtil.getEnvironment());

    public static final Font GAME_FONT_02 = FFB.setFoxFont(FONT.BAHNSCHRIFT, 26, true, FoxVideoMonitorUtil.getEnvironment());

    public static final Font GAME_FONT_03 = FFB.setFoxFont(FONT.BAHNSCHRIFT, 32, true, FoxVideoMonitorUtil.getEnvironment());

    public static final Font MENU_BUTTONS_BIG_FONT;

    public static final Font LITTLE_UNICODE_FONT;

    public static final Font MENU_BUTTONS_FONT;

    public static final Font PROPAGANDA_FONT;

    public static final Font PROPAGANDA_BIG_FONT;


    // cache:
    public static final String NO_CACHED_IMAGE_MOCK_KEY = "no_image_mock_key";
    public static final AtomicBoolean isConnectionAwait = new AtomicBoolean(false);
    public static final AtomicBoolean isPingAwait = new AtomicBoolean(false);


    // net:
    @Getter
    private static final String connectionUser = "freya";

    @Getter
    private static final String connectionPassword = "0358";

    @Getter
    private static final boolean connectionAutoCommit = false;

    @Getter
    @Setter
    private static Server server;

    @Getter
    @Setter
    private static SocketConnection localSocketConnection;


    // project:
    @Getter
    private static final String gameAuthor = "KiraLis39";
    @Getter
    private static final Path database = Path.of(System.getenv("LOCALAPPDATA").concat("\\Freya\\freya.db")).toAbsolutePath();
    @Getter
    private static final String userSaveFile = "./saves/".concat(SystemUtils.USER_NAME).concat("/save.json");
    @Getter
    private static final String gameConfigFile = "./config.json";
    @Getter
    private static final String logoImageUrl = "/images/logo.png";
    @Getter
    private static final String logoImageUrl2 = "/images/logo2.png";
    @Getter
    private static final String bcryptSalt = "xxwv";

    // db hikari:
    @Getter
    private static final String connectionUrl = "jdbc:sqlite:".concat(getDatabase().toString());
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
    private static final FoxPlayer backgPlayer = new FoxPlayer("backgPlayer");
    @Getter
    private static final FoxPlayer voicePlayer = new FoxPlayer("voicePlayer");


    @Getter
    @Setter
    private static GameWindowJME gameWindow;

    @Getter
    @Setter
    private static DisplayMode defaultDisplayMode;

    @Getter
    @Setter
    private static FoxLogo logo;

    @Getter
    private static final Color grayBackColor = new Color(0, 0, 0, 223);

    @Getter
    private static final Color mainMenuBackgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.85f);

    @Getter
    private static final Color mainMenuBackgroundColor2 = new Color(0.0f, 0.0f, 0.0f, 0.45f);

    @Getter
    @Setter
    private static long gameStartedIn;

    @Getter
    private static final String notRealizedString = "Не реализовано ещё";

    @Getter
    private static Cursor defaultCursor;

    @Getter
    @Setter
    private static GameConfig gameConfig;


    // user config:
    @Getter
    @Setter
    private static UserConfig userConfig;

    // game:
    @Getter
    private static final int minimapDim = 2048;

    // dynamic:
    @Setter
    @Getter
    private static int realFreshRate = 0;

    @Getter
    private static final AtomicInteger frames = new AtomicInteger(0);

    // задержка вспомогательного потока сцен:
    public static final long SECOND_THREAD_SLEEP_MILLISECONDS = 250;

    static {
        try {
            registerFonts();
        } catch (Exception e) {
            log.error("Fonts register error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

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
        LITTLE_UNICODE_FONT = FFB.setFoxFont(ruf, 18, false, FoxVideoMonitorUtil.getEnvironment());
        MENU_BUTTONS_FONT = FFB.setFoxFont(ruf, 20, true, FoxVideoMonitorUtil.getEnvironment());
        MENU_BUTTONS_BIG_FONT = FFB.setFoxFont(ruf, 28, true, FoxVideoMonitorUtil.getEnvironment());

        ruf = FFB.addNewFont("Propaganda");
        PROPAGANDA_FONT = FFB.setFoxFont(ruf, 26, true, FoxVideoMonitorUtil.getEnvironment());
        PROPAGANDA_BIG_FONT = FFB.setFoxFont(ruf, 30, true, FoxVideoMonitorUtil.getEnvironment());
    }

    private Constants() {
    }

    private static void registerFonts() throws Exception {
        try (InputStream stream = new FileInputStream("./fonts/Propaganda.ttf")) {
            FFB.register(Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f), FoxVideoMonitorUtil.getEnvironment());
        }
    }

    public static GraphicsConfiguration getGraphicsConfiguration() {
        return FoxVideoMonitorUtil.getConfiguration();
    }

    public static void showNFP() {
        new FOptionPane().buildFOptionPane("Не реализовано:",
                "Приносим свои извинения! Данный функционал ещё находится в разработке.",
                FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
    }

    public static boolean isPingAwait() {
        return isPingAwait.get();
    }

    public static void setPingAwait(boolean b) {
        isPingAwait.set(b);
    }

    public static boolean isConnectionAwait() {
        return isConnectionAwait.get();
    }

    public static void setConnectionAwait(boolean b) {
        isConnectionAwait.set(b);
    }
}
