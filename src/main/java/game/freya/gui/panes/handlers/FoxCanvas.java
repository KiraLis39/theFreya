package game.freya.gui.panes.handlers;

import fox.FoxRender;
import fox.components.FOptionPane;
import fox.utils.FoxPointConverterUtil;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.dto.roots.CharacterDTO;
import game.freya.enums.player.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.interfaces.iCanvas;
import game.freya.gui.panes.interfaces.iSubPane;
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
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static game.freya.config.Constants.FFB;
import static game.freya.config.Constants.ONE_TURN_PI;
import static javax.swing.JLayeredPane.PALETTE_LAYER;

@Getter
@Setter
@Slf4j
// iCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable
public abstract class FoxCanvas extends JPanel implements iCanvas {
    public static final long SECOND_THREAD_SLEEP_MILLISECONDS = 250;

    private static final AtomicInteger frames = new AtomicInteger(0);

    private static final short rightShift = 21;

    private static final double infoStrut = 58d, infoStrutHardness = 40d;

    private static final int minimapDim = 2048;

    private static final int halfDim = (int) (minimapDim / 2d);

    private static long timeStamp = System.currentTimeMillis();

    private final String name;

    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;

    private final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;

    private final String pausedString, downInfoString1, downInfoString2;

    private final transient GameControllerService gameController;

    private final transient UIHandler uiHandler;

    private final Color grayBackColor = new Color(0, 0, 0, 223);

    private final AtomicBoolean isConnectionAwait = new AtomicBoolean(false);

    private final AtomicBoolean isPingAwait = new AtomicBoolean(false);

    private AtomicInteger drawErrors = new AtomicInteger(0);

    private transient Thread secondThread;

    private transient Rectangle2D viewPort;

    private transient Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;

    @Getter
    private transient Rectangle avatarRect, minimapRect, minimapShowRect, minimapHideRect;

    private transient BufferedImage pAvatar;

    private transient VolatileImage backImage, minimapImage;

    private transient Polygon leftGrayMenuPoly;

    private transient Polygon headerPoly;

    private transient Duration duration;

    private transient JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane,
            heroesListPane, networkListPane, networkCreatingPane;

    private float downShift = 0;

    private boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;

    private boolean revolatileNeeds = false, isOptionsMenuSetVisible = false;

    private transient Chat chat;

    private transient JFrame parentFrame;

    private byte creatingSubsRetry = 0;

    protected FoxCanvas(String name, GameControllerService controller, JFrame parentFrame, UIHandler uiHandler) {
        super(null, true);

        this.name = name;
        this.uiHandler = uiHandler;
        this.parentFrame = parentFrame;
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

        setForeground(Color.BLACK);
    }

    public void incrementFramesCounter() {
        frames.incrementAndGet();
    }

