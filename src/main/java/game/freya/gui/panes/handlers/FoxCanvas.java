package game.freya.gui.panes.handlers;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.interfaces.iCanvas;
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
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.JLayeredPane.PALETTE_LAYER;

@Getter
@Setter
@Slf4j
// iCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable
public abstract class FoxCanvas extends Canvas implements iCanvas {
    public static final long SECOND_THREAD_SLEEP_MILLISECONDS = 333;

    private static final AtomicInteger frames = new AtomicInteger(0);
    private static final short rightShift = 21;
    private static long timeStamp = System.currentTimeMillis();
    @Getter
    private static byte drawErrorCount = 0;

    private final String name;
    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;
    private final String pausedString, downInfoString1, downInfoString2;
    private final transient GameController gameController;
    private final transient JLayeredPane parentLayers;
    private final transient UIHandler uiHandler;
    private transient Thread secondThread;
    private transient Rectangle2D viewPort;
    private transient Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;
    private transient Rectangle avatarRect;
    private transient BufferedImage pAvatar;
    private transient VolatileImage backImage;
    private transient Polygon leftGrayMenuPoly;
    private transient Polygon headerPoly;
    private transient Duration duration;
    private transient Area area;
    private transient JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane,
            heroesListPane, networkListPane, networkCreatingPane;
    private float downShift = 0;
    private boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;
    private boolean revolatileNeeds = false, isOptionsMenuSetVisible = false, isConnectionAwait = false;

    protected FoxCanvas(GraphicsConfiguration gConf, String name, GameController controller, JLayeredPane layeredPane, UIHandler uiHandler) {
        super(gConf);
        this.name = name;
        this.uiHandler = uiHandler;
        this.parentLayers = layeredPane;
        this.gameController = controller;

        this.audioSettingsButtonText = "Настройки звука";
        this.videoSettingsButtonText = "Настройки графики";
        this.hotkeysSettingsButtonText = "Управление";
        this.gameplaySettingsButtonText = "Геймплей";
        this.backButtonText = "← Назад";
        this.exitButtonText = "← Выход";

        this.backToGameButtonText = "Вернуться";
        this.optionsButtonText = "Настройки";
        this.saveButtonText = "Сохранить";

        this.downInfoString1 = controller.getGameConfig().getAppCompany();
        this.downInfoString2 = controller.getGameConfig().getAppName().concat(" v.").concat(controller.getGameConfig().getAppVersion());

        this.pausedString = "- PAUSED -";
    }

    public void incrementFramesCounter() {
        frames.incrementAndGet();
    }

    public void drawDebugInfo(Graphics2D g2D) {
        this.drawDebugInfo(g2D, null);
    }

    public void drawDebugInfo(Graphics2D g2D, String worldTitle) {
        if (Constants.isDebugInfoVisible()) {
            drawDebug(g2D, worldTitle);
        }

        if (Constants.isFpsInfoVisible()) {
            drawFps(g2D);
        }
    }

