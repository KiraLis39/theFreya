package game.freya.gui.panes.handlers;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.Media;
import game.freya.config.UserConfig;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.other.MovingVector;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.Window;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.sub.AudioSettingsPane;
import game.freya.gui.panes.sub.GameplaySettingsPane;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.HeroesListPane;
import game.freya.gui.panes.sub.HotkeysSettingsPane;
import game.freya.gui.panes.sub.NetCreatingPane;
import game.freya.gui.panes.sub.NetworkListPane;
import game.freya.gui.panes.sub.VideoSettingsPane;
import game.freya.gui.panes.sub.WorldCreatingPane;
import game.freya.gui.panes.sub.WorldsListPane;
import game.freya.gui.panes.sub.components.Chat;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFW;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static game.freya.config.Constants.FFB;
import static javax.swing.JLayeredPane.PALETTE_LAYER;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPostEmptyEvent;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_FOG;
import static org.lwjgl.opengl.GL11.GL_FOG_COLOR;
import static org.lwjgl.opengl.GL11.GL_FOG_DENSITY;
import static org.lwjgl.opengl.GL11.GL_GEQUAL;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PERSPECTIVE_CORRECTION_HINT;
import static org.lwjgl.opengl.GL11.GL_POINT_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_POINT_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11.GL_POLYGON_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_POLYGON_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDepthRange;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFogf;
import static org.lwjgl.opengl.GL11.glFogfv;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glLightfv;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPolygonOffset;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL13.GL_SAMPLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_COMPRESSION_HINT;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP_HINT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER_DERIVATIVE_HINT;

@Getter
@Setter
@Slf4j
public abstract class FoxWindow extends Window {
    public static final long SECOND_THREAD_SLEEP_MILLISECONDS = 250;

    private static final AtomicInteger frames = new AtomicInteger(0);

    private static final short rightShift = 21;

    private static final double infoStrut = 58d, infoStrutHardness = 40d;

    private static final int minimapDim = 2048;

    private static final int halfDim = (int) (minimapDim / 2d);

    private static final Color grayBackColor = new Color(0, 0, 0, 223);

    private static final float[] ambientLight = {0.5f, 0.5f, 0.5f, 1.0f}; // 0.0f, 0.0f, 0.3f, 1.0f

    private static final float[] ambientSpecular = {1.0f, 0.33f, 0.33f, 1.0f};

    private static final float[] ambientPosition = {0.0f, 0.0f, -2.0f, 1.0f}; // 31.84215f, 36.019997f, 28.262873f, 1.0f

    private static final float[] ambientDirection = {0.0f, -0.25f, -0.5f, 1.0f};

    private static final float[] ambientAttenuation = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffuseLight = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffusePosition = {0.5f, 0.5f, -1.5f, 1.0f};

    private static final float[] diffuseSpecular = {0.65f, 0.65f, 0.65f, 1.0f};

    private static final float accelerationMod = 2.0f;

    private static final float camZspeed = 0f;

    private static final float pitchSpeed = 0.15f;

    private static final float yawSpeed = 0.33f;

    private static long timeStamp = System.currentTimeMillis();

    private static double oldPitch = 0, oldYaw = 0;

    private static float currentPitch = 30;

    private static float currentYaw = 0;

    private static float heroXPos = 0, heroYPos = 0;

    private final String name;

    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;

    private final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;

    private final String pausedString, downInfoString1, downInfoString2;

    private final GameController gameController;

    private final AtomicBoolean isConnectionAwait = new AtomicBoolean(false);

    private final AtomicBoolean isPingAwait = new AtomicBoolean(false);

    private Rectangle2D viewPort;

    private Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;

    @Getter
    private Rectangle avatarRect, minimapRect, minimapShowRect, minimapHideRect;

    private BufferedImage pAvatar;

//    private VolatileImage backImage, minimapImage;

    private Polygon leftGrayMenuPoly;

    private Polygon headerPoly;

    private Duration duration;

    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane,
            heroesListPane, networkListPane, networkCreatingPane;

    private float downShift = 0;

    private boolean firstButtonOver = false, firstButtonPressed = false, secondButtonOver = false, secondButtonPressed = false,
            thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;

    private boolean revolatileNeeds = false, isOptionsMenuSetVisible = false;

    @Setter
    @Getter
    private volatile boolean cameraMovingLeft = false, cameraMovingRight = false, cameraMovingForward = false,
            cameraMovingBack = false, cameraMovingUp = false, cameraMovingDown = false;

    private Chat chat;

    private JFrame parentFrame;

    private byte creatingSubsRetry = 0;

    private long lastTimestamp;

    private int fps;

    private boolean isAccelerated = false, isSneaked = false, isZoomEnabled = false;

    private Thread sneakThread;

    private float heroHeight = -6;

    private float velocity = 0;

    private WindowManager windowManager;

    private ScreenType type;

    protected FoxWindow(ScreenType type, String name, WindowManager windowManager, GameController controller) {
        super(controller);

        this.name = name;
        this.type = type;
        this.gameController = controller;
        this.windowManager = windowManager;

        this.audioSettingsButtonText = "Настройки звука";
        this.videoSettingsButtonText = "Настройки графики";
        this.hotkeysSettingsButtonText = "Управление";
        this.gameplaySettingsButtonText = "Геймплей";
        this.backButtonText = "← Назад";
        this.exitButtonText = "← Выход";

        this.backToGameButtonText = "Вернуться";
        this.optionsButtonText = "Настройки";
        this.saveButtonText = "Сохранить";

        this.downInfoString1 = Constants.getAppCompany();
        this.downInfoString2 = Constants.getAppName().concat(" v.").concat(Constants.getAppVersion());

        this.pausedString = "- PAUSED -";

//        setForeground(Color.BLACK);

        setInAc();
    }

    protected void drawBackground(Graphics2D bufGraphics2D) throws AWTException {
//        Graphics2D v2D = getValidVolatileGraphic();
//        Constants.RENDER.setRender(v2D, FoxRender.RENDER.MED,
//                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

//        if (getName().equals("GameCanvas")) {
//            repaintGame(v2D);
//        } else {
//            repaintMenu(v2D);
//        }

//        Constants.RENDER.setRender(v2D, FoxRender.RENDER.MED,
//                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
//        drawUI(v2D, getName());

//        Constants.RENDER.setRender(v2D, FoxRender.RENDER.LOW,
//                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
//        drawDebugInfo(v2D, gameController.getCurrentWorldTitle());

//        if (Constants.isFpsInfoVisible()) {
//            drawFps(v2D);
//        }

//        v2D.dispose();

        // draw accomplished volatile image:
//        bufGraphics2D.drawImage(this.backImage, 0, 0, this);
    }

    protected void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.GAME_FONT_03);
        g2D.setColor(new Color(0, 0, 0, 63));
        g2D.drawString(getPausedString(),
                (int) (getWidth() / 2D - FFB.getHalfWidthOfString(g2D, getPausedString())), getHeight() / 2 + 3);

        g2D.setFont(Constants.GAME_FONT_02);
        g2D.setColor(Color.GRAY);
        g2D.drawString(getPausedString(),
                (int) (getWidth() / 2D - FFB.getHalfWidthOfString(g2D, getPausedString())), getHeight() / 2);

        // fill left gray menu polygon:
        drawLeftGrayPoly(g2D);

        drawEscMenu(g2D);
    }

//    private void drawFps(double width, double height) {

    // FPS check:
//        incrementFramesCounter();
//        if (System.currentTimeMillis() >= timeStamp + 1000L) {
//            Constants.setRealFreshRate(frames.get());
//            frames.set(0);
//            timeStamp = System.currentTimeMillis();
//        }

