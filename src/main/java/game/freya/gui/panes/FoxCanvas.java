package game.freya.gui.panes;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.gui.panes.interfaces.iCanvas;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@Slf4j
// iCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable
public abstract class FoxCanvas extends Canvas implements iCanvas {
    private static final short rightShift = 21;

    private final String name;
    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;
    private final String pausedString, downInfoString1, downInfoString2;
    private transient VolatileImage backImage;
    private transient Rectangle avatarRect;
    private transient Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;
    private transient Polygon leftGrayMenuPoly;
    private transient Polygon headerPoly;
    private transient Duration duration;
    private float downShift = 0;
    private long timeStamp = System.currentTimeMillis();
    private int frames = 0;

    private boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;
    private boolean isOptionsMenuSetVisible = false, isCreatingNewHeroSetVisible = false, isCreatingNewWorldSetVisible = false,
            isChooseWorldMenuVisible = false, isChooseHeroMenuVisible = false, isNetworkMenuVisible = false, isCreatingNewNetworkVisible = false;
    private boolean isAudioSettingsMenuVisible = false, isVideoSettingsMenuVisible = false, isHotkeysSettingsMenuVisible = false,
            isGameplaySettingsMenuVisible = false;
    private boolean revolatileNeeds = false;

    protected FoxCanvas(GraphicsConfiguration gConf, String name, GameController controller) {
        super(gConf);
        this.name = name;

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
        this.frames++;
    }

    public void drawDebugInfo(Graphics2D g2D, String worldTitle) {
        if (Constants.isDebugInfoVisible()) {
            incrementFramesCounter();

            if (System.currentTimeMillis() - this.timeStamp >= 1000L) {
                Constants.setRealFreshRate(this.frames);
                this.timeStamp = System.currentTimeMillis();
                this.frames = 0;
            }

            if (worldTitle != null) {
                String pass = LocalDateTime.of(0, 1, (int) (duration.toDaysPart() + 1),
                                duration.toHoursPart(), duration.toMinutesPart(), 0, 0)
                        .format(Constants.DATE_FORMAT_2);

                g2D.setFont(Constants.DEBUG_FONT);
                g2D.setColor(Color.BLACK);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + 22);
                g2D.drawString("В игре: %s".formatted(pass), rightShift - 1f, downShift + 43);

                g2D.setColor(Color.GRAY);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + 21);
                g2D.drawString("В игре: %s".formatted(pass), rightShift, downShift + 42);
            }

            if (downShift == 0) {
                downShift = getHeight() * 0.14f;
            }
            g2D.setFont(Constants.DEBUG_FONT);
            g2D.setColor(Color.BLACK);
            g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                    .formatted(Constants.getUserConfig().getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), rightShift - 1f, downShift + 1f);

            if (Constants.isLowFpsAlarm()) {
                g2D.setColor(Color.RED);
            } else {
                g2D.setColor(Color.GRAY);
            }
            g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                    .formatted(Constants.getUserConfig().getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), rightShift, downShift);
        }
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

        log.info("Новый серый меню размер: {}", getLeftGrayMenuPoly().getBounds());
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
}