    protected void drawBackground(Graphics2D bufGraphics2D) throws AWTException {
        Graphics2D v2D = getValidVolatileGraphic();
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.MED,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

        if (getName().equals("GameCanvas")) {
            repaintGame(v2D);
        } else {
            repaintMenu(v2D);
        }

        Constants.RENDER.setRender(v2D, FoxRender.RENDER.MED,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        drawUI(v2D, getName());

        Constants.RENDER.setRender(v2D, FoxRender.RENDER.LOW,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        drawDebugInfo(v2D, gameController.getCurrentWorldTitle());

        if (Constants.isFpsInfoVisible()) {
            drawFps(v2D);
        }

        v2D.dispose();

        // draw accomplished volatile image:
        bufGraphics2D.drawImage(this.backImage, 0, 0, this);
    }

    private Graphics2D getValidVolatileGraphic() throws AWTException {
        if (this.backImage == null) {
            this.backImage = createVolatileImage(getWidth(), getHeight(), new ImageCapabilities(true));
        }

        if (isRevolatileNeeds()) {
            this.backImage = createVolatileImage(getWidth(), getHeight(), new ImageCapabilities(true));
            setRevolatileNeeds(false);
        }

        while (validateBackImage() == VolatileImage.IMAGE_INCOMPATIBLE) {
            log.debug("Recreating new volatile image by incompatible...");
            this.backImage = createVolatileImage(getWidth(), getHeight(), new ImageCapabilities(true));
        }

        if (validateBackImage() == VolatileImage.IMAGE_RESTORED) {
            log.debug("Awaits while volatile image is restored...");
            return this.backImage.createGraphics();
        } else {
            return (Graphics2D) this.backImage.getGraphics();
        }
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

    private void repaintMenu(Graphics2D v2D) {
        v2D.drawImage(isShadowBackNeeds() ? Constants.CACHE.getBufferedImage("backMenuImageShadowed") : Constants.CACHE.getBufferedImage("backMenuImage"),
                0, 0, getWidth(), getHeight(), this);

        drawLeftGrayPoly(v2D);

        if (!isShadowBackNeeds()) {
            drawAvatar(v2D);
        }
    }

    private void repaintGame(Graphics2D v2D) throws AWTException {
        // рисуем мир:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.HIGH,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        gameController.getDrawCurrentWorld(v2D);

        // рисуем данные героев поверх игры:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.OFF);
        drawHeroesData(v2D);

        // рисуем миникарту:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.OFF);
        drawMinimap(v2D);

        Constants.RENDER.setRender(v2D, FoxRender.RENDER.HIGH, true, true);
        if (Constants.isPaused()) {
            drawPauseMode(v2D);
        }
    }

    private void drawMinimap(Graphics2D v2D) throws AWTException {
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
                    v2D.drawImage(this.minimapImage, getMinimapRect().x, getMinimapRect().y,
                            getMinimapRect().width, getMinimapRect().height, this);
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

    private void updateMiniMap() throws AWTException {
        Point2D.Double myPos = gameController.getCurrentHeroPosition();
        MovingVector cVector = gameController.getCurrentHeroVector();
        int srcX = (int) (myPos.x - halfDim);
        int srcY = (int) (myPos.y - halfDim);

        Graphics2D m2D;
        if (minimapImage == null || minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            log.info("Recreating new minimap volatile image by incompatible...");
            minimapImage = createVolatileImage(minimapDim, minimapDim, new ImageCapabilities(true));
        }
        if (minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_RESTORED) {
            log.info("Awaits while minimap volatile image is restored...");
            m2D = this.minimapImage.createGraphics();
        } else {
            m2D = (Graphics2D) this.minimapImage.getGraphics();
            m2D.clearRect(0, 0, minimapImage.getWidth(), minimapImage.getHeight());
        }

        // draw minimap:
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

//        v2D.setColor(backColor);
//        v2D.fillRect(0, 0, camera.width, camera.height);

        // отображаем себя на миникарте:
        AffineTransform grTrMem = m2D.getTransform();
        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
        m2D.drawImage(Constants.CACHE.getBufferedImage("green_arrow"), halfDim - 64, halfDim - 64, 128, 128, null);
        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (CharacterDTO connectedHero : gameController.getConnectedHeroes()) {
            if (gameController.getCurrentHeroUid().equals(connectedHero.getUid())) {
                continue;
            }
            int otherHeroPosX = (int) (halfDim - (myPos.x - connectedHero.getLocation().x));
            int otherHeroPosY = (int) (halfDim - (myPos.y - connectedHero.getLocation().y));
//            log.info("Рисуем игрока {} в точке миникарты {}x{}...", connectedHero.getHeroName(), otherHeroPosX, otherHeroPosY);
            m2D.setColor(connectedHero.getBaseColor());
            m2D.fillRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
            m2D.setColor(connectedHero.getSecondColor());
            m2D.drawRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
        }

        if (gameController.getCurrentWorldMap() != null) {
            // сканируем все сущности указанного квадранта:
            Rectangle scanRect = new Rectangle(
                    Math.min(Math.max(srcX, 0), gameController.getCurrentWorldMap().getWidth() - minimapDim),
                    Math.min(Math.max(srcY, 0), gameController.getCurrentWorldMap().getHeight() - minimapDim),
                    minimapDim, minimapDim);

            m2D.setColor(Color.CYAN);
            gameController.getWorldEnvironments(scanRect)
                    .forEach(entity -> {
                        int otherHeroPosX = (int) (halfDim - (myPos.x - entity.getCenterPoint().x));
                        int otherHeroPosY = (int) (halfDim - (myPos.y - entity.getCenterPoint().y));
                        m2D.fillRect(otherHeroPosX - 16, otherHeroPosY - 16, 32, 32);
                    });
        }

        m2D.setStroke(new BasicStroke(5f));
        m2D.setPaint(Color.WHITE);
        m2D.drawRect(3, 3, minimapDim - 7, minimapDim - 7);

        m2D.setStroke(new BasicStroke(7f));
        m2D.setPaint(Color.GRAY);
        m2D.drawRect(48, 48, minimapDim - 96, minimapDim - 96);
        m2D.dispose();
    }

    private void drawHeroesData(Graphics2D g2D) {
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.WHITE);

        Collection<CharacterDTO> heroes;
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
            int sMod = (int) (infoStrut - ((getViewPort().getHeight() - getViewPort().getY()) / infoStrutHardness));
            heroes.forEach(hero -> {
                int strutMod = sMod;
                if (gameController.isHeroActive(hero, getViewPort().getBounds())) {

                    // Преобразуем координаты героя из карты мира в координаты текущего холста:
                    Point2D relocatedPoint = FoxPointConverterUtil.relocateOn(getViewPort(), getBounds(), hero.getLocation()); // or relocateOnAlt(...) better?

                    // draw hero name:
                    g2D.drawString(hero.getName(),
                            (int) (relocatedPoint.getX() - FFB.getHalfWidthOfString(g2D, hero.getName())),
                            (int) (relocatedPoint.getY() - strutMod));

                    strutMod += 24;

                    // draw hero OIL:
                    g2D.setColor(Color.YELLOW);
                    g2D.fillRoundRect((int) (relocatedPoint.getX() - 50),
                            (int) (relocatedPoint.getY() - strutMod),
                            hero.getHealth(), 9, 3, 3);
                    g2D.setColor(Color.WHITE);
                    g2D.drawRoundRect((int) (relocatedPoint.getX() - 50),
                            (int) (relocatedPoint.getY() - strutMod),
                            hero.getMaxHealth(), 9, 3, 3);

                    // draw hero HP:
                    g2D.setColor(Color.RED);
                    g2D.fillRoundRect((int) (relocatedPoint.getX() - 50),
                            (int) (relocatedPoint.getY() - strutMod),
                            hero.getHealth(), 9, 3, 3);
                    g2D.setColor(Color.WHITE);
                    g2D.drawRoundRect((int) (relocatedPoint.getX() - 50),
                            (int) (relocatedPoint.getY() - strutMod),
                            hero.getMaxHealth(), 9, 3, 3);
                }
            });
        }
    }

    protected void drawDebugInfo(Graphics2D v2D, String worldTitle) {
        if (Constants.isDebugInfoVisible() && worldTitle != null) {
            drawDebug(v2D, worldTitle);
        }
    }

    private void drawDebug(Graphics2D v2D, String worldTitle) {
        v2D.setFont(Constants.DEBUG_FONT);

        if (worldTitle != null && gameController.isGameActive()) {
            String pass = duration != null
                    ? "День %d, %02d:%02d".formatted(duration.toDays(), duration.toHours(), duration.toMinutes())
//                    ? LocalDateTime.of(0, 1, (int) (duration.toDaysPart() + 1), duration.toHoursPart(), duration.toMinutesPart(), 0, 0)
//                    .format(Constants.DATE_FORMAT_2)
                    : "=na=";
//            System.out.format(, duration.toDays(), duration.toHours(), duration.toMinutes(), duration.getSeconds(), duration.toMillis());

            v2D.setColor(Color.BLACK);
            v2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + 22);
            v2D.drawString("В игре: %s".formatted(pass), rightShift - 1f, downShift + 43);

            v2D.setColor(Color.GRAY);
            v2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + 21);
            v2D.drawString("В игре: %s".formatted(pass), rightShift, downShift + 42);
        }

        final int leftShift = 340;
        v2D.setColor(Color.GRAY);

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
        boolean isServerIsOpen = gameController.isServerIsOpen();
        boolean isSocketIsConnected = gameController.isSocketIsOpen();
        v2D.setColor(isServerIsOpen || isSocketIsConnected ? Color.GREEN : Color.DARK_GRAY);
        v2D.drawString("Server open: %s".formatted(isServerIsOpen || isSocketIsConnected), getWidth() - leftShift, getHeight() - 210);
        if (isServerIsOpen) {
            v2D.drawString("Connected clients: %s".formatted(gameController.getConnectedClientsCount()),
                    getWidth() - leftShift, getHeight() - 190);
        }
        v2D.drawString("Connected players: %s".formatted(isServerIsOpen
                        ? gameController.getConnectedPlayers().size() : gameController.getPlayedHeroesService().getHeroes().size()),
                getWidth() - leftShift, getHeight() - 170);
        v2D.setColor(Color.GRAY);

        // если мы в игре:
        if (gameController.isGameActive()) {
            // hero info:
            if (gameController.getCurrentHeroPosition() != null) {
                Shape playerShape = new Ellipse2D.Double(
                        (int) gameController.getCurrentHeroPosition().x - Constants.MAP_CELL_DIM / 2d,
                        (int) gameController.getCurrentHeroPosition().y - Constants.MAP_CELL_DIM / 2d,
                        Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);
                v2D.drawString("Hero pos: %.0fx%.0f".formatted(playerShape.getBounds2D().getCenterX(), playerShape.getBounds2D().getCenterY()),
                        getWidth() - leftShift, getHeight() - 140);
                v2D.drawString("Hero speed: %s".formatted(gameController.getCurrentHeroSpeed()), getWidth() - leftShift, getHeight() - 120);
                v2D.drawString("Hero vector: %s %s %s".formatted(gameController.getCurrentHeroVector().getY(),
                                gameController.getCurrentHeroVector().getX(), gameController.getCurrentHeroVector().getZ()),
                        getWidth() - leftShift, getHeight() - 100);
            }

            // gameplay info:
            if (gameController.getCurrentWorldMap() != null) {
                v2D.drawString("GameMap WxH: %dx%d"
                                .formatted(gameController.getCurrentWorldMap().getWidth(), gameController.getCurrentWorldMap().getHeight()),
                        getWidth() - leftShift, getHeight() - 70);

                v2D.drawString("Canvas XxY-WxH: %dx%d-%dx%d".formatted(getBounds().x, getBounds().y, getBounds().width, getBounds().height),
                        getWidth() - leftShift, getHeight() - 50);

                if (viewPort != null) {
                    v2D.drawString("ViewPort XxY-WxH: %dx%d-%dx%d"
                                    .formatted(viewPort.getBounds().x, viewPort.getBounds().y, viewPort.getBounds().width, viewPort.getBounds().height),
                            getWidth() - leftShift, getHeight() - 30);
                }
            }
        }
    }

