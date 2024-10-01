package game.freya.gui.panes2d;

import fox.FoxRender;
import fox.components.FOptionPane;
import fox.utils.FoxPointConverterUtil;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes2d.sub.AudioSettingsPane;
import game.freya.gui.panes2d.sub.GameplaySettingsPane;
import game.freya.gui.panes2d.sub.HeroCreatingPane;
import game.freya.gui.panes2d.sub.HeroesListPane;
import game.freya.gui.panes2d.sub.HotkeysSettingsPane;
import game.freya.gui.panes2d.sub.NetCreatingPane;
import game.freya.gui.panes2d.sub.NetworkListPane;
import game.freya.gui.panes2d.sub.VideoSettingsPane;
import game.freya.gui.panes2d.sub.WorldCreatingPane;
import game.freya.gui.panes2d.sub.WorldsListPane;
import game.freya.gui.panes2d.sub.components.Chat;
import game.freya.gui.panes2d.sub.iSubPane;
import game.freya.net.SocketConnection;
import game.freya.net.data.NetConnectTemplate;
import game.freya.net.server.Server;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Screenshoter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
import java.util.concurrent.atomic.AtomicInteger;

import static game.freya.config.Constants.FFB;
import static javax.swing.JLayeredPane.PALETTE_LAYER;

@Slf4j
@Getter
@Setter
// iCanvasRunnable уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable
public abstract class RunnableCanvasPanel extends JPanel implements iCanvasRunnable {
    private transient final ApplicationProperties props;
    private transient final GameControllerService gameControllerService;
    private transient final CharacterService characterService;
    private transient final UIHandler uiHandler;
    private transient final String name;
    private transient final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private transient final String backToGameButtonText, optionsButtonText, saveButtonText, backButtonText, exitButtonText;
    private transient final String pausedString, downInfoString1, downInfoString2;
    private final short rightShift = 21;
    private final short infoStrut = 58, infoStrutHardness = 40;
    private final int halfDim = (int) (Constants.getMinimapDim() / 2d);
    private transient Long was;
    private transient JFrame parentFrame;
    private transient AtomicInteger drawErrors = new AtomicInteger(0);
    private transient Thread resizeThread = null;
    private transient Graphics2D g2D;
    private transient Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();
    private transient Thread secondThread;
    private transient Rectangle2D viewPort;
    private transient Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;
    private transient Rectangle minimapRect, minimapShowRect, minimapHideRect;
    private transient BufferedImage pAvatar, menu, menuShadowed, greenArrow;
    private String playerNickName;
    private String currentWorldTitle;
    private VolatileImage backImage, minimapImage;
    private Duration duration;
    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane,
            worldsListPane, heroesListPane, networkListPane, networkCreatingPane;
    private short downShift = 0;
    private Chat chat;

    private byte creatingSubsRetry = 0;
    private long tpf;

    private volatile long currentTimePerFrame = 0;

    protected RunnableCanvasPanel(
            String name,
            GameControllerService gameControllerService,
            CharacterService characterService,
            JFrame parentFrame,
            UIHandler uiHandler,
            ApplicationProperties props
    ) {
        super(null, true);
        this.props = props;

        this.name = name;
        this.uiHandler = uiHandler;
        this.parentFrame = parentFrame;
        this.characterService = characterService;
        this.gameControllerService = gameControllerService;

        this.audioSettingsButtonText = "Настройки звука";
        this.videoSettingsButtonText = "Настройки графики";
        this.hotkeysSettingsButtonText = "Управление";
        this.gameplaySettingsButtonText = "Геймплей";
        this.backButtonText = "← Назад";
        this.exitButtonText = "← Выход";

        this.backToGameButtonText = "Вернуться";
        this.optionsButtonText = "Настройки";
        this.saveButtonText = "Сохранить";

        this.downInfoString1 = props.getAppCompany();
        this.downInfoString2 = props.getAppName()
                .concat(" v.").concat(props.getAppVersion());

        this.pausedString = "- PAUSED -";

        setForeground(Color.BLACK);
    }

