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
import game.freya.gl.Window;
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

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_FOG;
import static org.lwjgl.opengl.GL11.GL_FOG_COLOR;
import static org.lwjgl.opengl.GL11.GL_FOG_DENSITY;
import static org.lwjgl.opengl.GL11.GL_GEQUAL;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
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
import static org.lwjgl.opengl.GL11.glShadeModel;
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
public class FoxWindow extends Window {

    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;

    private final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;

    private final String pausedString, downInfoString1, downInfoString2;

    private final GameController gameController;

    private final AtomicBoolean isPingAwait = new AtomicBoolean(false);

    private final AtomicBoolean isConnectionAwait = new AtomicBoolean(false);

    private Rectangle2D viewPort;

    private Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;

    @Getter
    private Rectangle avatarRect, minimapRect, minimapShowRect, minimapHideRect;

    private BufferedImage pAvatar;

    private Polygon headerPoly;

    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane,
            heroesListPane, networkListPane, networkCreatingPane;

    private boolean firstButtonOver = false, firstButtonPressed = false, secondButtonOver = false, secondButtonPressed = false,
            thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;

    private boolean isOptionsMenuSetVisible = false;

    @Setter
    @Getter
    private volatile boolean isCameraMovingLeft = false, isCameraMovingRight = false, isCameraMovingForward = false,
            isCameraMovingBack = false, isCameraMovingUp = false, isCameraMovingDown = false;

    private WindowManager windowManager;

    private double oldPitch = 0, oldYaw = 0;

    private Chat chat;

    public FoxWindow(WindowManager windowManager, GameController controller) {
        super();

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

        setInAc();
    }