    private void drawFps(Graphics2D v2D) {
        if (!isVisible() || !isDisplayable() || !isShowing()) {
            return;
        }

        // FPS check:
        incrementFramesCounter();
        if (System.currentTimeMillis() >= timeStamp + 1000L) {
            Constants.setRealFreshRate(frames.get());
            frames.set(0);
            timeStamp = System.currentTimeMillis();
        }

        if (downShift == 0) {
            downShift = getHeight() * 0.14f;
        }

        v2D.setFont(Constants.DEBUG_FONT);
        v2D.setColor(Color.BLACK);
        if (gameController.isGameActive() && gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
            v2D.drawString("World IP: " + gameController.getCurrentWorldAddress(), rightShift - 1f, downShift - 25);
        }
        v2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getFpsLimit(), FoxVideoMonitorUtil.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift - 1f, downShift + 1f);

        v2D.setColor(Color.GRAY);
        if (gameController.isGameActive() && gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
            v2D.drawString("World IP: " + gameController.getCurrentWorldAddress(), rightShift, downShift - 24);
        }
        v2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getFpsLimit(), FoxVideoMonitorUtil.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift, downShift);
    }

    protected void reloadShapes(FoxCanvas canvas) {
        downShift = getHeight() * 0.14f;

        setLeftGrayMenuPoly(new Polygon(
                new int[]{0, (int) (canvas.getBounds().getWidth() * 0.25D), (int) (canvas.getBounds().getWidth() * 0.2D), 0},
                new int[]{0, 0, canvas.getHeight(), canvas.getHeight()},
                4));

        setHeaderPoly(new Polygon(
                new int[]{0, (int) (canvas.getWidth() * 0.3D), (int) (canvas.getWidth() * 0.29D), (int) (canvas.getWidth() * 0.3D), 0},
                new int[]{3, 3, (int) (canvas.getHeight() * 0.031D), (int) (canvas.getHeight() * 0.061D), (int) (canvas.getHeight() * 0.061D)},
                5));

        try {
            ((iSubPane) getAudiosPane()).recalculate(canvas);
            ((iSubPane) getVideosPane()).recalculate(canvas);
            ((iSubPane) getHotkeysPane()).recalculate(canvas);
            ((iSubPane) getGameplayPane()).recalculate(canvas);
            ((iSubPane) getHeroCreatingPane()).recalculate(canvas);
            ((iSubPane) getWorldCreatingPane()).recalculate(canvas);
            ((iSubPane) getWorldsListPane()).recalculate(canvas);
            ((iSubPane) getHeroesListPane()).recalculate(canvas);
            ((iSubPane) getNetworkListPane()).recalculate(canvas);
            ((iSubPane) getNetworkCreatingPane()).recalculate(canvas);
        } catch (Exception e) {
            log.error("Ошибка при коррекции размеров панелей: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    protected void recalculateMenuRectangles() {
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

        minimapRect = new Rectangle(2, getHeight() - 258, 256, 256);
        minimapShowRect = new Rectangle(minimapRect.width - 14, minimapRect.y, 16, 16);
        minimapHideRect = new Rectangle(0, getHeight() - 16, 16, 16);
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

    protected int validateBackImage() {
        return this.backImage.validate(Constants.getGraphicsConfiguration());
    }

    protected void closeBackImage() {
        this.backImage.flush();
        this.backImage.getGraphics().dispose();
    }

    protected void onExitBack(FoxCanvas canvas) {
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
        g2D.drawImage(pAvatar, getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, this);
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

    protected void drawUI(Graphics2D v2D, String canvasName) {
        if (canvasName.equals("GameCanvas") && isShadowBackNeeds()) {
            v2D.setColor(grayBackColor);
            v2D.fillRect(0, 0, getWidth(), getHeight());
        }
        uiHandler.drawUI(v2D, this);
    }

    protected void createChat() {
        this.chat = new Chat(new Point(getWidth() - getWidth() / 5 - 9, 64), new Dimension(getWidth() / 5, getHeight() / 4));
    }

    protected void decreaseDrawErrorCount() {
        log.info("Понижаем количество ошибок отрисовки...");
        drawErrors.decrementAndGet();
    }

    protected void setSecondThread(String threadName, Thread secondThread) {
        if (this.secondThread != null && this.secondThread.isAlive()) {
            this.secondThread.interrupt();
        }

        this.secondThread = secondThread;
        this.secondThread.setName(threadName);
        this.secondThread.setDaemon(true);
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
        if (drawErrors.getAndIncrement() >= 100) {
            new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                    "Что-то не так с графической системой (%s). Передайте последний лог (error.*) разработчику для решения проблемы."
                            .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            if (gameController.isGameActive()) {
                throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
            } else {
                gameController.exitTheGame(null, 11);
            }
            Thread.yield();
        }
    }

    protected int getDrawErrors() {
        return drawErrors.get();
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
}