    private void drawDebug(Graphics2D g2D, String worldTitle) {
        g2D.setFont(Constants.DEBUG_FONT);

        if (worldTitle != null) {
            String pass = duration != null ? LocalDateTime.of(0, 1, (int) (duration.toDaysPart() + 1),
                            duration.toHoursPart(), duration.toMinutesPart(), 0, 0)
                    .format(Constants.DATE_FORMAT_2) : "=na=";

            g2D.setColor(Color.BLACK);
            g2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + 22);
            g2D.drawString("В игре: %s".formatted(pass), rightShift - 1f, downShift + 43);

            g2D.setColor(Color.GRAY);
            g2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + 21);
            g2D.drawString("В игре: %s".formatted(pass), rightShift, downShift + 42);
        }

        final int leftShift = 340;
        g2D.setColor(Color.GRAY);

        // graphics info:
        BufferCapabilities gCap = getBufferStrategy().getCapabilities();
        g2D.drawString("Front accelerated: " + gCap.getFrontBufferCapabilities().isAccelerated(),
                getWidth() - leftShift, getHeight() - 350);
        g2D.drawString("Front is true volatile: " + gCap.getFrontBufferCapabilities().isTrueVolatile(),
                getWidth() - leftShift, getHeight() - 330);
        g2D.drawString("Back accelerated: " + gCap.getBackBufferCapabilities().isAccelerated(),
                getWidth() - leftShift, getHeight() - 305);
        g2D.drawString("Back is true volatile: " + gCap.getBackBufferCapabilities().isTrueVolatile(),
                getWidth() - leftShift, getHeight() - 285);
        g2D.drawString("Fullscreen required: " + gCap.isFullScreenRequired(), getWidth() - leftShift, getHeight() - 260);
        g2D.drawString("Multi-buffer available: " + gCap.isMultiBufferAvailable(), getWidth() - leftShift, getHeight() - 240);
        g2D.drawString("Is page flipping: " + gCap.isPageFlipping(), getWidth() - leftShift, getHeight() - 220);

        // server info:
        g2D.setColor(gameController.isServerIsOpen() ? Color.GREEN : Color.DARK_GRAY);
        g2D.drawString("Server open: " + gameController.isServerIsOpen(), getWidth() - leftShift, getHeight() - 190);
        g2D.drawString("Connected players: " + gameController.getConnectedPlayersCount(), getWidth() - leftShift, getHeight() - 170);

        // hero info:
        Point2D.Double playerPos = gameController.getCurrentHeroPosition();
        if (playerPos != null) {
            Shape playerShape = new Ellipse2D.Double(
                    (int) playerPos.x - Constants.MAP_CELL_DIM / 2d,
                    (int) playerPos.y - Constants.MAP_CELL_DIM / 2d,
                    Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);
            g2D.setColor(Color.GRAY);
            g2D.drawString("Hero pos: " + playerShape.getBounds2D().getCenterX() + "x" + playerShape.getBounds2D().getCenterY(),
                    getWidth() - leftShift, getHeight() - 140);
        }

        g2D.drawString("Hero speed: " + gameController.getCurrentHeroSpeed(), getWidth() - leftShift, getHeight() - 120);

        // gameplay info:
        if (Constants.isFpsInfoVisible()) {
            g2D.setColor(Constants.isLowFpsAlarm() ? Color.RED : Color.GRAY);
            g2D.drawString("Delay fps: " + Constants.getDelay(), getWidth() - leftShift, getHeight() - 90);
            g2D.setColor(Color.GRAY);
        }

        if (gameController.getCurrentWorldMap() != null) {
            g2D.drawString("GameMap WxH: " + gameController.getCurrentWorldMap().getWidth() + "x" + gameController.getCurrentWorldMap().getHeight(),
                    getWidth() - leftShift, getHeight() - 70);
        }

        g2D.drawString("Canvas XxY-WxH: " + getBounds().x + "x" + getBounds().y + "-"
                + getBounds().width + "x" + getBounds().height, getWidth() - leftShift, getHeight() - 50);

        if (viewPort != null) {
            g2D.drawString("ViewPort XxY-WxH: " + viewPort.getBounds().x + "x" + viewPort.getBounds().y + "-"
                    + viewPort.getBounds().width + "x" + viewPort.getBounds().height, getWidth() - leftShift, getHeight() - 30);
        }

        if (Constants.isPaused()) {
            // draw menus area border:
            if (area == null || area.getBounds().getHeight() != getBounds().getHeight()) {
                area = new Area(getBounds());
                area.subtract(new Area(this.leftGrayMenuPoly));
            }
            g2D.setColor(Color.RED);
            g2D.draw(area);
        }
    }

    private void drawFps(Graphics2D g2D) {
        // FPS check:
        incrementFramesCounter();
        if (System.currentTimeMillis() >= timeStamp + 1000L) {
            resetFpsCounter();
        }

        if (downShift == 0) {
            downShift = getHeight() * 0.14f;
        }
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getFpsLimit(), Constants.MON.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift - 1f, downShift + 1f);

        if (Constants.isLowFpsAlarm()) {
            g2D.setColor(Color.RED);
        } else {
            g2D.setColor(Color.GRAY);
        }
        g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getFpsLimit(), Constants.MON.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift, downShift);
    }

    private void resetFpsCounter() {
        Constants.setCurrentFreshRate(frames.get());
        timeStamp = System.currentTimeMillis();
        frames.set(0);
    }

    public void reloadShapes(FoxCanvas canvas) {
        downShift = getHeight() * 0.14f;

        setLeftGrayMenuPoly(new Polygon(
                new int[]{0, (int) (canvas.getBounds().getWidth() * 0.25D), (int) (canvas.getBounds().getWidth() * 0.2D), 0},
                new int[]{0, 0, canvas.getHeight(), canvas.getHeight()},
                4));

        setHeaderPoly(new Polygon(
                new int[]{0, (int) (canvas.getWidth() * 0.3D), (int) (canvas.getWidth() * 0.29D), (int) (canvas.getWidth() * 0.3D), 0},
                new int[]{3, 3, (int) (canvas.getHeight() * 0.031D), (int) (canvas.getHeight() * 0.061D), (int) (canvas.getHeight() * 0.061D)},
                5));
    }

    public void recalculateMenuRectangles() {
        int buttonsRectsWidth = (int) (getWidth() * 0.14D);
        // стандартное меню:
        firstButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.15D),
                buttonsRectsWidth, 30);
        secondButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.20D),
                buttonsRectsWidth, 30);
        thirdButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.25D),
                buttonsRectsWidth, 30);
        fourthButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.30D),
                buttonsRectsWidth, 30);
        exitButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.85D),
                buttonsRectsWidth, 30);

        avatarRect = new Rectangle(getWidth() - 135, 8, 128, 128);
    }

    public void checkGameplayDuration(long inGamePlayed) {
        this.duration = Duration.ofMillis(inGamePlayed + (System.currentTimeMillis() - Constants.getGameStartedIn()));
    }

    public void drawHeader(Graphics2D g2D, String headerTitle) {
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

    public void showOptions(Graphics2D g2D) {
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

    public void drawLeftGrayPoly(Graphics2D g2D) {
        // fill left gray polygon:
        g2D.setColor(isOptionsMenuSetVisible() ? Constants.getMainMenuBackgroundColor2() : Constants.getMainMenuBackgroundColor());
        g2D.fillPolygon(getLeftGrayMenuPoly());
    }

    public int validateBackImage() {
        return this.backImage.validate(Constants.getGraphicsConfiguration());
    }

    public void closeBackImage() {
        this.backImage.flush();
        this.backImage.getGraphics().dispose();
    }

    public void onExitBack(FoxCanvas canvas) {
        if (isOptionsMenuSetVisible()) {
            setOptionsMenuSetVisible(false);
            audiosPane.setVisible(false);
            videosPane.setVisible(false);
            hotkeysPane.setVisible(false);
            gameplayPane.setVisible(false);
        } else if (audiosPane.isVisible()) {
            audiosPane.setVisible(false);
        } else if (videosPane.isVisible()) {
            videosPane.setVisible(false);
        } else if (hotkeysPane.isVisible()) {
            hotkeysPane.setVisible(false);
        } else if (gameplayPane.isVisible()) {
            gameplayPane.setVisible(false);
        } else if (heroCreatingPane.isVisible()) {
            heroCreatingPane.setVisible(false);
            heroesListPane.setVisible(true);
            return;
        } else if (worldCreatingPane.isVisible()) {
            worldCreatingPane.setVisible(false);
            worldsListPane.setVisible(true);
            return;
        } else if (worldsListPane.isVisible()) {
            worldsListPane.setVisible(false);
        } else if (heroesListPane.isVisible()) {
            heroesListPane.setVisible(false);
        } else if (networkListPane.isVisible()) {
            networkListPane.setVisible(false);
        } else if (networkCreatingPane.isVisible()) {
            networkCreatingPane.setVisible(false);
            networkListPane.setVisible(true);
            return;
        } else if (canvas instanceof MenuCanvas mCanvas
                && (int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
            mCanvas.exitTheGame();
        } else if (canvas instanceof GameCanvas) {
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

    private void hidePanelIfNotNull(JPanel panel) {
        if (panel != null) {
            panel.setVisible(false);
        }
    }

    public boolean isShadowBackNeeds() {
        return isOptionsMenuSetVisible
                || (heroCreatingPane != null && heroCreatingPane.isVisible())
                || (worldCreatingPane != null && worldCreatingPane.isVisible())
                || (worldsListPane != null && worldsListPane.isVisible())
                || (heroesListPane != null && heroesListPane.isVisible())
                || (networkListPane != null && networkListPane.isVisible())
                || (networkCreatingPane != null && networkCreatingPane.isVisible());
    }

    public void drawAvatar(Graphics2D g2D) {
        if (pAvatar == null) {
            pAvatar = gameController.getCurrentPlayerAvatar();
        }
        g2D.setColor(Color.BLACK);
        g2D.setStroke(new BasicStroke(5f));
        g2D.drawImage(pAvatar, getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, this);
        g2D.drawRoundRect(getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, 16, 16);
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.drawString(gameController.getCurrentPlayerNickName(),
                (int) (getAvatarRect().getCenterX() - Constants.FFB.getHalfWidthOfString(g2D, gameController.getCurrentPlayerNickName())),
                getAvatarRect().height + 24);
    }

    public void recreateSubPanes() {
        // удаляем старые панели с фрейма:
        dropOldPanesFromLayer();

        // создаём новые панели:
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
            parentLayers.add(getAudiosPane(), PALETTE_LAYER);
            parentLayers.add(getVideosPane(), PALETTE_LAYER);
            parentLayers.add(getHotkeysPane(), PALETTE_LAYER);
            parentLayers.add(getGameplayPane(), PALETTE_LAYER);
            parentLayers.add(getHeroCreatingPane(), PALETTE_LAYER);
            parentLayers.add(getWorldCreatingPane(), PALETTE_LAYER);
            parentLayers.add(getWorldsListPane(), PALETTE_LAYER);
            parentLayers.add(getHeroesListPane(), PALETTE_LAYER);
            parentLayers.add(getNetworkListPane(), PALETTE_LAYER);
            parentLayers.add(getNetworkCreatingPane(), PALETTE_LAYER);
        } catch (Exception e) {
            log.error("Ошибка при добавлении панелей на слой: {}", ExceptionUtils.getFullExceptionMessage(e));
            recreateSubPanes();
        }
    }

    public void dropOldPanesFromLayer() {
        try {
            parentLayers.remove(getAudiosPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма audiosPane, которой там нет.");
        }
        try {
            parentLayers.remove(getVideosPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма videosPane, которой там нет.");
        }
        try {
            parentLayers.remove(getHotkeysPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма hotkeysPane, которой там нет.");
        }
        try {
            parentLayers.remove(getGameplayPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма gameplayPane, которой там нет.");
        }
        try {
            parentLayers.remove(getHeroCreatingPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма heroCreatingPane, которой там нет.");
        }
        try {
            parentLayers.remove(getWorldCreatingPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма worldCreatingPane, которой там нет.");
        }
        try {
            parentLayers.remove(getWorldsListPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма worldsListPane, которой там нет.");
        }
        try {
            parentLayers.remove(getHeroesListPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма heroesListPane, которой там нет.");
        }
        try {
            parentLayers.remove(getNetworkListPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма networkListPane, которой там нет.");
        }
        try {
            parentLayers.remove(getNetworkCreatingPane());
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма networkCreatingPane, которой там нет.");
        }
    }

    protected void drawUI(Graphics2D g2D) {
        if (isShadowBackNeeds()) {
            g2D.setColor(new Color(0, 0, 0, 223));
            g2D.fillRect(0, 0, getWidth(), getHeight());
        }
        uiHandler.drawUI(this, g2D);
    }

    public void decreaseDrawErrorCount() {
        drawErrorCount--;
    }

    public void increaseDrawErrorCount() {
        drawErrorCount++;
    }

    public void setSecondThread(String threadName, Thread secondThread) {
        if (this.secondThread != null && this.secondThread.isAlive()) {
            this.secondThread.interrupt();
        }

        this.secondThread = secondThread;
        this.secondThread.setName(threadName);
        this.secondThread.setDaemon(true);
    }
}