    private void drawMinimap(Graphics2D v2D) {
        // down left minimap:
        Rectangle mapButRect;
        if (Constants.isMinimapShowed()) {
            mapButRect = getMinimapShowRect();

//            updateMiniMap();

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
        long fullGameTime = inGamePlayed + (System.currentTimeMillis() - Constants.getGameStartedIn());
        Constants.setDuration(Duration.ofMillis(fullGameTime));
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
//        drawLeftGrayPoly(g2D);

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
        setWorldCreatingPane(new WorldCreatingPane(windowManager));
        setHeroCreatingPane(new HeroCreatingPane(this, gameController));
        setWorldsListPane(new WorldsListPane(windowManager, gameController));
        setHeroesListPane(new HeroesListPane(this, gameController));
        setNetworkListPane(new NetworkListPane(windowManager, gameController));
        setNetworkCreatingPane(new NetCreatingPane(this, gameController));

        // добавляем панели на слой:
        try {
            if (getAudiosPane() == null) {
                Thread.sleep(100);
            }
//            parentFrame.getContentPane().add(getAudiosPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getVideosPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getHotkeysPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getGameplayPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getHeroCreatingPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getWorldCreatingPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getWorldsListPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getHeroesListPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getNetworkListPane(), PALETTE_LAYER, 0);
//            parentFrame.getContentPane().add(getNetworkCreatingPane(), PALETTE_LAYER, 0);
        } catch (Exception e) {
            log.error("Ошибка при добавлении панелей на слой: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    protected void setSecondThread(String threadName, Thread secondThread) {
//        if (this.secondThread != null && this.secondThread.isAlive()) {
//            this.secondThread.interrupt();
//        }

//        this.secondThread = secondThread;
//        this.secondThread.setName(threadName);
//        this.secondThread.setDaemon(true);
    }

    protected boolean canDrag(MovingVector vector) {
        return switch (vector) {
            case UP -> viewPort.getHeight() < gameController.getCurrentWorldMap().getHeight();
            case UP_RIGHT -> false;
            case RIGHT -> viewPort.getX() > 0;
            case RIGHT_DOWN -> false;
            case DOWN -> viewPort.getY() > 0;
            case DOWN_LEFT -> false;
            case LEFT -> viewPort.getWidth() < gameController.getCurrentWorldMap().getWidth();
            case LEFT_UP -> false;
            case NONE -> false;
        };
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
        if (canDrag(MovingVector.LEFT)) {
            log.debug("Drag left...");
            double mapWidth = gameController.getCurrentWorldMap().getWidth();
            double newWidth = Math.min(getViewPort().getWidth() + pixels, mapWidth);
            getViewPort().setRect(getViewPort().getX() + pixels - (newWidth == mapWidth
                            ? Math.abs(getViewPort().getWidth() + pixels - mapWidth) : 0),
                    getViewPort().getY(), newWidth, getViewPort().getHeight());
        }
    }

    public void dragRight(double pixels) {
        if (canDrag(MovingVector.RIGHT)) {
            log.debug("Drag right...");
            double newX = getViewPort().getX() - pixels > 0 ? getViewPort().getX() - pixels : 0;
            getViewPort().setRect(newX, getViewPort().getY(),
                    getViewPort().getWidth() - pixels + (newX == 0 ? Math.abs(getViewPort().getX() - pixels) : 0), getViewPort().getHeight());
        }
    }

    public void dragUp(double pixels) {
        if (canDrag(MovingVector.UP)) {
            log.debug("Drag up...");
            double mapHeight = gameController.getCurrentWorldMap().getHeight();
            double newHeight = Math.min(getViewPort().getHeight() + pixels, mapHeight);
            getViewPort().setRect(getViewPort().getX(), getViewPort().getY() + pixels - (newHeight == mapHeight
                            ? Math.abs(getViewPort().getHeight() + pixels - mapHeight) : 0),
                    getViewPort().getWidth(), newHeight);
        }
    }

    public void dragDown(double pixels) {
        if (canDrag(MovingVector.DOWN)) {
            log.debug("Drag down...");
            double newY = getViewPort().getY() - pixels > 0 ? getViewPort().getY() - pixels : 0;
            getViewPort().setRect(getViewPort().getX(), newY, getViewPort().getWidth(),
                    getViewPort().getHeight() - pixels + (newY == 0 ? Math.abs(getViewPort().getY() - pixels) : 0));
        }
    }

    public void configureThis() {
        // обрезка области рисования:
        // Запрещает отрисовку за пределами указанной квадратной зоны на экране. Естественно, основное применение этой фичи - GUI
        //  (например, довольно сложно реализовать скроллящуюся панель без этой возможности).
        // Сопутствующая функция: glScissor(x, y, width, height).
        //  Координаты и размеры указываются в пикселях в окне, а не в том, что называется "пикселями" в ГУИ и на практике обычно
        //  оказывается больше реальных пикселей. Кроме того, ось Y идет снизу, а не сверху. Пример использования (запретить отрисовку
        //  за пределами квадрата 100х100 в верхнем левом углу экрана): glScissor(0, mc.displayHeight - 100, 100, 100);
//         glEnable(GL_SCISSOR_TEST);
//         glScissor(256, getHeight() - 256, 512, 128);

        setupTextures();
        setupCullFace();
        setupLights();
        setupColorMaterial();
        setupSmooth(); // интерполяция.
        setupBlend();
        setupDepth(); // буфер глубины (учёт расположения объектов в глубину псевдо-объема).
        setupFog();
        setupAlphaTest(); // учёт прозрачности.
    }

    private void setupTextures() {
        if (Constants.getGameConfig().isUseTextures()) {
            if (glIsEnabled(GL_TEXTURE_2D)) {
                return;
            }

            glEnable(GL_TEXTURE_2D); // включаем отображение текстур.
            glHint(GL_SAMPLES, 4);
            glEnable(GL_MULTISAMPLE);

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
    }

    private void setupSmooth() {
        if (Constants.getGameConfig().isSmoothEnabled()) {
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
        } else {
            setFlat();
        }
    }

    private void setFlat() {
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

    private static final float[] ambientLight = {0.5f, 0.5f, 0.5f, 1.0f}; // 0.0f, 0.0f, 0.3f, 1.0f

    private static final float[] ambientSpecular = {0.75f, 0.75f, 0.75f, 1.0f};

    private static final float[] ambientPosition = {0.0f, 0.5f, -2.0f, 1.0f}; // 31.84215f, 36.019997f, 28.262873f, 1.0f

    private static final float[] ambientDirection = {0.0f, 0.0f, 0.5f, 1.0f};

    private static final float[] ambientAttenuation = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffuseLight = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffusePosition = {0.5f, 0.5f, -1.5f, 1.0f};

    private static final float[] diffuseSpecular = {0.65f, 0.65f, 0.65f, 1.0f};

    private void setupLights() {
        if (Constants.getGameConfig().isLightsEnabled()) {
            if (glIsEnabled(GL_LIGHTING) && glIsEnabled(GL_COLOR_MATERIAL)) {
                return;
            }

            glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);
            glLightfv(GL_LIGHT0, GL_SPECULAR, ambientSpecular);
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

            setupColorMaterial();
        } else {
            // glDisable(GL_LIGHT1); // надо ли?
            // glDisable(GL_LIGHT0); // надо ли?
            glDisable(GL_LIGHTING);
            glDisable(GL_NORMALIZE); // надо ли?
        }
    }

    private void setupColorMaterial() {
        if (Constants.getGameConfig().isMaterialEnabled()) {
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
        } else {
            glDisable(GL_COLOR_MATERIAL);
        }
    }

    private void setupCullFace() {
        if (Constants.getGameConfig().isCullFaceGlEnabled()) {
            if (glIsEnabled(GL_CULL_FACE)) {
                return;
            }

            // настройка отображения передней и задней частей полигонов:
            // glPolygonMode(GL_FRONT, GL_FILL); // 0) GL_FRONT_AND_BACK | GL_FRONT | GL_BACK // 1) GL_POINT | GL_LINE | GL_FILL
            glPolygonMode(GL_BACK, GL_FILL); // 0) GL_FRONT_AND_BACK | GL_FRONT | GL_BACK // 1) GL_POINT | GL_LINE | GL_FILL

            // задаём ориентацию по часовой\против часовой:
            glFrontFace(GL_CCW); // GL_CW | GL_CCW

            // отсечение прямоугольников, обращенных от или скрытых от глаз:
            glCullFace(GL_BACK); // GL_FRONT, GL_BACK, или GL_FRONT_AND_BACK
            glEnable(GL_CULL_FACE);
        } else {
            glDisable(GL_CULL_FACE);
        }
    }

    private void setupDepth() {
        if (Constants.getGameConfig().isDepthEnabled()) {
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
        } else {
            glDisable(GL_DEPTH_TEST);
        }
    }

    private void setupBlend() {
        if (Constants.getGameConfig().isBlendEnabled()) {
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
        } else {
            setupDepth();
        }
    }

    private void setupFog() {
        if (Constants.getGameConfig().isUseFog()) {
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
        } else {
            glDisable(GL_FOG);
        }
    }

    private void setupAlphaTest() {
        if (Constants.getGameConfig().isUseAlphaTest()) {
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
        } else {
            glDisable(GL_ALPHA_TEST);
        }
    }

    private void setInAc() {
        glfwSetKeyCallback(getWindow(), (win, key, scancode, action, mods) -> onKeyAction(key, scancode, action, mods));
        glfwSetMouseButtonCallback(getWindow(), (long win, int button, int isPressed, int mod) -> onMouseAction(button, isPressed, mod));
        glfwSetCursorPosCallback(getWindow(), (cursor, yaw, pitch) -> onMouseMoving(yaw, pitch));
        glfwSetScrollCallback(getWindow(), (long win, double unknown, double direction) -> onWheelScrolling(direction));
        glfwSetWindowCloseCallback(getWindow(), (long win) -> onWindowsClosing());
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

    private void onWindowsClosing() {
        windowManager.exit();
    }

    private void onWheelScrolling(double direction) {

    }

    private void onMouseMoving(double yaw, double pitch) {
        if (Constants.isAltControlMode()) {
            return;
        }

        if (windowManager.isGameScreen()) {
            // преобразуем координаты курсора в изменение от предыдущего значения:
            float curPitch = (float) (pitch - oldPitch);
            gameController.setCameraPitch(-curPitch);
            oldPitch = pitch;

            float curYaw = (float) (yaw - oldYaw);
            gameController.setCameraYaw(curYaw);
            oldYaw = yaw;
        }
    }

    private void onMouseAction(int button, int isPressed, int mod) {

    }

    private void onKeyAction(int key, int scanCode, int action, int mods) {
        // Переключение в полноэкранный режим или в оконный:
        if (key == UserConfig.DefaultHotKeys.FULLSCREEN.getGlEvent() && action == GLFW_RELEASE) {
            Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
            windowManager.loadScreen(null);
        }

        if (windowManager.isMenuScreen() && key == UserConfig.DefaultHotKeys.PAUSE.getGlEvent() && action == GLFW_RELEASE) {
            windowManager.showConfirmExitRequest();
        }

        // временная заглушка для теста смены сцен:
        if (key == GLFW_KEY_F1 && action == GLFW_RELEASE) {
            if (windowManager.isMenuLoadingScreen()) {
                windowManager.loadScreen(ScreenType.MENU_SCREEN);
            } else if (windowManager.isMenuScreen()) {
                windowManager.loadScreen(ScreenType.GAME_LOADING_SCREEN);
            } else if (windowManager.isGameLoadingScreen()) {
                windowManager.loadScreen(ScreenType.GAME_SCREEN);
            } else if (windowManager.isGameScreen()) {
                windowManager.loadScreen(ScreenType.MENU_LOADING_SCREEN);
            }
        }

        // просто жмём энтер для быстрого запуска последней игры:
        if (windowManager.isMenuScreen() && key == GLFW_KEY_ENTER && action == GLFW_RELEASE) {
            if (getHeroesListPane().isVisible()) {
                gameController.playWithThisHero(gameController.getMyCurrentWorldHeroes().get(0));
                getHeroesListPane().setVisible(false);
            } else if (getWorldsListPane().isVisible()) {
                UUID lastWorldUid = gameController.getCurrentPlayerLastPlayedWorldUid();
                if (gameController.isWorldExist(lastWorldUid)) {
                    gameController.chooseOrCreateHeroForWorld(lastWorldUid);
                } else {
                    gameController.chooseOrCreateHeroForWorld(gameController.findAllWorldsByNetworkAvailable(false).get(0).getUid());
                }
            } else {
                getWorldsListPane().setVisible(true);
            }
        }

        if (windowManager.isGameScreen()) {
            // Если реализовать управление камерой на основе движения мыши, установите на GLFW_CURSOR_DISABLED.
            if (key == GLFW_KEY_F2 && action == GLFW_PRESS) {
                glfwSetInputMode(getWindow(), GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                glfwSetCursor(getWindow(), glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
            } else if (key == GLFW_KEY_LEFT_ALT) {
                Constants.setAltControlMode(action != GLFW_RELEASE, getWindow());
            }

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
                gameController.setAcceleration(action != GLFW_RELEASE);
            }

            // приседание:
            if (key == UserConfig.DefaultHotKeys.SNEAK.getGlEvent()) {
                gameController.setSneak(action != GLFW_RELEASE);
            }

            // зум:
            if (key == UserConfig.DefaultHotKeys.ZOOM.getGlEvent()) {
                gameController.setZoom(action != GLFW_RELEASE);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getAudiosPane().setVisible(true);
                getVideosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);

            } else {
                Constants.setPaused(false);
                setOptionsMenuSetVisible(false);
            }
        }
        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getVideosPane().setVisible(true);
                getAudiosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            }
        }
        if (isThirdButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getHotkeysPane().setVisible(true);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                // нет нужды в паузе здесь, просто сохраняемся:
                gameController.justSave();
                Constants.setPaused(false);
                new FOptionPane().buildFOptionPane("Успешно", "Игра сохранена!",
                        FOptionPane.TYPE.INFO, null, Constants.getDefaultCursor(), 3, false);
            }
        }
        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getGameplayPane().setVisible(true);
                getHotkeysPane().setVisible(false);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
            } else {
                Constants.showNFP();
            }
        }
        if (isExitButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                onExitBack();
            } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти в главное меню?",
                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
                windowManager.stopGame();
            }
        }

        if (gameController.isGameActive()) {
            if (getMinimapShowRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(false);
            }
            if (getMinimapHideRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(true);
            }
        }
    }

    public void startGame() {
        getHeroCreatingPane().setVisible(false);
        getHeroesListPane().setVisible(false);

        log.info("Подготовка к запуску игры должна была пройти успешно. Запуск игрового мира...");
        windowManager.loadScreen(ScreenType.GAME_SCREEN);
    }

    public void mouseReleased() {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getAudiosPane().isVisible()) {
                    getAudiosPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getWorldsListPane().isVisible()) {
                getWorldsListPane().setVisible(false);
                getWorldCreatingPane().setVisible(true);
            } else if (getHeroesListPane().isVisible()) {
                gameController.openCreatingNewHeroPane(null);
            } else if (getNetworkListPane().isVisible()) {
                getNetworkListPane().setVisible(false);
                getNetworkCreatingPane().setVisible(true);
            } else {
                if (gameController.findAllWorldsByNetworkAvailable(false).isEmpty()) {
                    getWorldCreatingPane().setVisible(true);
                } else {
                    getWorldsListPane().setVisible(true);
                }
            }
        }

        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                // нажато Настройки графики:
                if (!getVideosPane().isVisible()) {
                    getVideosPane().setVisible(true);
                    getAudiosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getNetworkListPane().isVisible()) {
                ((NetworkListPane) getNetworkListPane()).reloadNet();
            } else {
                getNetworkListPane().setVisible(true);
            }
        }

        if (isThirdButtonOver()) {
            if (!isOptionsMenuSetVisible() && !getHeroCreatingPane().isVisible() && !getWorldsListPane().isVisible()) {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (isOptionsMenuSetVisible()) {
                if (!getHotkeysPane().isVisible()) {
                    getHotkeysPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getGameplayPane().isVisible()) {
                    getGameplayPane().setVisible(true);
                    getHotkeysPane().setVisible(false);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isExitButtonOver()) {
            onExitBack();
        }
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
    }

    @Override
    protected void onGameRestore() {
        if (gameController.isCurrentWorldIsNetwork()) {
            return;
        }

        if (Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(false);
            log.debug("Resume game...");
        }
    }

    @Override
    protected void onGameHide() {
        if (gameController.isCurrentWorldIsNetwork()) {
            return;
        }

        log.debug("Hide or minimized");
        if (!Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.debug("Paused...");
        }
    }

    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public boolean isConnectionAwait() {
        return isConnectionAwait.get();
    }

    public void setConnectionAwait(boolean b) {
        isConnectionAwait.set(b);
    }

    public boolean isPingAwait() {
        return isPingAwait.get();
    }

    public void setPingAwait(boolean b) {
        isPingAwait.set(b);
    }

    public void createChat() {
        this.chat = new Chat(new Point(getWidth() - getWidth() / 5 - 9, 64),
                new Dimension(getWidth() / 5, getHeight() / 4));
    }
}