//        v2D.setFont(Constants.DEBUG_FONT);
//        v2D.setColor(Color.BLACK);
//        if (gameController.isGameActive() && gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
//            v2D.drawString("World IP: " + gameController.getCurrentWorldAddress(), rightShift - 1f, downShift - 25);
//        }
//        v2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
//                .formatted(Constants.getUserConfig().getFpsLimit(), Constants.MON.getRefreshRate(),
//                        Constants.getRealFreshRate()), rightShift - 1f, downShift + 1f);

//        v2D.setColor(Color.GRAY);
//        if (gameController.isGameActive() && gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
//            v2D.drawString("World IP: " + gameController.getCurrentWorldAddress(), rightShift, downShift - 24);
//        }
//        v2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
//                .formatted(Constants.getUserConfig().getFpsLimit(), Constants.MON.getRefreshRate(),
//                        Constants.getRealFreshRate()), rightShift, downShift);
//    }

    private void drawEscMenu(Graphics2D g2D) {
        // buttons text:
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(getBackToGameButtonText(), getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
        g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getBackToGameButtonText(), getFirstButtonRect().x, getFirstButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getOptionsButtonText(), getSecondButtonRect().x - 1, getSecondButtonRect().y + 17);
        g2D.setColor(isSecondButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getOptionsButtonText(), getSecondButtonRect().x, getSecondButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getSaveButtonText(), getThirdButtonRect().x - 1, getThirdButtonRect().y + 17);
        g2D.setColor(isThirdButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getSaveButtonText(), getThirdButtonRect().x, getThirdButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
    }

    private void drawMinimap(Graphics2D v2D) {
        // down left minimap:
        if (!Constants.isPaused()) {
            Rectangle mapButRect;
            if (Constants.isMinimapShowed()) {
                mapButRect = getMinimapShowRect();

                updateMiniMap();

                if (getMinimapRect() != null) {
                    // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2));
                    Composite cw = v2D.getComposite();
                    v2D.setComposite(AlphaComposite.SrcAtop.derive(Constants.getUserConfig().getMiniMapOpacity()));
//                    v2D.drawImage(this.minimapImage, getMinimapRect().x, getMinimapRect().y,
//                            getMinimapRect().width, getMinimapRect().height, this);
                    v2D.setComposite(cw);

                    if (Constants.isDebugInfoVisible()) {
                        v2D.setColor(Color.CYAN);
                        v2D.draw(getMinimapRect());
                    }
                }
            } else {
                mapButRect = getMinimapHideRect();
            }

            if (mapButRect != null) {
                // draw minimap button:
                v2D.setColor(Color.YELLOW);
                v2D.fillRoundRect(mapButRect.x, mapButRect.y, mapButRect.width, mapButRect.height, 4, 4);
                v2D.setColor(Color.GRAY);
                v2D.drawRoundRect(mapButRect.x, mapButRect.y, mapButRect.width, mapButRect.height, 4, 4);

                v2D.setColor(Color.BLACK);
                v2D.setStroke(new BasicStroke(2f));
                v2D.drawPolyline(new int[]{mapButRect.x + 3, mapButRect.x + mapButRect.width / 2, mapButRect.x + mapButRect.width - 3},
                        new int[]{mapButRect.y + 3, mapButRect.y + mapButRect.height - 3, mapButRect.y + 3}, 3);
            }
        } else {
            drawPauseMode(v2D);
        }
    }

    private void updateMiniMap() {
        Point2D.Double myPos = gameController.getCurrentHeroPosition();
        MovingVector cVector = gameController.getCurrentHeroVector();
        int srcX = (int) (myPos.x - halfDim);
        int srcY = (int) (myPos.y - halfDim);

//        Graphics2D m2D;
//        if (minimapImage == null || minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
//            log.info("Recreating new minimap volatile image by incompatible...");
////            minimapImage = createVolatileImage(minimapDim, minimapDim, new ImageCapabilities(true));
//        }
//        if (minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_RESTORED) {
//            log.info("Awaits while minimap volatile image is restored...");
//            m2D = this.minimapImage.createGraphics();
//        } else {
//            m2D = (Graphics2D) this.minimapImage.getGraphics();
//            m2D.clearRect(0, 0, minimapImage.getWidth(), minimapImage.getHeight());
//        }

        // draw minimap:
//        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

//        v2D.setColor(backColor);
//        v2D.fillRect(0, 0, camera.width, camera.height);

        // отображаем себя на миникарте:
//        AffineTransform grTrMem = m2D.getTransform();
//        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
//        m2D.drawImage((Image) Constants.CACHE.get("green_arrow"), halfDim - 64, halfDim - 64, 128, 128, null);
//        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (HeroDTO connectedHero : gameController.getConnectedHeroes()) {
            if (gameController.getCurrentHeroUid().equals(connectedHero.getCharacterUid())) {
                continue;
            }
            int otherHeroPosX = (int) (halfDim - (myPos.x - connectedHero.getLocation().x));
            int otherHeroPosY = (int) (halfDim - (myPos.y - connectedHero.getLocation().y));
//            log.info("Рисуем игрока {} в точке миникарты {}x{}...", connectedHero.getHeroName(), otherHeroPosX, otherHeroPosY);
//            m2D.setColor(connectedHero.getBaseColor());
//            m2D.fillRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
//            m2D.setColor(connectedHero.getSecondColor());
//            m2D.drawRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
        }

        if (gameController.getCurrentWorldMap() != null) {
            // сканируем все сущности указанного квадранта:
            Rectangle scanRect = new Rectangle(
                    Math.min(Math.max(srcX, 0), gameController.getCurrentWorldMap().getWidth() - minimapDim),
                    Math.min(Math.max(srcY, 0), gameController.getCurrentWorldMap().getHeight() - minimapDim),
                    minimapDim, minimapDim);

//            m2D.setColor(Color.CYAN);
//            gameController.getWorldEnvironments(scanRect)
//                    .forEach(entity -> {
//                        int otherHeroPosX = (int) (halfDim - (myPos.x - entity.getCenterPoint().x));
//                        int otherHeroPosY = (int) (halfDim - (myPos.y - entity.getCenterPoint().y));
//                        m2D.fillRect(otherHeroPosX - 16, otherHeroPosY - 16, 32, 32);
//                    });
        }

//        m2D.setStroke(new BasicStroke(5f));
//        m2D.setPaint(Color.WHITE);
//        m2D.drawRect(3, 3, minimapDim - 7, minimapDim - 7);

//        m2D.setStroke(new BasicStroke(7f));
//        m2D.setPaint(Color.GRAY);
//        m2D.drawRect(48, 48, minimapDim - 96, minimapDim - 96);
//        m2D.dispose();
    }

    private void drawHeroesData(Graphics2D g2D) {
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.WHITE);

        Collection<HeroDTO> heroes;
        if (gameController.isCurrentHeroOnline()) {
            heroes = gameController.getConnectedHeroes();
        } else {
            if (gameController.getCurrentHero() == null) {
                throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "Этого не должно было случиться.");
            }
            heroes = List.of(gameController.getCurrentHero());
        }

        if (getViewPort() != null) {
            // draw heroes data:
//            int sMod = (int) (infoStrut - ((getViewPort().getHeight() - getViewPort().getY()) / infoStrutHardness));
            heroes.forEach(hero -> {
//                int strutMod = sMod;
                if (gameController.isHeroActive(hero, getViewPort().getBounds())) {

                    // Преобразуем координаты героя из карты мира в координаты текущего холста:
//                    Point2D relocatedPoint = FoxPointConverter.relocateOn(getViewPort(), getBounds(), hero.getLocation());

                    // draw hero name:
//                    g2D.drawString(hero.getCharacterName(),
//                            (int) (relocatedPoint.getX() - FFB.getHalfWidthOfString(g2D, hero.getCharacterName())),
//                            (int) (relocatedPoint.getY() - strutMod));

//                    strutMod += 24;

                    // draw hero OIL:
                    g2D.setColor(Color.YELLOW);
//                    g2D.fillRoundRect((int) (relocatedPoint.getX() - 50),
//                            (int) (relocatedPoint.getY() - strutMod),
//                            hero.getHealth(), 9, 3, 3);
                    g2D.setColor(Color.WHITE);
//                    g2D.drawRoundRect((int) (relocatedPoint.getX() - 50),
//                            (int) (relocatedPoint.getY() - strutMod),
//                            hero.getMaxHealth(), 9, 3, 3);

                    // draw hero HP:
                    g2D.setColor(Color.RED);
//                    g2D.fillRoundRect((int) (relocatedPoint.getX() - 50),
//                            (int) (relocatedPoint.getY() - strutMod),
//                            hero.getHealth(), 9, 3, 3);
                    g2D.setColor(Color.WHITE);
//                    g2D.drawRoundRect((int) (relocatedPoint.getX() - 50),
//                            (int) (relocatedPoint.getY() - strutMod),
//                            hero.getMaxHealth(), 9, 3, 3);
                }
            });
        }
    }

    protected void drawDebug(double width, double height, String worldTitle) {
        glBegin(GL_LINE_LOOP);

        glColor3f(0.5f, 0.0f, 0.0f);
        glLineWidth(12f);

        glVertex2d(1, 1);
        glVertex2d(width - 2, 1);
        glVertex2d(width - 2, height - 2);
        glVertex2d(1, height - 2);

        glEnd();

//        v2D.setFont(Constants.DEBUG_FONT);

//        if (worldTitle != null && gameController.isGameActive()) {
//            String pass = duration != null
//                    ? "День %d, %02d:%02d".formatted(duration.toDays(), duration.toHours(), duration.toMinutes())
////                    ? LocalDateTime.of(0, 1, (int) (duration.toDaysPart() + 1), duration.toHoursPart(), duration.toMinutesPart(), 0, 0)
////                    .format(Constants.DATE_FORMAT_2)
//                    : "=na=";
//            System.out.format(, duration.toDays(), duration.toHours(), duration.toMinutes(), duration.getSeconds(), duration.toMillis());

//            v2D.setColor(Color.BLACK);
//            v2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + 22);
//            v2D.drawString("В игре: %s".formatted(pass), rightShift - 1f, downShift + 43);
//
//            v2D.setColor(Color.GRAY);
//            v2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + 21);
//            v2D.drawString("В игре: %s".formatted(pass), rightShift, downShift + 42);
//        }

//        final int leftShift = 340;
//        v2D.setColor(Color.GRAY);

        // graphics info:
//        BufferCapabilities gCap = getBufferStrategy().getCapabilities();
//        v2D.drawString("Front accelerated: %s".formatted(gCap.getFrontBufferCapabilities().isAccelerated()),
//                getWidth() - leftShift, getHeight() - 370);
//        v2D.drawString("Front is true volatile: %s".formatted(gCap.getFrontBufferCapabilities().isTrueVolatile()),
//                getWidth() - leftShift, getHeight() - 350);
//        v2D.drawString("Back accelerated: %s".formatted(gCap.getBackBufferCapabilities().isAccelerated()),
//                getWidth() - leftShift, getHeight() - 325);
//        v2D.drawString("Back is true volatile: %s".formatted(gCap.getBackBufferCapabilities().isTrueVolatile()),
//                getWidth() - leftShift, getHeight() - 305);
//        v2D.drawString("Fullscreen required: %s".formatted(gCap.isFullScreenRequired()), getWidth() - leftShift, getHeight() - 280);
//        v2D.drawString("Multi-buffer available: %s".formatted(gCap.isMultiBufferAvailable()), getWidth() - leftShift, getHeight() - 260);
//        v2D.drawString("Is page flipping: %s".formatted(gCap.isPageFlipping()), getWidth() - leftShift, getHeight() - 240);

        // server info:
//        boolean isServerIsOpen = gameController.isServerIsOpen();
//        boolean isSocketIsConnected = gameController.isSocketIsOpen();
//        v2D.setColor(isServerIsOpen || isSocketIsConnected ? Color.GREEN : Color.DARK_GRAY);
//        v2D.drawString("Server open: %s".formatted(isServerIsOpen || isSocketIsConnected), getWidth() - leftShift, getHeight() - 210);
//        if (isServerIsOpen) {
//            v2D.drawString("Connected clients: %s".formatted(gameController.getConnectedClientsCount()),
//                    getWidth() - leftShift, getHeight() - 190);
//        }
//        v2D.drawString("Connected players: %s".formatted(isServerIsOpen
//                        ? gameController.getConnectedPlayers().size() : gameController.getPlayedHeroesService().getHeroes().size()),
//                getWidth() - leftShift, getHeight() - 170);
//        v2D.setColor(Color.GRAY);

        // если мы в игре:
//        if (gameController.isGameActive()) {
//            // hero info:
////            if (gameController.getCurrentHeroPosition() != null) {
////                Shape playerShape = new Ellipse2D.Double(
////                        (int) gameController.getCurrentHeroPosition().x - Constants.MAP_CELL_DIM / 2d,
////                        (int) gameController.getCurrentHeroPosition().y - Constants.MAP_CELL_DIM / 2d,
////                        Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);
////                v2D.drawString("Hero pos: %.0fx%.0f".formatted(playerShape.getBounds2D().getCenterX(), playerShape.getBounds2D().getCenterY()),
////                        getWidth() - leftShift, getHeight() - 140);
////                v2D.drawString("Hero speed: %s".formatted(gameController.getCurrentHeroSpeed()), getWidth() - leftShift, getHeight() - 120);
////                v2D.drawString("Hero vector: %s %s %s".formatted(gameController.getCurrentHeroVector().getY(),
////                                gameController.getCurrentHeroVector().getX(), gameController.getCurrentHeroVector().getZ()),
////                        getWidth() - leftShift, getHeight() - 100);
////            }
//
//            // gameplay info:
//            if (gameController.getCurrentWorldMap() != null) {
////                v2D.drawString("GameMap WxH: %dx%d"
////                                .formatted(gameController.getCurrentWorldMap().getWidth(), gameController.getCurrentWorldMap().getHeight()),
////                        getWidth() - leftShift, getHeight() - 70);
////
////                v2D.drawString("Canvas XxY-WxH: %dx%d-%dx%d".formatted(getBounds().x, getBounds().y, getBounds().width, getBounds().height),
////                        getWidth() - leftShift, getHeight() - 50);
//
//                if (viewPort != null) {
////                    v2D.drawString("ViewPort XxY-WxH: %dx%d-%dx%d"
////                                    .formatted(viewPort.getBounds().x, viewPort.getBounds().y, viewPort.getBounds().width, viewPort.getBounds().height),
////                            getWidth() - leftShift, getHeight() - 30);
//                }
//            }
//        }

//        if (Constants.isFpsInfoVisible()) {
//            drawFps(width, height);
//        }
    }

    protected void checkGameplayDuration(long inGamePlayed) {
        this.duration = Duration.ofMillis(inGamePlayed + (System.currentTimeMillis() - Constants.getGameStartedIn()));
    }

    protected void drawHeader(Graphics2D g2D, String headerTitle) {
        g2D.setColor(Color.DARK_GRAY.darker());
        g2D.fill(getHeaderPoly());
        g2D.setColor(Color.BLACK);
        g2D.draw(getHeaderPoly());

        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString(headerTitle, getWidth() / 11 - 1, (int) (getHeight() * 0.041D) + 1);
        g2D.setColor(Color.BLACK);
        g2D.drawString(headerTitle, getWidth() / 11, (int) (getHeight() * 0.041D));
    }

    protected void showOptions(Graphics2D g2D) {
        drawLeftGrayPoly(g2D);

        // draw header:
        drawHeader(g2D, "Настройки игры");

        // default buttons text:
        g2D.setColor(Color.BLACK);
        g2D.drawString(audioSettingsButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
        g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(audioSettingsButtonText, firstButtonRect.x, firstButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(videoSettingsButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
        g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(videoSettingsButtonText, secondButtonRect.x, secondButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(hotkeysSettingsButtonText, thirdButtonRect.x - 1, thirdButtonRect.y + 17);
        g2D.setColor(thirdButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(hotkeysSettingsButtonText, thirdButtonRect.x, thirdButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(gameplaySettingsButtonText, fourthButtonRect.x - 1, fourthButtonRect.y + 17);
        g2D.setColor(fourthButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(gameplaySettingsButtonText, fourthButtonRect.x, fourthButtonRect.y + 18);

        addExitVariantToOptionsMenuFix(g2D);
    }

    private void addExitVariantToOptionsMenuFix(Graphics2D g2D) {
        g2D.setColor(Color.BLACK);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
    }

    protected void drawLeftGrayPoly(Graphics2D g2D) {
        // fill left gray polygon:
        g2D.setColor(isOptionsMenuSetVisible() ? Constants.getMainMenuBackgroundColor2() : Constants.getMainMenuBackgroundColor());
        g2D.fillPolygon(getLeftGrayMenuPoly());
    }

    public void onExitBack() {
        if (isOptionsMenuSetVisible()) {
            setOptionsMenuSetVisible(false);
            audiosPane.setVisible(false);
            videosPane.setVisible(false);
            hotkeysPane.setVisible(false);
            gameplayPane.setVisible(false);
        } else if (audiosPane != null && audiosPane.isVisible()) {
            audiosPane.setVisible(false);
        } else if (videosPane != null && videosPane.isVisible()) {
            videosPane.setVisible(false);
        } else if (hotkeysPane != null && hotkeysPane.isVisible()) {
            hotkeysPane.setVisible(false);
        } else if (gameplayPane != null && gameplayPane.isVisible()) {
            gameplayPane.setVisible(false);
        } else if (heroCreatingPane != null && heroCreatingPane.isVisible()) {
            heroCreatingPane.setVisible(false);
            heroesListPane.setVisible(true);
            return;
        } else if (worldCreatingPane != null && worldCreatingPane.isVisible()) {
            worldCreatingPane.setVisible(false);
            worldsListPane.setVisible(true);
            return;
        } else if (worldsListPane != null && worldsListPane.isVisible()) {
            worldsListPane.setVisible(false);
        } else if (heroesListPane != null && heroesListPane.isVisible()) {
            heroesListPane.setVisible(false);
        } else if (networkListPane != null && networkListPane.isVisible()) {
            networkListPane.setVisible(false);
        } else if (networkCreatingPane != null && networkCreatingPane.isVisible()) {
            networkCreatingPane.setVisible(false);
            networkListPane.setVisible(true);
            return;
        } else {
            Constants.setPaused(!Constants.isPaused());
        }

        // любая панель на этом месте скрывается:
        hidePanelIfNotNull(audiosPane);
        hidePanelIfNotNull(videosPane);
        hidePanelIfNotNull(hotkeysPane);
        hidePanelIfNotNull(gameplayPane);
        hidePanelIfNotNull(heroCreatingPane);
        hidePanelIfNotNull(worldCreatingPane);
        hidePanelIfNotNull(worldsListPane);
        hidePanelIfNotNull(heroesListPane);
        hidePanelIfNotNull(networkListPane);
        hidePanelIfNotNull(networkCreatingPane);
    }

    protected void hidePanelIfNotNull(JPanel panel) {
        if (panel != null) {
            panel.setVisible(false);
        }
    }

    protected boolean isShadowBackNeeds() {
        return isOptionsMenuSetVisible
                || (heroCreatingPane != null && heroCreatingPane.isVisible())
                || (worldCreatingPane != null && worldCreatingPane.isVisible())
                || (worldsListPane != null && worldsListPane.isVisible())
                || (heroesListPane != null && heroesListPane.isVisible())
                || (networkListPane != null && networkListPane.isVisible())
                || (networkCreatingPane != null && networkCreatingPane.isVisible());
    }

    protected void drawAvatar(Graphics2D g2D) {
        if (pAvatar == null) {
            pAvatar = gameController.getCurrentPlayerAvatar();
        }
        g2D.setColor(Color.BLACK);
        g2D.setStroke(new BasicStroke(5f));
//        g2D.drawImage(pAvatar, getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, this);
        g2D.drawRoundRect(getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, 16, 16);
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.drawString(gameController.getCurrentPlayerNickName(),
                (int) (getAvatarRect().getCenterX() - Constants.FFB.getHalfWidthOfString(g2D, gameController.getCurrentPlayerNickName())),
                getAvatarRect().height + 24);
    }

    protected void createSubPanes() {
        setAudiosPane(new AudioSettingsPane(this));
        setVideosPane(new VideoSettingsPane(this));
        setHotkeysPane(new HotkeysSettingsPane(this));
        setGameplayPane(new GameplaySettingsPane(this));
        setWorldCreatingPane(new WorldCreatingPane(this));
        setHeroCreatingPane(new HeroCreatingPane(this, gameController));
        setWorldsListPane(new WorldsListPane(this, gameController));
        setHeroesListPane(new HeroesListPane(this, gameController));
        setNetworkListPane(new NetworkListPane(this, gameController));
        setNetworkCreatingPane(new NetCreatingPane(this));

        // добавляем панели на слой:
        try {
            if (getAudiosPane() == null) {
                Thread.sleep(100);
            }
            parentFrame.getContentPane().add(getAudiosPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getVideosPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getHotkeysPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getGameplayPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getHeroCreatingPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getWorldCreatingPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getWorldsListPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getHeroesListPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getNetworkListPane(), PALETTE_LAYER, 0);
            parentFrame.getContentPane().add(getNetworkCreatingPane(), PALETTE_LAYER, 0);
        } catch (Exception e) {
            log.error("Ошибка при добавлении панелей на слой: {}", ExceptionUtils.getFullExceptionMessage(e));
            creatingSubsRetry++;
            if (creatingSubsRetry < 3) {
                createSubPanes();
            } else {
                log.error("Слишком часто не удается создать панели. Обратить внимание!");
                return;
            }
        }
        creatingSubsRetry = 0;
    }

    protected void createChat() {
        this.chat = new Chat(new Point(getWidth() - getWidth() / 5 - 9, 64), new Dimension(getWidth() / 5, getHeight() / 4));
    }

    protected void setSecondThread(String threadName, Thread secondThread) {
//        if (this.secondThread != null && this.secondThread.isAlive()) {
//            this.secondThread.interrupt();
//        }

//        this.secondThread = secondThread;
//        this.secondThread.setName(threadName);
//        this.secondThread.setDaemon(true);
    }

    /**
     * Проверка доступности удалённого сервера:
     *
     * @param host     адрес, куда стучимся для получения pong.
     * @param port     адрес, куда стучимся для получения pong.
     * @param worldUid uid мира, который пропинговывается.
     * @return успешность получения pong от удалённого Сервера.
     */
    public boolean ping(String host, Integer port, UUID worldUid) {
        return gameController.ping(host, port, worldUid);
    }

    public boolean isConnectionAwait() {
        return isConnectionAwait.get();
    }

    public void setConnectionAwait(boolean b) {
        isConnectionAwait.set(b);
        if (!b) {
            // очищаем от анимации панели:
            getNetworkListPane().repaint();
        }
    }

    public boolean isPingAwait() {
        return isPingAwait.get();
    }

    public void setPingAwait(boolean b) {
        isPingAwait.set(b);
        if (!b) {
            // очищаем от анимации панели:
            getNetworkListPane().repaint();
        }
    }

    protected void throwExceptionAndYield(Exception e) {
//        if (drawErrors.getAndIncrement() >= 100) {
        new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                "Что-то не так с графической системой (%s). Передайте последний лог (error.*) разработчику для решения проблемы."
                        .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
        if (gameController.isGameActive()) {
            throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
        } else {
            gameController.exitTheGame(null, 11);
        }
        Thread.yield();
//        }
    }


    protected boolean canDragDown() {
        return viewPort.getY() > 0;
    }

    protected boolean canDragUp() {
        return viewPort.getHeight() < gameController.getCurrentWorldMap().getHeight();
    }

    protected boolean canDragLeft() {
        return viewPort.getWidth() < gameController.getCurrentWorldMap().getWidth();
    }

    protected boolean canDragRight() {
        return viewPort.getX() > 0;
    }

    protected void checkOutOfFieldCorrection() {
        while (getViewPort().getX() < 0) {
            dragLeft(1d);
        }

        while (getViewPort().getWidth() > gameController.getCurrentWorldMap().getWidth()) {
            dragRight(1d);
        }

        while (getViewPort().getY() < 0) {
            dragUp(1d);
        }

        while (getViewPort().getHeight() > gameController.getCurrentWorldMap().getHeight()) {
            dragDown(1d);
        }
    }

    public void dragLeft(double pixels) {
        if (canDragLeft()) {
            log.debug("Drag left...");
            double mapWidth = gameController.getCurrentWorldMap().getWidth();
            double newWidth = Math.min(getViewPort().getWidth() + pixels, mapWidth);
            getViewPort().setRect(getViewPort().getX() + pixels - (newWidth == mapWidth
                            ? Math.abs(getViewPort().getWidth() + pixels - mapWidth) : 0),
                    getViewPort().getY(), newWidth, getViewPort().getHeight());
        }
    }

    public void dragRight(double pixels) {
        if (canDragRight()) {
            log.debug("Drag right...");
            double newX = getViewPort().getX() - pixels > 0 ? getViewPort().getX() - pixels : 0;
            getViewPort().setRect(newX, getViewPort().getY(),
                    getViewPort().getWidth() - pixels + (newX == 0 ? Math.abs(getViewPort().getX() - pixels) : 0), getViewPort().getHeight());
        }
    }

    public void dragUp(double pixels) {
        if (canDragUp()) {
            log.debug("Drag up...");
            double mapHeight = gameController.getCurrentWorldMap().getHeight();
            double newHeight = Math.min(getViewPort().getHeight() + pixels, mapHeight);
            getViewPort().setRect(getViewPort().getX(), getViewPort().getY() + pixels - (newHeight == mapHeight
                            ? Math.abs(getViewPort().getHeight() + pixels - mapHeight) : 0),
                    getViewPort().getWidth(), newHeight);
        }
    }

    public void dragDown(double pixels) {
        if (canDragDown()) {
            log.debug("Drag down...");
            double newY = getViewPort().getY() - pixels > 0 ? getViewPort().getY() - pixels : 0;
            getViewPort().setRect(getViewPort().getX(), newY, getViewPort().getWidth(),
                    getViewPort().getHeight() - pixels + (newY == 0 ? Math.abs(getViewPort().getY() - pixels) : 0));
        }
    }

    protected void delayDrawing(long delta) {
        if (Constants.isFpsLimited() && Constants.getAimTimePerFrame() > delta) {
            try {
                long delay = Constants.getAimTimePerFrame() - delta - 12;
                if (delay > 0) {
                    Thread.sleep(delay);
                } else {
                    Thread.yield();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected void configureThis() {
        // обрезка области рисования:
        // Запрещает отрисовку за пределами указанной квадратной зоны на экране. Естественно, основное применение этой фичи - GUI
        //  (например, довольно сложно реализовать скроллящуюся панель без этой возможности).
        // Сопутствующая функция: glScissor(x, y, width, height).
        //  Координаты и размеры указываются в пикселях в окне, а не в том, что называется "пикселями" в ГУИ и на практике обычно
        //  оказывается больше реальных пикселей. Кроме того, ось Y идет снизу, а не сверху. Пример использования (запретить отрисовку
        //  за пределами квадрата 100х100 в верхнем левом углу экрана): glScissor(0, mc.displayHeight - 100, 100, 100);
//         glEnable(GL_SCISSOR_TEST);
//         glScissor(256, getHeight() - 256, 512, 128);

        // текстуры:
        if (Constants.getGameConfig().isUseTextures()) {
            if (glIsEnabled(GL_TEXTURE_2D)) {
                return;
            }

            glEnable(GL_TEXTURE_2D); // включаем отображение текстур.
            glHint(GL_SAMPLES, 4);
            glEnable(GL_MULTISAMPLE);

            gameController.loadMenuTextures(); // подключаем текстуры, если требуется.

            // Включает смещение данных из буфера глубины при отрисовке. Звучит немного непонятно, зато решает гораздо более понятную проблему.
            //  Если попробовать отрендерить что-то поверх уже отрисованной поверхности (пример из Майна -
            //  текстура разрушения блока поверх самого блока), то начнутся проблемы, связанные с точностью буфера глубины.
//            glEnable(GL_POLYGON_OFFSET_POINT);
//            glEnable(GL_POLYGON_OFFSET_LINE);
            glEnable(GL_POLYGON_OFFSET_FILL);
            // Задает смещение. Обычное использование в майне - glPolygonOffset(-3.0F, -3.0F).
            //  Кроме того, перед рендерингом, с использованием этой возможности, обычно отключают glDepthMask().
            glPolygonOffset(1.0f, 1.0f);
            // glPolygonOffset(-0.5f, -0.5f);

            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
//		      glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
//            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_DONT_CARE);

//		      glHint(GL_TEXTURE_COMPRESSION_HINT, GL_FASTEST);
            glHint(GL_TEXTURE_COMPRESSION_HINT, GL_NICEST);
//            glHint(GL_TEXTURE_COMPRESSION_HINT, GL_DONT_CARE);

//		      glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_FASTEST);
            glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_NICEST);
//            glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_DONT_CARE);

//		      glHint(GL_GENERATE_MIPMAP_HINT, GL_FASTEST);
            glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
//            glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);
        } else {
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_MULTISAMPLE);
//            glDisable(GL_POLYGON_OFFSET_POINT);
//            glDisable(GL_POLYGON_OFFSET_LINE);
            glDisable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(0f, 0f);
        }

        // обрезание невидимых глазу частей:
        if (Constants.getGameConfig().isCullFaceGlEnabled()) {
            cullFace();
        } else {
            glDisable(GL_CULL_FACE);
        }

        // освещение:
        if (Constants.getGameConfig().isLightsEnabled()) {
            setLights();
        } else {
            // glDisable(GL_LIGHT1); // надо ли?
            // glDisable(GL_LIGHT0); // надо ли?
            glDisable(GL_LIGHTING);
            glDisable(GL_NORMALIZE); // надо ли?
        }

        if (Constants.getGameConfig().isColorMaterialEnabled()) {
            setColorMaterial();
        } else {
            glDisable(GL_COLOR_MATERIAL);
        }

        // интерполяция
        if (Constants.getGameConfig().isSmoothEnabled()) {
            setSmooth();
        } else {
            setFlat();
        }

        // ?..
        if (Constants.getGameConfig().isBlendEnabled()) {
            setBlend();
        } else {
            setDepth();
        }

        // буфер глубины (учёт расположения объектов в глубину псевдо-объема):
        if (Constants.getGameConfig().isDepthEnabled()) {
            setDepth();
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        // туман:
        if (Constants.getGameConfig().isUseFog()) {
            setFog();
        } else {
            glDisable(GL_FOG);
        }

        // учёт прозрачности?..
        if (Constants.getGameConfig().isUseAlphaTest()) {
            setAlphaTest();
        } else {
            glDisable(GL_ALPHA_TEST);
        }
    }

    protected void setSmooth() {
        if (glIsEnabled(GL_SMOOTH)) {
            return;
        }

        glDisable(GL_FLAT);

        glEnable(GL_POINT_SMOOTH);
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glEnable(GL_POLYGON_SMOOTH);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        glEnable(GL_SMOOTH);

        // Задает простое или сглаженное освещение. GL_FLAT стоит использовать, если в качестве нормалей
        //  вы используете перпендикуляр к полигону,
        //  GL_SMOOTH - если средний вектор между перпендикулярами к нескольким полигонам.
        glShadeModel(GL_SMOOTH);
    }

    protected void setFlat() {
        glDisable(GL_SMOOTH);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_POINT_SMOOTH);
        glDisable(GL_POLYGON_SMOOTH);

        glEnable(GL_FLAT);

        // Задает простое или сглаженное освещение. GL_FLAT стоит использовать, если в качестве нормалей
        //  вы используете перпендикуляр к полигону,
        //  GL_SMOOTH - если средний вектор между перпендикулярами к нескольким полигонам.
        glShadeModel(GL_FLAT);
    }

    protected void setLights() {
        if (glIsEnabled(GL_LIGHTING) && glIsEnabled(GL_COLOR_MATERIAL)) {
            return;
        }

        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);
//        glLightfv(GL_LIGHT0, GL_SPECULAR, ambientSpecular);
//        glLightfv(GL_LIGHT0, GL_POSITION, ambientPosition);
//        glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, ambientDirection);
//        glLightfv(GL_LIGHT0, GL_LINEAR_ATTENUATION, ambientAttenuation);
//        glLightfv(GL_LIGHT0, GL_CONSTANT_ATTENUATION, ambientAttenuation);
//        glLightfv(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, ambientAttenuation);

//        glLighti(GL_LIGHT0, GL_SPOT_EXPONENT, 64); //range 0-128
//        glLighti(GL_LIGHT0, GL_SPOT_CUTOFF, 90); //range 0-90 and the special value 180
        glEnable(GL_LIGHT0);

        glLightfv(GL_LIGHT1, GL_DIFFUSE, diffuseLight);
        glLightfv(GL_LIGHT1, GL_POSITION, diffusePosition);
        glLightfv(GL_LIGHT1, GL_SPECULAR, diffuseSpecular);
        // glLightfv(GL_LIGHT1, GL_SPOT_DIRECTION, new float[] {0.25f, 0.25f, -0.75f, 0.5f});
        // glLightf(GL_LIGHT1, GL_QUADRATIC_ATTENUATION, 0.5f); // GL_LINEAR_ATTENUATION | GL_QUADRATIC_ATTENUATION | GL_CONSTANT_ATTENUATION
        // glLightf(GL_LIGHT1, GL_SPOT_CUTOFF, 45.0f);
        // glLightf(GL_LIGHT1, GL_SPOT_EXPONENT, 2.0f);
        glEnable(GL_LIGHT1);

        glEnable(GL_LIGHTING);
        //glEnable(GL_NORMALIZE); // "Довольно затратно, на практике использовать не стоит"?..

        // Упрощенный ускоренный вариант GL_NORMALIZE. Он подразумевает, что переданные в openGL нормали уже были нормализованы,
        //  но вы масштабировали матрицу трансформации (использовали glScale()).
        //  Работает верно только в тех случаях, когда матрица была масштабирована без искажений,
        //  то есть x, y и z, которые вы передали в glScale(), были равны.
        glEnable(GL_RESCALE_NORMAL);

        setColorMaterial();
    }

    protected void setColorMaterial() {
        /*
            Локальная точка зрения имеет тенденцию давать более реалистичные результаты, но поскольку направление необходимо вычислять
            для каждой вершины, при использовании локальной точки зрения общая производительность снижается. По умолчанию предполагается
            бесконечная точка обзора. Вот как можно перейти на локальную точку обзора:
        */
        // glLightModeli(GL_LIGHT_MODEL_LOCAL_VIEWER, GL_TRUE); // GL_FALSE

        /*
            Возможно, вам захочется, чтобы внутренняя поверхность была полностью освещена в соответствии с заданными условиями освещения;
            вы также можете указать другое описание материала для задних сторон При включении двустороннего освещения с помощью
        */
        // glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_TRUE); // GL_FALSE

        // glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambientLight);

        // glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, new float[] {0.5f, 0.6f, 0.4f, 0.75f});
        // glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuseLight);
        // glMaterialfv(GL_FRONT, GL_SPECULAR, ambientSpecular);
        // glMaterialfv(GL_FRONT, GL_SHININESS, new float[] {0.5f, 0.5f, 0.5f, 0.75f});
        // glMaterialfv(GL_FRONT, GL_EMISSION, new float[] {0.1f, 0.1f, 0.1f, 1.0f});

        // glMaterialf(GL_FRONT, GL_SHININESS, 128);
        // glMaterialf(GL_BACK, GL_SHININESS, 128);

        /*
         * GL_AMBIENT рассеянный свет GL_DIFFUSE тоже рассеянный свет, пояснения смотри ниже GL_SPECULAR отраженный свет GL_EMISSION
         * излучаемый свет GL_SHININESS степень отраженного света GL_AMBIENT_AND_DIFFUSE оба рассеянных света
         */
        // glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

        glEnable(GL_COLOR_MATERIAL);
    }

    protected void cullFace() {
        if (glIsEnabled(GL_CULL_FACE)) {
            return;
        }

        // настройка отображения передней и задней частей полигонов:
        // glPolygonMode(GL_FRONT, GL_FILL); // 0) GL_FRONT_AND_BACK | GL_FRONT | GL_BACK // 1) GL_POINT | GL_LINE | GL_FILL
        glPolygonMode(GL_BACK, GL_LINE); // 0) GL_FRONT_AND_BACK | GL_FRONT | GL_BACK // 1) GL_POINT | GL_LINE | GL_FILL

        // задаём ориентацию по часовой\против часовой:
        glFrontFace(GL_CCW); // GL_CW | GL_CCW

        // отсечение прямоугольников, обращенных от или скрытых от глаз:
        glCullFace(GL_BACK); // GL_FRONT, GL_BACK, или GL_FRONT_AND_BACK
        glEnable(GL_CULL_FACE);
    }

    protected void setDepth() {
        if (glIsEnabled(GL_DEPTH_TEST) && !glIsEnabled(GL_BLEND)) {
            return;
        }

        if (glIsEnabled(GL_BLEND)) {
            // glDisable(GL_BLEND); // не совсем ясно, надо или нет, но раскомментирование убивает прозрачность.
        }
        glDepthMask(true);
        glClearDepth(Constants.getUserConfig().getClearDepth());
        glDepthRange(Constants.getUserConfig().getZNear(), Constants.getUserConfig().getZFar());

        // glDepthFunc(GL_LESS);
        glDepthFunc(GL_LEQUAL);
        // glDepthFunc(GL_EQUAL);
        // glDepthFunc(GL_NOTEQUAL);
        // glDepthFunc(GL_GEQUAL);
        // glDepthFunc(GL_GREATER);
        // glDepthFunc(GL_ALWAYS);
        // glDepthFunc(GL_NEVER);

        // Буфер глубины или z-буфер используется для удаления невидимых линий и поверхностей:
        glEnable(GL_DEPTH_TEST);
    }

    protected void setBlend() {
        if (glIsEnabled(GL_BLEND)) {
            return;
        }

//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // "нормальное" смешивание. с сортировкой полигонов от ближнего к дальнему
//        glBlendFunc(GL_ONE, GL_ONE); // аддиктивное смешивание, сложение нового и старого цвета. Полезно для "энергетических" эффектов вроде огня и электричества.
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE); // то же самое, но с учетом прозрачности с текстуры. с сортировкой полигонов от ближнего к дальнему
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Включить альфа-смешение

        // Буфер глубины или z-буфер используется для удаления невидимых линий и поверхностей:
        if (glIsEnabled(GL_DEPTH_TEST)) {
            glDepthMask(false);
            glDisable(GL_DEPTH_TEST);
        }

        // glEnable(GL_ALPHA_TEST); // нужно?..
        glEnable(GL_BLEND);
    }

    protected void setFog() {
        if (glIsEnabled(GL_FOG)) {
            return;
        }

        final float[] fogcolor = {0.2f, 0.2f, 0.2f, 1.00f}; // цвет тумана

        glEnable(GL_FOG);
        glFogfv(GL_FOG_COLOR, fogcolor); // устанавливаем цвет тумана
        glFogf(GL_FOG_DENSITY, 0.75f);

//	      glHint(GL_FOG_HINT, GL_FASTEST);
//		  glHint(GL_FOG_HINT, GL_NICEST);
//        glHint(GL_FOG_HINT, GL_DONT_CARE);
    }

    protected void setAlphaTest() {
        if (glIsEnabled(GL_ALPHA_TEST)) {
            return;
        }

//		  glAlphaFunc(GL_ALWAYS, 0.33f);
//		  glAlphaFunc(GL_LESS, 0.50f);
//		  glAlphaFunc(GL_EQUAL, 1.00f);
//		  glAlphaFunc(GL_LEQUAL, 0.75f);
//		  glAlphaFunc(GL_GREATER, 0.75f);
//		  glAlphaFunc(GL_NOTEQUAL, 0.00f);
        glAlphaFunc(GL_GEQUAL, 0.25f);
//		  glAlphaFunc(GL_NEVER, 0.00f);

        glEnable(GL_ALPHA_TEST);
    }

    public void setAcceleration(boolean b) {
        this.isAccelerated = b;
    }

    public void setSneak(boolean b) {
        this.isSneaked = b;
        if (sneakThread != null && sneakThread.isAlive()) {
            sneakThread.interrupt();
        }
        if (isSneaked) {
            sneakThread = new Thread(() -> {
                while (heroHeight < -4 && !Thread.currentThread().isInterrupted()) {
                    try {
                        heroHeight += 0.1f;
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            sneakThread = new Thread(() -> {
                while (heroHeight > -6 && !Thread.currentThread().isInterrupted()) {
                    try {
                        heroHeight -= 0.1f;
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        sneakThread.start();
    }

    public void setZoom(boolean isZoomEnabled) {
        this.isZoomEnabled = isZoomEnabled;
    }

    // здесь вычисляется скорость передвижения героя по миру:
    protected float getHeroSpeed() {
        float heroSpeed = 0.085f;
//        heroSpeed = gameController.getCurrentHeroSpeed();
        return isAccelerated ? heroSpeed * accelerationMod : isSneaked ? heroSpeed * 0.5f : heroSpeed;
    }

    private void setInAc() {
        // когда физическая клавиша нажата или отпущена или когда она повторяется:
        glfwSetKeyCallback(getWindow(), (win, key, scancode, action, mods) -> {
            // Переключение в полноэкранный режим или в оконный:
            if (key == UserConfig.DefaultHotKeys.FULLSCREEN.getGlEvent() && action == GLFW_RELEASE) {
                Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
                windowManager.loadScreen(null);
            }

            if (type.equals(ScreenType.MENU_SCREEN) && key == UserConfig.DefaultHotKeys.PAUSE.getGlEvent() && action == GLFW_RELEASE) {
                gameController.showConfirmExitRequest(this);
            }

            // временная заглушка для теста смены сцен:
            if (key == GLFW_KEY_F1 && action == GLFW_RELEASE) {
                if (type.equals(ScreenType.MENU_SCREEN)) {
                    windowManager.loadScreen(ScreenType.GAME_SCREEN);
                } else if (type.equals(ScreenType.GAME_SCREEN)) {
                    windowManager.loadScreen(ScreenType.MENU_SCREEN);
                }
            }

            // просто жмём энтер для быстрого запуска последней игры:
            if (type.equals(ScreenType.MENU_SCREEN) && key == GLFW_KEY_ENTER && action == GLFW_RELEASE) {
                if (getHeroesListPane().isVisible()) {
                    playWithThisHero(gameController.getMyCurrentWorldHeroes().get(0));
                    getHeroesListPane().setVisible(false);
                } else if (getWorldsListPane().isVisible()) {
                    UUID lastWorldUid = gameController.getCurrentPlayerLastPlayedWorldUid();
                    if (gameController.isWorldExist(lastWorldUid)) {
                        chooseOrCreateHeroForWorld(lastWorldUid);
                    } else {
                        chooseOrCreateHeroForWorld(gameController.findAllWorldsByNetworkAvailable(false).get(0).getUid());
                    }
                } else {
                    getWorldsListPane().setVisible(true);
                }
            }

            // установка курсора:
            if (type.equals(ScreenType.GAME_SCREEN)) {
                // Если реализовать управление камерой на основе движения мыши, установите на GLFW_CURSOR_DISABLED.
                if (key == GLFW_KEY_F2 && action == GLFW_PRESS) {
                    glfwSetInputMode(getWindow(), GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursor(getWindow(), glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
                }
                if (key == GLFW_KEY_LEFT_ALT && action == GLFW_PRESS) {
                    Constants.setAltControlMode(true, win);
                }
                if (key == GLFW_KEY_LEFT_ALT && action == GLFW_RELEASE) {
                    Constants.setAltControlMode(false, win);
                }
            }

            if (type.equals(ScreenType.GAME_SCREEN)) {
                // Кнопки влево-вправо, вверх-вниз (камера):
                if (key == UserConfig.DefaultHotKeys.CAM_FORWARD.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_FORWARD.getGlEvent()) {
                    setCameraMovingForward(action != GLFW_RELEASE);
                } else if (key == UserConfig.DefaultHotKeys.CAM_BACK.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_BACK.getGlEvent()) {
                    setCameraMovingBack(action != GLFW_RELEASE);
                }
                if (key == UserConfig.DefaultHotKeys.CAM_LEFT.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_LEFT.getGlEvent()) {
                    setCameraMovingLeft(action != GLFW_RELEASE);
                } else if (key == UserConfig.DefaultHotKeys.CAM_RIGHT.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_RIGHT.getGlEvent()) {
                    setCameraMovingRight(action != GLFW_RELEASE);
                }
                if (key == UserConfig.DefaultHotKeys.CAM_LEFT.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_LEFT.getGlEvent()) {
                    setCameraMovingLeft(action != GLFW_RELEASE);
                } else if (key == UserConfig.DefaultHotKeys.CAM_RIGHT.getGlEvent() || key == UserConfig.DefaultHotKeys.MOVE_RIGHT.getGlEvent()) {
                    setCameraMovingRight(action != GLFW_RELEASE);
                }

                // бег\ускорение:
                if (key == UserConfig.DefaultHotKeys.ACCELERATION.getGlEvent()) {
                    setAcceleration(action != GLFW_RELEASE);
                }

                // приседание:
                if (key == UserConfig.DefaultHotKeys.SNEAK.getGlEvent()) {
                    setSneak(action != GLFW_RELEASE);
                }

                // зум:
                if (key == UserConfig.DefaultHotKeys.ZOOM.getGlEvent()) {
                    setZoom(action != GLFW_RELEASE);
                }
            }
        });

        glfwSetMouseButtonCallback(getWindow(), (long win, int button, int isPressed, int mod) -> {
//            log.info("win: {}, button: {}, isPressed: {}, mod: {}", win, button, isPressed, mod);
        });

        // уведомление, когда курсор перемещается по окну:
        glfwSetCursorPosCallback(getWindow(), (cursor, yaw, pitch) -> {
            if (Constants.isAltControlMode()) {
                return;
            }

            if (type.equals(ScreenType.GAME_SCREEN)) {
                // преобразуем координаты курсора в изменение от предыдущего значения:
                float curPitch = (float) (pitch - oldPitch);
                setCameraPitch(-curPitch);
                oldPitch = pitch;

                float curYaw = (float) (yaw - oldYaw);
                setCameraYaw(curYaw);
                oldYaw = yaw;
            }
        });

        // уведомления, когда пользователь прокручивает страницу, используя колесо мыши:
        glfwSetScrollCallback(getWindow(), (long win, double unknown, double direction) -> {
//            log.info("unknown: {}, direction: {}", unknown, direction);
        });

        // при закрытии окна игры:
        glfwSetWindowCloseCallback(getWindow(), (long win) -> {
            if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол без сохранения?",
                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
                gameController.setGlWindowBreaked(true);
                if (!glfwWindowShouldClose(getWindow())) {
                    glfwPostEmptyEvent();
                    glfwSetWindowShouldClose(getWindow(), true); // Закрывает окно

                    // Free the window callbacks and destroy the window
                    glfwFreeCallbacks(getWindow());
                    glfwDestroyWindow(getWindow());
                }
            } else {
                glfwSetWindowShouldClose(getWindow(), false); // Не закрывает окно :)
            }
        });

        // при сворачивании:
//        glfwSetWindowIconifyCallback(getWindow(), (long win, boolean isIconify) -> {
//            if (isIconify) {
//                onGameHide();
//            } else {
//                onGameRestore();
//            }
//        });

//        glfwSetWindowSizeCallback(getWindow(), (long win, int w, int h) -> {
//            Media.playSound("landing");
//            log.info("Размер окна был изменен на {}x{}", w, h);
//
//            onResize(currentScreen);
//        });

//        glfwSetWindowMaximizeCallback(getWindow(), (long win, boolean isMaximized) -> {
//            Media.playSound("landing");
//            log.info("Размер окна был {}", isMaximized ? "максимизирован." : "восстановлен.");
//        });

        // если игру надо приостанавливать во время обучения при всплывающих подсказках:
        glfwSetWindowFocusCallback(getWindow(), (long win, boolean focusState) -> {
            Media.playSound("touch");
            log.info("Фокус был {} на окно {}", focusState ? "(1)." : "(2).", win);
        });

        // уведомление, когда курсор входит или покидает область содержимого окна:
        // glfwSetCursorEnterCallback(window, курсор_enter_callback);

        // получать пути к файлам и/или каталогам, помещенным в окно (Функция обратного вызова получает массив путей в кодировке UTF-8):
        // glfwSetDropCallback(окно, drop_callback);
    }

    /**
     * После выбора мира - приходим сюда для создания нового героя или
     * выбора существующего, для игры в данном мире.
     *
     * @param worldUid uuid выбранного для игры мира.
     */
    public void chooseOrCreateHeroForWorld(UUID worldUid) {
        getWorldsListPane().setVisible(false);
        getWorldCreatingPane().setVisible(false);
        getNetworkListPane().setVisible(false);
        getNetworkCreatingPane().setVisible(false);

        gameController.setCurrentWorld(worldUid);
        if (gameController.getMyCurrentWorldHeroes().isEmpty()) {
            getHeroCreatingPane().setVisible(true);
        } else {
            getHeroesListPane().setVisible(true);
        }
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(HeroDTO hero) {
        gameController.setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());
        gameController.setCurrentHero(hero);

        // если этот мир по сети:
        if (gameController.isCurrentWorldIsNetwork()) {
            // шлем на Сервер своего выбранного Героя:
            if (gameController.registerCurrentHeroOnServer()) {
                gameController.getPlayedHeroesService().addHero(gameController.getCurrentHero());
                startGame();
            } else {
                log.error("Сервер не принял нашего Героя: {}", gameController.getLocalSocketConnection().getLastExplanation());
                gameController.setCurrentHeroOfflineAndSave(null);
                getHeroCreatingPane().repaint();
                getHeroesListPane().repaint();
            }
        } else {
            // иначе просто запускаем мир и играем локально:
            startGame();
        }
    }

    public void startGame() {
        getHeroCreatingPane().setVisible(false);
        getHeroesListPane().setVisible(false);

        log.info("Подготовка к запуску игры должна была пройти успешно. Запуск игрового мира...");
        gameController.loadScreen(ScreenType.GAME_SCREEN);
    }

    public void setCameraYaw(double yaw) {
        if (yaw != 0) {
            currentYaw += (float) (yaw * yawSpeed);
            if (currentYaw > 360) {
                currentYaw = 0;
            }
            if (currentYaw < 0) {
                currentYaw = 360;
            }
        }
    }

    public void setCameraPitch(double pitch) {
        if (pitch != 0) {
            currentPitch += (float) (pitch * pitchSpeed);
            if (currentPitch < 0) {
                currentPitch = 0;
            }
            if (currentPitch > 180) {
                currentPitch = 180;
            }
        }
    }

    public void moveCameraToHero() {
//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(heroXPos, heroYPos, -6);
    }

    public void moveHero() {
        glRotatef(-currentPitch, 1, 0, 0);
        glRotatef(currentYaw, 0, 0, 1);

        float ugol = (float) (currentYaw / 180f * Math.PI);
        setVelocity(isCameraMovingForward() ? getHeroSpeed() : isCameraMovingBack() ? -getHeroSpeed() : 0);
        if (isCameraMovingLeft()) {
            setVelocity(getHeroSpeed());
            ugol -= Math.PI * (isCameraMovingForward() ? 0.25 : isCameraMovingBack() ? 0.75 : 0.5);
        }
        if (isCameraMovingRight()) {
            setVelocity(getHeroSpeed());
            ugol += Math.PI * (isCameraMovingForward() ? 0.25 : isCameraMovingBack() ? 0.75 : 0.5);
        }

        if (getVelocity() != 0) {
            heroXPos += Math.sin(ugol) * getVelocity();
            heroYPos += Math.cos(ugol) * getVelocity();
        }

//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(-heroXPos, -heroYPos, getHeroHeight());
    }
}