    @Override
    public void paint(Graphics g) {
        try {
            drawBackground((Graphics2D) g);
        } catch (AWTException e) {
            log.error("Game paint exception here: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    private void drawBackground(Graphics2D bufGraphics2D) throws AWTException {
        if (!isDisplayable()) {
            try {
                Thread.sleep(100);
                return;
            } catch (InterruptedException _) {
            }
        }

        tpf = System.currentTimeMillis();

        if (currentWorldTitle == null && gameControllerService.getWorldService().getCurrentWorld() != null) {
            currentWorldTitle = gameControllerService.getWorldService().getCurrentWorld().getName();
        }

        prepareBackImage();

        // draw accomplished volatile image:
        bufGraphics2D.drawImage(this.backImage, 0, 0, this);

        currentTimePerFrame = System.currentTimeMillis() - tpf;
    }

    protected void dragViewIfNeeds() {
        if (Controls.isMouseRightEdgeOver()) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragLeft(2d);
                Thread.yield();
            }
        }
        if (Controls.isMouseLeftEdgeOver()) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragRight(2d);
                Thread.yield();
            }
        }
        if (Controls.isMouseUpEdgeOver()) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragDown(2d);
                Thread.yield();
            }
        }
        if (Controls.isMouseDownEdgeOver()) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragUp(2d);
                Thread.yield();
            }
        }
    }

    protected void setGameActive() {
        setVisible(true);
        createSubPanes();
        init();

        // включаем активную игру:
        Controls.setGameActive(true);

        createChat();

//        Controls.setPaused(false);
        Constants.setGameStartedIn(System.currentTimeMillis());
    }

    private void prepareBackImage() throws AWTException {
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
        if (Constants.getGameConfig().isDebugInfoVisible() && currentWorldTitle != null) {
            drawDebug(v2D, currentWorldTitle);
        }
    }

    private Graphics2D getValidVolatileGraphic() throws AWTException {
        if (this.backImage == null) {
            this.backImage = createVolatileImage(getWidth(), getHeight(), new ImageCapabilities(true));
        }

        if (Controls.isRevolatileNeeds()) {
            this.backImage = createVolatileImage(getWidth(), getHeight(), new ImageCapabilities(true));
            Controls.setRevolatileNeeds(false);
        }

        return (Graphics2D) this.backImage.getGraphics();
    }

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.GAME_FONT_03);
        g2D.setColor(new Color(0, 0, 0, 63));
        g2D.drawString(getPausedString(),
                (int) (getWidth() / 2D - FFB.getHalfWidthOfString(g2D, getPausedString())), getHeight() / 2 + 3);

        g2D.setFont(Constants.GAME_FONT_02);
        g2D.setColor(Color.GRAY);
        g2D.drawString(getPausedString(),
                (int) (getWidth() / 2D - FFB.getHalfWidthOfString(g2D, getPausedString())), getHeight() / 2);

        drawEscMenu(g2D);
    }

    private void drawEscMenu(Graphics2D g2D) {
        // buttons text:
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(getBackToGameButtonText(), getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
        g2D.setColor(Controls.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getBackToGameButtonText(), getFirstButtonRect().x, getFirstButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getOptionsButtonText(), getSecondButtonRect().x - 1, getSecondButtonRect().y + 17);
        g2D.setColor(Controls.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getOptionsButtonText(), getSecondButtonRect().x, getSecondButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getSaveButtonText(), getThirdButtonRect().x - 1, getThirdButtonRect().y + 17);
        g2D.setColor(Controls.isThirdButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getSaveButtonText(), getThirdButtonRect().x, getThirdButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(Controls.isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
    }

    private void repaintMenu(Graphics2D v2D) {
        if (menu == null) {
            menu = Constants.CACHE.getBufferedImage("menu");
        }
        if (menuShadowed == null) {
            menuShadowed = Constants.CACHE.getBufferedImage("menu_shadowed");
        }

        v2D.drawImage(isShadowBackNeeds() ? menuShadowed : menu, 0, 0, getWidth(), getHeight(), this);
    }

    private void repaintGame(Graphics2D v2D) throws AWTException {
        // рисуем мир:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.HIGH,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        gameControllerService.getWorldService().getCurrentWorld().draw(v2D);

        // рисуем данные героев поверх игры:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.OFF);
        drawHeroesData(v2D);

        // рисуем миникарту:
        Constants.RENDER.setRender(v2D, FoxRender.RENDER.OFF);
        drawMinimap(v2D);

        Constants.RENDER.setRender(v2D, FoxRender.RENDER.HIGH, true, true);
//        if (Controls.isPaused()) {
//            drawPauseMode(v2D);
//        }
    }

    private void drawMinimap(Graphics2D v2D) throws AWTException {
        // down left minimap:
//        if (!Controls.isPaused()) {
        Rectangle mapButRect;
        if (Controls.isMinimapShowed()) {
            mapButRect = getMinimapShowRect();

            updateMiniMap();

            if (getMinimapRect() != null) {
                // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2));
                Composite cw = v2D.getComposite();
                v2D.setComposite(AlphaComposite.SrcAtop.derive(Constants.getUserConfig().getMiniMapOpacity()));
                v2D.drawImage(this.minimapImage, getMinimapRect().x, getMinimapRect().y,
                        getMinimapRect().width, getMinimapRect().height, this);
                v2D.setComposite(cw);

                if (Constants.getGameConfig().isDebugInfoVisible()) {
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
//        } else {
//            drawPauseMode(v2D);
//        }
    }

    private void updateMiniMap() throws AWTException {
        Point2D.Double myPos = gameControllerService.getCharacterService().getCurrentHero().getLocation();
//        MovingVector cVector = gameControllerService.getCharacterService().getCurrentHero().getVector();
        int srcX = (int) (myPos.x - halfDim);
        int srcY = (int) (myPos.y - halfDim);

        Graphics2D m2D;
        if (minimapImage == null || minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            log.info("Recreating new minimap volatile image by incompatible...");
            minimapImage = createVolatileImage(Constants.getMinimapDim(), Constants.getMinimapDim(), new ImageCapabilities(true));
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
        if (greenArrow == null) {
            greenArrow = Constants.CACHE.getBufferedImage("green_arrow");
        }
        AffineTransform grTrMem = m2D.getTransform();
//        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
        m2D.drawImage(greenArrow, halfDim - 64, halfDim - 64, 128, 128, null);
        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (PlayCharacterDto connectedHero : Constants.getServer().getAcceptedHeroes()) {
            if (gameControllerService.getCharacterService().getCurrentHero().getUid().equals(connectedHero.getUid())) {
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

        if (gameControllerService.getWorldEngine().getGameMap() != null) {
            // сканируем все сущности указанного квадранта:
            Rectangle2D.Double scanRect = new Rectangle2D.Double(
                    Math.min(Math.max(srcX, 0), gameControllerService.getWorldEngine().getGameMap().getWidth() - Constants.getMinimapDim()),
                    Math.min(Math.max(srcY, 0), gameControllerService.getWorldEngine().getGameMap().getHeight() - Constants.getMinimapDim()),
                    Constants.getMinimapDim(), Constants.getMinimapDim());

            m2D.setColor(Color.CYAN);
            gameControllerService.getWorldService().getEnvironmentsFromRectangle(scanRect)
                    .forEach(entity -> {
                        int otherHeroPosX = (int) (halfDim - (myPos.x - entity.getLocation().x));
                        int otherHeroPosY = (int) (halfDim - (myPos.y - entity.getLocation().y));
                        m2D.fillRect(otherHeroPosX - 16, otherHeroPosY - 16, 32, 32);
                    });
        }

        m2D.setStroke(new BasicStroke(5f));
        m2D.setPaint(Color.WHITE);
        m2D.drawRect(3, 3, Constants.getMinimapDim() - 7, Constants.getMinimapDim() - 7);

        m2D.setStroke(new BasicStroke(7f));
        m2D.setPaint(Color.GRAY);
        m2D.drawRect(48, 48, Constants.getMinimapDim() - 96, Constants.getMinimapDim() - 96);
        m2D.dispose();
    }

    private void drawHeroesData(Graphics2D g2D) {
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.WHITE);

        Collection<PlayCharacterDto> heroes;
        if (gameControllerService.getCharacterService().getCurrentHero().isOnline()) {
            heroes = Constants.getServer().getAcceptedHeroes();
        } else {
            if (characterService.getCurrentHero() == null) {
                throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "Этого не должно было случиться.");
            }
            heroes = List.of(characterService.getCurrentHero());
        }

        if (getViewPort() != null) {
            // draw heroes data:
            int sMod = (int) (infoStrut - ((getViewPort().getHeight() - getViewPort().getY()) / infoStrutHardness));
            heroes.forEach(hero -> {
                int strutMod = sMod;
                if (hero.isOnline() && getViewPort().getBounds().contains(hero.getLocation())) {
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

    private void drawDebug(Graphics2D v2D, String worldTitle) {
        v2D.setFont(Constants.DEBUG_FONT);

        if (worldTitle != null && Controls.isGameActive()) {
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

        // server info:
        boolean isServerIsOpen = Constants.getServer().isOpen();
        boolean isSocketIsConnected = Constants.getLocalSocketConnection().isOpen();
        v2D.setColor(isServerIsOpen || isSocketIsConnected ? Color.GREEN : Color.DARK_GRAY);
        v2D.drawString("Server open: %s".formatted(isServerIsOpen || isSocketIsConnected), getWidth() - leftShift, getHeight() - 210);
        if (isServerIsOpen) {
            v2D.drawString("Connected clients: %s".formatted(Constants.getServer().getAuthorizedPlayers()),
                    getWidth() - leftShift, getHeight() - 190);
        }
        v2D.drawString("Connected players: %s".formatted(isServerIsOpen
                        ? Constants.getServer().getAuthorizedPlayers() : Constants.getServer().getAcceptedHeroes().size()),
                getWidth() - leftShift, getHeight() - 170);
        v2D.setColor(Color.GRAY);

        // если мы в игре:
        if (Controls.isGameActive()) {
            // hero info:
            if (gameControllerService.getCharacterService().getCurrentHero().getLocation() != null) {
                Shape playerShape = new Ellipse2D.Double(
                        (int) gameControllerService.getCharacterService().getCurrentHero().getLocation().x - Constants.MAP_CELL_DIM / 2d,
                        (int) gameControllerService.getCharacterService().getCurrentHero().getLocation().y - Constants.MAP_CELL_DIM / 2d,
                        Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);
                v2D.drawString("Hero pos: %.0fx%.0f".formatted(playerShape.getBounds2D().getCenterX(), playerShape.getBounds2D().getCenterY()),
                        getWidth() - leftShift, getHeight() - 140);
                v2D.drawString("Hero speed: %s".formatted(gameControllerService.getCharacterService().getCurrentHero().getSpeed()),
                        getWidth() - leftShift, getHeight() - 120);
                v2D.drawString("Hero vector: %s %s %s".formatted(
                                gameControllerService.getCharacterService().getCurrentHero().getVector().getY(),
                                gameControllerService.getCharacterService().getCurrentHero().getVector().getX(),
                                gameControllerService.getCharacterService().getCurrentHero().getVector().getZ()),
                        getWidth() - leftShift, getHeight() - 100);
            }

            // gameplay info:
            if (gameControllerService.getWorldEngine().getGameMap() != null) {
                v2D.drawString("GameMap WxH: %dx%d".formatted(gameControllerService.getWorldEngine().getGameMap().getWidth(),
                        gameControllerService.getWorldEngine().getGameMap().getHeight()), getWidth() - leftShift, getHeight() - 70);

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

    protected void reloadShapes(RunnableCanvasPanel canvas) {
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

        minimapRect = new Rectangle(2, getHeight() - 258, 256, 256);
        minimapShowRect = new Rectangle(minimapRect.width - 14, minimapRect.y, 16, 16);
        minimapHideRect = new Rectangle(0, getHeight() - 16, 16, 16);
    }

    protected void drawHeader(Graphics2D g2D, String headerTitle) {
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString(headerTitle, getWidth() / 11 - 1, (int) (getHeight() * 0.041D) + 1);
        g2D.setColor(Color.BLACK);
        g2D.drawString(headerTitle, getWidth() / 11, (int) (getHeight() * 0.041D));
    }

    protected void showOptions(Graphics2D g2D) {
        // draw header:
        drawHeader(g2D, "Настройки игры");

        // default buttons text:
        g2D.setColor(Color.BLACK);
        g2D.drawString(audioSettingsButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
        g2D.setColor(Controls.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(audioSettingsButtonText, firstButtonRect.x, firstButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(videoSettingsButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
        g2D.setColor(Controls.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(videoSettingsButtonText, secondButtonRect.x, secondButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(hotkeysSettingsButtonText, thirdButtonRect.x - 1, thirdButtonRect.y + 17);
        g2D.setColor(Controls.isThirdButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(hotkeysSettingsButtonText, thirdButtonRect.x, thirdButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(gameplaySettingsButtonText, fourthButtonRect.x - 1, fourthButtonRect.y + 17);
        g2D.setColor(Controls.isFourthButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(gameplaySettingsButtonText, fourthButtonRect.x, fourthButtonRect.y + 18);

        addExitVariantToOptionsMenuFix(g2D);
    }

    private void addExitVariantToOptionsMenuFix(Graphics2D g2D) {
        g2D.setColor(Color.BLACK);
        g2D.drawString(Controls.isOptionsMenuVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(Controls.isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(Controls.isOptionsMenuVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
    }

    protected void closeGraphics() {
        this.backImage.flush();
        this.backImage.getGraphics().dispose();
        if (this.g2D != null) {
            this.g2D.dispose();
        }
    }

    private void onExitBack(RunnableCanvasPanel canvas) {
        if (Controls.isOptionsMenuVisible()) {
            Controls.setOptionsMenuVisible(false);
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
        } else if (canvas instanceof MenuCanvasRunnable mcr && (int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        ) {
            mcr.stop();
            gameControllerService.exitTheGame(null, 0);
        }
//        else if (canvas instanceof GamePaneRunnable gpr) {
//            Controls.setPaused(!Controls.isPaused());
//        }

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

    @Override
    public void stop() {
        closeGraphics();

        if (Controls.isGameActive()) {
            doScreenshot();
        }

        setVisible(false);
        stopAllThreads();
    }

    private void doScreenshot() {
//        boolean paused = Controls.isPaused();
        boolean debug = Constants.getGameConfig().isDebugInfoVisible();

//        Controls.setPaused(false);
        Constants.getGameConfig().setDebugInfoVisible(false);
        Screenshoter.doScreenshot(getBounds(),
                Constants.getGameConfig().getWorldsImagesDir() + gameControllerService.getWorldService().getCurrentWorld().getUid());
//        Controls.setPaused(paused);
        Constants.getGameConfig().setDebugInfoVisible(debug);
    }

    private void hidePanelIfNotNull(JPanel panel) {
        if (panel != null) {
            panel.setVisible(false);
        }
    }

    protected boolean isShadowBackNeeds() {
        return Controls.isOptionsMenuVisible()
                || (heroCreatingPane != null && heroCreatingPane.isVisible())
                || (worldCreatingPane != null && worldCreatingPane.isVisible())
                || (worldsListPane != null && worldsListPane.isVisible())
                || (heroesListPane != null && heroesListPane.isVisible())
                || (networkListPane != null && networkListPane.isVisible())
                || (networkCreatingPane != null && networkCreatingPane.isVisible());
    }

    protected void createSubPanes() {
        creatingSubsRetry++;

        setAudiosPane(new AudioSettingsPane(this));
        setVideosPane(new VideoSettingsPane(this));
        setHotkeysPane(new HotkeysSettingsPane(this));
        setGameplayPane(new GameplaySettingsPane(this));
        setWorldCreatingPane(new WorldCreatingPane(this, gameControllerService));
        setHeroCreatingPane(new HeroCreatingPane(this, gameControllerService));
        setWorldsListPane(new WorldsListPane(this, gameControllerService));
        setHeroesListPane(new HeroesListPane(this, gameControllerService));
        setNetworkListPane(new NetworkListPane(this, gameControllerService));
        setNetworkCreatingPane(new NetCreatingPane(this, gameControllerService));

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
            if (creatingSubsRetry < 3) {
                createSubPanes();
            } else {
                log.error("Слишком часто не удается создать панели. Обратить внимание!");
                return;
            }
        }
        creatingSubsRetry = 0;
    }

    private void drawUI(Graphics2D v2D, String canvasName) {
        if (canvasName.equals("GameCanvas") && isShadowBackNeeds()) {
            v2D.setColor(Constants.getGrayBackColor());
            v2D.fillRect(0, 0, getWidth(), getHeight());
        }
        uiHandler.drawUI(v2D, this);
    }

    private void createChat() {
        this.chat = new Chat(new Point(getWidth() - getWidth() / 5 - 9, 64), new Dimension(getWidth() / 5, getHeight() / 4));
    }

    protected void decreaseDrawErrorCount() {
        log.debug("Понижаем количество ошибок отрисовки...");
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

    protected void throwExceptionAndYield(Exception e) {
        if (drawErrors.getAndIncrement() >= 100) {
            new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                    "Что-то не так с графической системой (%s). Передайте последний лог (error.*) разработчику для решения проблемы."
                            .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            if (Controls.isGameActive()) {
                throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
            } else {
                gameControllerService.exitTheGame(null, 11);
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
        return viewPort.getHeight() < gameControllerService.getWorldEngine().getGameMap().getHeight();
    }

    protected boolean canDragLeft() {
        return viewPort.getWidth() < gameControllerService.getWorldEngine().getGameMap().getWidth();
    }

    protected boolean canDragRight() {
        return viewPort.getX() > 0;
    }

    protected void checkOutOfFieldCorrection() {
        while (getViewPort().getX() < 0) {
            dragLeft(1d);
        }

        while (getViewPort().getWidth() > gameControllerService.getWorldEngine().getGameMap().getWidth()) {
            dragRight(1d);
        }

        while (getViewPort().getY() < 0) {
            dragUp(1d);
        }

        while (getViewPort().getHeight() > gameControllerService.getWorldEngine().getGameMap().getHeight()) {
            dragDown(1d);
        }
    }

    public void dragLeft(double pixels) {
        if (canDragLeft()) {
            log.debug("Drag left...");
            double mapWidth = gameControllerService.getWorldEngine().getGameMap().getWidth();
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
            double mapHeight = gameControllerService.getWorldEngine().getGameMap().getHeight();
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

    public void serverUp(WorldDto aNetworkWorld) {
        getNetworkListPane().repaint(); // костыль для отображения анимации

        // Если игра по сети, но Сервер - мы, и ещё не запускался:
        UUID curWorldUid = gameControllerService.getWorldService().saveOrUpdate(aNetworkWorld).getUid();
        gameControllerService.getWorldService().setCurrentWorld(curWorldUid);

        // Открываем локальный Сервер:
        if (gameControllerService.getWorldService().getCurrentWorld().isLocal()
                && gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()
                && (Constants.getServer() == null || Constants.getServer().isClosed())
        ) {
            if (openServer()) {
                log.info("Сервер сетевой игры успешно активирован на {}", Constants.getServer().getAddress());
            } else {
                log.warn("Что-то пошло не так при активации Сервера.");
                new FOptionPane().buildFOptionPane("Server error:", "Что-то пошло не так при активации Сервера.", 60, true);
                return;
            }
        }

        if (Constants.getLocalSocketConnection() != null && Constants.getLocalSocketConnection().isOpen()) {
            log.error("Socket should was closed here! Closing...");
            Constants.getLocalSocketConnection().close();
        }

        // Подключаемся к локальному Серверу как новый Клиент:
        connectToServer(NetConnectTemplate.builder()
                .address(aNetworkWorld.getAddress())
                .password(aNetworkWorld.getPassword())
                .worldUid(aNetworkWorld.getUid())
                .build());
    }

    /**
     * Создание и открытие Сервера.
     * Создаётся экземпляр Сервера, ждём его запуска и возвращаем успешность процесса.
     *
     * @return успешность открытия Сервера.
     */
    private boolean openServer() {
        Constants.setServer(Server.getInstance(gameControllerService));
        Constants.getServer().start();
        Constants.getServer().untilOpen(Constants.getGameConfig().getServerOpenTimeAwait());
        return Constants.getServer().isOpen();
    }

    public void connectToServer(NetConnectTemplate connectionTemplate) {
        getHeroesListPane().setVisible(false);

        Constants.setConnectionAwait(true);
        getNetworkListPane().repaint(); // костыль для отображения анимации

        if (connectionTemplate.address().isBlank()) {
            new FOptionPane().buildFOptionPane("Ошибка адреса:", "Адрес сервера не может быть пустым.", 10, true);
        }

        // 1) приходим сюда с host:port для подключения
        String address = connectionTemplate.address().trim();
        String h = address.contains(":") ? address.split(":")[0].trim() : address;
        Integer p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;
        getNetworkListPane().repaint(); // костыль для отображения анимации
        try {
            // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
            if (connectToServer(h.trim(), p, connectionTemplate.password())) {
                // 3) проверка героя в этом мире:
                chooseOrCreateHeroForWorld(gameControllerService.getWorldService().getCurrentWorld().getUid());
            } else {
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, Constants.getLocalSocketConnection().getLastExplanation());
            }
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            if (gse.getCode().equals("ER07")) {
                new FOptionPane().buildFOptionPane("Не доступно:", gse.getMessage(), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            }
        } catch (IllegalThreadStateException tse) {
            log.error("Connection Thread state exception: {}", ExceptionUtils.getFullExceptionMessage(tse));
        } catch (Exception e) {
            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка подключения '%s'.\n"
                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10/13:13958)")
                    .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
        } finally {
//            gameControllerService.getLocalSocketConnection().close();
            Constants.setConnectionAwait(false);
        }
    }

    private boolean connectToServer(String host, Integer port, String password) {
        // создание нового подключения к Серверу (сокета):
        Constants.setLocalSocketConnection(new SocketConnection());

        // подключаемся к серверу:
        if (Constants.getLocalSocketConnection().isOpen() && Constants.getLocalSocketConnection().getHost().equals(host)) {
            // верно ли подобное поведение?
            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", Constants.getLocalSocketConnection().getHost());
        } else {
            Constants.getLocalSocketConnection().openSocket(host, port, gameControllerService, false);
            Constants.getLocalSocketConnection().untilOpen(Constants.getGameConfig().getSocketConnectionTimeout());
        }

        if (!Constants.getLocalSocketConnection().isOpen()) {
            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED,
                    "No reached socket connection to " + host + (port == null ? "" : ":" + port));
        } else if (!host.equals(Constants.getLocalSocketConnection().getHost())) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
        }

        // передаём свои данные для авторизации:
        Constants.getLocalSocketConnection().authRequest(password);

        Thread authThread = Thread.startVirtualThread(() -> {
            while (!Constants.getLocalSocketConnection().isAuthorized() && !Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
        });

        try {
            // ждём окончания авторизации Сервером:
            authThread.join(Constants.getGameConfig().getSocketAuthTimeout());

            // когда таймаут уже кончился:
            if (authThread.isAlive()) {
                log.error("Так и не получили успешной авторизации от Сервера за отведённое время.");
                authThread.interrupt();
                Constants.getLocalSocketConnection().close();
                return false;
            } else {
                log.info("Успешная авторизация Сервером.");
                return true;
            }
        } catch (InterruptedException e) {
            log.error("Ошибка авторизации Сервером: {}", e.getMessage(), e);
            authThread.interrupt();
            Constants.getLocalSocketConnection().close();
            return false;
        }
    }

    /**
     * После выбора мира - приходим сюда для создания нового героя или
     * выбора существующего, для игры в данном мире.
     *
     * @param worldUid uid выбранного для игры мира.
     */
    public void chooseOrCreateHeroForWorld(UUID worldUid) {
        getWorldsListPane().setVisible(false);
        getWorldCreatingPane().setVisible(false);
        getNetworkListPane().setVisible(false);
        getNetworkCreatingPane().setVisible(false);

        gameControllerService.getWorldService().setCurrentWorld(worldUid);
        List<PlayCharacterDto> heroes = gameControllerService.getCharacterService().findAllByWorldUidAndOwnerUid(
                gameControllerService.getWorldService().getCurrentWorld().getUid(),
                gameControllerService.getPlayerService().getCurrentPlayer().getUid());
        if (heroes.isEmpty()) {
            getHeroCreatingPane().setVisible(true);
        } else {
            getHeroesListPane().setVisible(true);
        }
    }

    protected void onResize() {
        if (resizeThread != null && resizeThread.isAlive()) {
            return;
        }

        resizeThread = new Thread(() -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.debug("Resizing of menu canvas...");

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(getParentFrame().getSize());
            } else {
                setSize(getParentFrame().getRootPane().getSize());
            } // else if (!getSize().equals(getParentFrame().getRootPane().getSize())) {
            //       setSize(getParentFrame().getRootPane().getSize());
            //   }

            if (isVisible()) {
                reloadShapes(this);
                recalculateMenuRectangles();
            }

            recreateViewPort();
            moveViewToPlayer(0, 0);

            requestFocusInWindow();

            Controls.setRevolatileNeeds(true);
        });
        resizeThread.start();

        try {
            resizeThread.join(500);
        } catch (InterruptedException e) {
            resizeThread.interrupt();
        }
    }

    public void moveViewToPlayer(double x, double y) {
        if (gameControllerService.getWorldEngine().getGameMap() != null && getViewPort() != null) {
            Point2D.Double p = gameControllerService.getCharacterService().getCurrentHero().getLocation();
            Rectangle viewRect = getViewPort().getBounds();
            getViewPort().setRect(
                    p.x - (viewRect.getWidth() - viewRect.getX()) / 2D + x,
                    p.y - (viewRect.getHeight() - viewRect.getY()) / 2D + y,
                    p.x + (viewRect.getWidth() - viewRect.getX()) / 2D - x,
                    p.y + (viewRect.getHeight() - viewRect.getY()) / 2D - y);

            checkOutOfFieldCorrection();
        }
    }

    protected void recreateViewPort() {
        setViewPort(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    public void openCreatingNewHeroPane(PlayCharacterDto template) {
        getHeroesListPane().setVisible(false);
        getHeroCreatingPane().setVisible(true);
        if (template != null) {
            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
        }
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(PlayCharacterDto hero) {
        gameControllerService.getPlayerService().setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());
        gameControllerService.getCharacterService().setCurrentHero(hero);

        // если этот мир по сети:
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            // шлем на Сервер своего выбранного Героя:
            if (Constants.getLocalSocketConnection().registerOnServer()) {
//                gameControllerService.getPlayedHeroes().addHero(characterService.getCurrentHero());
                startGame();
            } else {
                log.error("Сервер не принял нашего Героя: {}", Constants.getLocalSocketConnection().getLastExplanation());
                characterService.getCurrentHero().setOnline(false);
                characterService.saveCurrent();
                getHeroCreatingPane().repaint();
                getHeroesListPane().repaint();
            }
        } else {
            // иначе просто запускаем мир и играем локально:
            startGame();
        }
    }

    private void startGame() {
        getHeroCreatingPane().setVisible(false);
        getHeroesListPane().setVisible(false);

        log.info("Подготовка к запуску игры должна была пройти успешно. Запуск игрового мира...");
        gameControllerService.getSceneController().loadScene(ScreenType.GAME_SCREEN);
    }

    protected void zoomIn() {
        log.debug("Zoom in...");

        // если окно меньше установленного лимита:
        if (getViewPort().getWidth() - getViewPort().getX() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
                || getViewPort().getHeight() - getViewPort().getY() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
        ) {
            log.debug("Can`t zoom in: vpWidth = {}, vpHeight = {} but minSize = {}",
                    getViewPort().getWidth() - getViewPort().getX(), getViewPort().getHeight() - getViewPort().getY(),
                    Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS);
            return;
        }

        moveViewToPlayer(Constants.getGameConfig().getScrollSpeed(),
                (int) (Constants.getGameConfig().getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));
    }

    protected void zoomOut() {
        log.debug("Zoom out...");

        // если окно больше установленного лимита или и так максимального размера:
        if (!canZoomOut(getViewPort().getWidth() - getViewPort().getX(), getViewPort().getHeight() - getViewPort().getY(),
                gameControllerService.getWorldEngine().getGameMap().getWidth(), gameControllerService.getWorldEngine().getGameMap().getHeight())) {
            return;
        }

        moveViewToPlayer(-Constants.getGameConfig().getScrollSpeed(),
                -(int) (Constants.getGameConfig().getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));

//        double delta = getBounds().getWidth() / getBounds().getHeight();
////        double widthPercent = getBounds().getWidth() * Constants.getScrollSpeed();
////        double heightPercent = getBounds().getHeight() * Constants.getScrollSpeed();
//
////        double factor = getBounds().getWidth() % getBounds().getHeight();
////        double resultX = getBounds().getWidth() / factor * 10;
////        double resultY = getBounds().getHeight() / factor * 10;
//        double sdf = (viewPort.getWidth() - viewPort.getX()) / 100d;
//        double sdf2 = (viewPort.getHeight() - viewPort.getY()) / (100d / delta);
//        viewPort.setRect(
//                viewPort.getX() - sdf,
//                viewPort.getY() - sdf2,
//                viewPort.getWidth() + sdf,
//                viewPort.getHeight() + sdf2);
////        log.info("f): {}, r1): {}, r2): {}", factor, resultX, resultY);

        // проверка на выход за края игрового поля:
        checkOutOfFieldCorrection();
    }

    private boolean canZoomOut(double viewWidth, double viewHeight, double mapWidth, double mapHeight) {
        int maxCellsSize = Constants.MAP_CELL_DIM * Constants.MAX_ZOOM_OUT_CELLS;

        // если окно больше установленного лимита:
        if (viewWidth >= maxCellsSize || viewHeight >= maxCellsSize) {
            log.debug("Can`t zoom out: viewWidth = {} and viewHeight = {} but maxCellsSize is {}", viewWidth, viewHeight, maxCellsSize);
            return false;
        }

        // если окно уже максимального размера:
        if (viewWidth >= mapWidth || viewHeight >= mapHeight) {
            log.debug("Can`t zoom out: maximum size reached.");
            return false;
        }

        return true;
    }

    protected void doAnimate() {
        if (getNetworkListPane().isVisible()) {
            if (Constants.isConnectionAwait()) {
                getNetworkListPane().repaint();
            }
            if (Constants.isPingAwait()) {
                getNetworkListPane().repaint();
            }
        }
    }

    protected void inAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "backFunction",
                Constants.getUserConfig().getHotkeys().getKeyPause().getSwingKey(),
                Constants.getUserConfig().getHotkeys().getKeyPause().getSwingMask(),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                        if (isVisible() && Controls.isPaused()) {
//                            onExitBack(RunnableCanvasPanel.this);
//                        } else {
//                            Controls.setPaused(!Controls.isPaused());
//                        }
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "enterNextFunction",
                KeyEvent.VK_ENTER, 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (getHeroesListPane().isVisible()) {
                            List<PlayCharacterDto> heroes = gameControllerService.getCharacterService().findAllByWorldUidAndOwnerUid(
                                    gameControllerService.getWorldService().getCurrentWorld().getUid(),
                                    gameControllerService.getPlayerService().getCurrentPlayer().getUid());
                            playWithThisHero(heroes.getFirst());
                            getHeroesListPane().setVisible(false);
                        } else if (getWorldsListPane().isVisible()) {
                            UUID lastWorldUid = gameControllerService.getPlayerService().getCurrentPlayer().getLastPlayedWorldUid();
                            if (gameControllerService.getWorldService().isWorldExist(lastWorldUid)) {
                                chooseOrCreateHeroForWorld(lastWorldUid);
                            } else {
                                chooseOrCreateHeroForWorld(gameControllerService.getWorldService()
                                        .findAllByNetAvailable(false).getFirst().getUid());
                            }
                        } else {
                            getWorldsListPane().setVisible(true);
                        }
                    }
                });

        Controls.setControlsMapped(true);
    }

    protected void stopAllThreads() {
        if (getResizeThread() != null && getResizeThread().isAlive()) {
            getResizeThread().interrupt();
        }
        if (getSecondThread() != null && getSecondThread().isAlive()) {
            getSecondThread().interrupt();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        onResize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.info("Возврат фокуса на холст...");
        requestFocus();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();

        if (!Controls.isGameActive() /* || Controls.isPaused() */) {
            // если мы в меню либо игра на паузе - проверяем меню:
            Controls.setFirstButtonOver(getFirstButtonRect().contains(p));
            Controls.setSecondButtonOver(getSecondButtonRect().contains(p));
            Controls.setThirdButtonOver(getThirdButtonRect().contains(p));
            Controls.setFourthButtonOver(getFourthButtonRect().contains(p));
            Controls.setExitButtonOver(getExitButtonRect().contains(p));
        } else { // иначе мониторим наведение на край окна для прокрутки поля:
            if (!Controls.isMovingKeyActive() && Constants.getUserConfig().isDragGameFieldOnFrameEdgeReached()) {
                Controls.setMouseLeftEdgeOver(p.getX() <= 15
                        && (Constants.getUserConfig().isFullscreen() || p.getX() > 1) && !getMinimapHideRect().contains(p));
                Controls.setMouseRightEdgeOver(p.getX() >= getWidth() - 15 && (Constants.getUserConfig().isFullscreen() || p.getX() < getWidth() - 1));
                Controls.setMouseUpEdgeOver(p.getY() <= 10 && (Constants.getUserConfig().isFullscreen() || p.getY() > 1));
                Controls.setMouseDownEdgeOver(p.getY() >= getHeight() - 15
                        && (Constants.getUserConfig().isFullscreen() || p.getY() < getHeight() - 1) && !getMinimapHideRect().contains(p));
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mousePressedOnPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (Controls.isGameActive()) {
            if (getMinimapShowRect().contains(e.getPoint())) {
                Controls.setMinimapShowed(false);
            }
            if (getMinimapHideRect().contains(e.getPoint())) {
                Controls.setMinimapShowed(true);
            }
        }

        if (Controls.isFirstButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
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
                openCreatingNewHeroPane(null);
            } else if (getNetworkListPane().isVisible()) {
                getNetworkListPane().setVisible(false);
                getNetworkCreatingPane().setVisible(true);
            } else {
                if (gameControllerService.getWorldService().findAllByNetAvailable(false).isEmpty()) {
                    getWorldCreatingPane().setVisible(true);
                } else {
                    getWorldsListPane().setVisible(true);
                }
            }
        }
        if (Controls.isSecondButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
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
                ((NetworkListPane) getNetworkListPane()).reloadNet(this);
            } else {
                getNetworkListPane().setVisible(true);
            }
        }
        if (Controls.isThirdButtonOver()) {
            if (!Controls.isOptionsMenuVisible() && !getHeroCreatingPane().isVisible() && !getWorldsListPane().isVisible()) {
                Controls.setOptionsMenuVisible(true);
                getAudiosPane().setVisible(true);
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (Controls.isOptionsMenuVisible()) {
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
        if (Controls.isFourthButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
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

        // ... // ... // ... // ... // ... // ... // todo

        if (Controls.isFirstButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
                getAudiosPane().setVisible(true);
                getVideosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);

            } else {
//                Controls.setPaused(false);
                Controls.setOptionsMenuVisible(false);
            }
        }
        if (Controls.isSecondButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
                getVideosPane().setVisible(true);
                getAudiosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                Controls.setOptionsMenuVisible(true);
                getAudiosPane().setVisible(true);
            }
        }
        if (Controls.isThirdButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
                getHotkeysPane().setVisible(true);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                // нет нужды в паузе здесь, просто сохраняемся:
                gameControllerService.saveTheGame(getDuration());
//                Controls.setPaused(false);
                new FOptionPane().buildFOptionPane("Успешно", "Игра сохранена!",
                        FOptionPane.TYPE.INFO, null, Constants.getDefaultCursor(), 3, false);
            }
        }
        if (Controls.isFourthButtonOver()) {
            if (Controls.isOptionsMenuVisible()) {
                getGameplayPane().setVisible(true);
                getHotkeysPane().setVisible(false);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
            } else {
                Constants.showNFP();
            }
        }

        if (Controls.isExitButtonOver()) {
            onExitBack(this);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            Point p = e.getPoint();
            log.debug("drag: {}x{}", p.x, p.y);
            if (p.getX() < mousePressedOnPoint.getX()) {
                Controls.setMouseLeftEdgeOver(true);
            } else if (p.getX() > mousePressedOnPoint.getX()) {
                Controls.setMouseRightEdgeOver(true);
            } else if (p.getY() < mousePressedOnPoint.getY()) {
                Controls.setMouseUpEdgeOver(true);
            } else if (p.getY() > mousePressedOnPoint.getY()) {
                Controls.setMouseDownEdgeOver(true);
            }
            this.mousePressedOnPoint = p;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
//        if (Controls.isPaused()) {
//            // not work into pause
//            return;
//        }

        switch (e.getWheelRotation()) {
            case 1 -> zoomOut();
            case -1 -> zoomIn();
            default -> log.warn("MouseWheelEvent unknown action: {}", e.getWheelRotation());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // hero movement:
        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveForward().getSwingKey()) {
            Controls.setPlayerMovingUp(true);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveBack().getSwingKey()) {
            Controls.setPlayerMovingDown(true);
        }
        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveLeft().getSwingKey()) {
            Controls.setPlayerMovingLeft(true);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveRight().getSwingKey()) {
            Controls.setPlayerMovingRight(true);
        }

        // camera movement:
        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookUp().getSwingKey()) {
            Controls.setMovingKeyActive(true);
            Controls.setMouseUpEdgeOver(true);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookDown().getSwingKey()) {
            Controls.setMovingKeyActive(true);
            Controls.setMouseDownEdgeOver(true);
        }
        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookLeft().getSwingKey()) {
            Controls.setMovingKeyActive(true);
            Controls.setMouseLeftEdgeOver(true);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookRight().getSwingKey()) {
            Controls.setMovingKeyActive(true);
            Controls.setMouseRightEdgeOver(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveForward().getSwingKey()) {
            Controls.setPlayerMovingUp(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveBack().getSwingKey()) {
            Controls.setPlayerMovingDown(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveLeft().getSwingKey()) {
            Controls.setPlayerMovingLeft(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyMoveRight().getSwingKey()) {
            Controls.setPlayerMovingRight(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookUp().getSwingKey()) {
            Controls.setMovingKeyActive(false);
            Controls.setMouseUpEdgeOver(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookDown().getSwingKey()) {
            Controls.setMovingKeyActive(false);
            Controls.setMouseDownEdgeOver(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookLeft().getSwingKey()) {
            Controls.setMovingKeyActive(false);
            Controls.setMouseLeftEdgeOver(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getHotkeys().getKeyLookRight().getSwingKey()) {
            Controls.setMovingKeyActive(false);
            Controls.setMouseRightEdgeOver(false);
        }
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }
}
