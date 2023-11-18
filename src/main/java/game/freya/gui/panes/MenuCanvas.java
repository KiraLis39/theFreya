package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.sub.AudioSettingsPane;
import game.freya.gui.panes.sub.GameplaySettingsPane;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.HeroesListPane;
import game.freya.gui.panes.sub.HotkeysSettingsPane;
import game.freya.gui.panes.sub.VideoSettingsPane;
import game.freya.gui.panes.sub.WorldCreatingPane;
import game.freya.gui.panes.sub.WorldsListPane;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static javax.swing.JLayeredPane.PALETTE_LAYER;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvas extends FoxCanvas {
    private final transient GameController gameController;
    private final transient JFrame parentFrame;
    private transient VolatileImage backMenuImage;
    private transient BufferedImage pAvatar;
    private transient Rectangle avatarRect;
    private String downInfoString1, downInfoString2;
    private String startGameButtonText, startNewGameButtonText, continueGameButtonText, coopPlayButtonText,
            optionsButtonText, exitButtonText, backButtonText, randomButtonText, resetButtonText, createNewButtonText;
    private String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;
    private boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;
    private boolean isOptionsMenuSetVisible = false, isCreatingNewHeroSetVisible = false,
            isCreatingNewWorldSetVisible = false, isChooseWorldMenuVisible = false, isChooseHeroMenuVisible = false;
    private boolean isAudioSettingsMenuVisible = false, isVideoSettingsMenuVisible = false,
            isHotkeysSettingsMenuVisible = false, isGameplaySettingsMenuVisible = false;
    private boolean isMenuActive, initialized = false;

    private transient Area area;
    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane, heroesListPane;
    private double parentHeightMemory = 0;
    private byte drawErrorCount = 0;
    private transient Thread resizeThread = null;
    private WorldDTO aNewWorldMemory;

    public MenuCanvas(JFrame parentFrame, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "MenuCanvas");
        this.gameController = gameController;
        this.parentFrame = parentFrame;

        setSize(parentFrame.getLayeredPane().getSize());
        setBackground(Color.DARK_GRAY.darker());
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        inAc();

        new Thread(this).start();
    }

    private void inAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "backFunction",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isVisible()) {
                            onExitBack();
                        } else {
                            Constants.setPaused(!Constants.isPaused());
                        }
                    }
                });
    }

    @Override
    public void run() {
        setMenuActive();

        while (isMenuActive) {
            if (getParent() == null || !isDisplayable() || Constants.isPaused()) {
                Thread.yield();
                continue;
            }

            if (!initialized) {
                init();
            }

            // если изменился размер фрейма:
            if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                log.info("Resizing by parent frame...");
                onResize();
                parentHeightMemory = parentFrame.getBounds().getHeight();
            }

            Graphics2D g2D = null;
            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
                }

                do {
                    g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                    Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED,
                            Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

                    drawBackground(g2D);
                    drawMenu(g2D);

                    if (Constants.isDebugInfoVisible()) {
                        super.drawDebugInfo(g2D, null);
                    }
                } while (getBufferStrategy().contentsRestored() || getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
                drawErrorCount++; // при неуспешной отрисовке
                if (drawErrorCount > 100) {
                    new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                            "Что-то не так с графической системой. Передайте последний лог (error.*) разработчику для решения проблемы.",
                            FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
//                    gameController.exitTheGame(null);
                    throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
                }
            } finally {
                if (g2D != null) {
                    g2D.dispose();
                }
            }

            if (Constants.isFrameLimited() && Constants.getDiscreteDelay() > 1) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // при успешной отрисовке:
            if (drawErrorCount > 0) {
                drawErrorCount--;
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void setMenuActive() {
        Constants.setPaused(false);
        this.isMenuActive = true;
    }

    private void drawBackground(Graphics2D g2D) {
        if (backMenuImage != null) {
            if (backMenuImage.validate(Constants.getGraphicsConfiguration()) != VolatileImage.IMAGE_OK) {
                recreateBackImage();
            }
            // draw background image:
            g2D.drawImage(backMenuImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void drawMenu(Graphics2D g2D) {
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);

        if (isOptionsMenuSetVisible) {
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
        } else if (isCreatingNewWorldSetVisible) {
            drawHeader(g2D, "Создание мира");

            // creating world buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomButtonText, firstButtonRect.x, firstButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
            g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetButtonText, secondButtonRect.x, secondButtonRect.y + 18);
        } else if (isChooseWorldMenuVisible) {
            drawHeader(g2D, "Выбор мира");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, firstButtonRect.x, firstButtonRect.y + 18);
        } else if (isChooseHeroMenuVisible) {
            drawHeader(g2D, "Выбор героя");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, firstButtonRect.x, firstButtonRect.y + 18);
        } else if (isCreatingNewHeroSetVisible) {
            drawHeader(g2D, "Создание героя");

            // creating hero buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomButtonText, firstButtonRect.x, firstButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
            g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetButtonText, secondButtonRect.x, secondButtonRect.y + 18);
        } else {
            // default buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(startGameButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(startGameButtonText, firstButtonRect.x, firstButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(coopPlayButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
            g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(coopPlayButtonText, secondButtonRect.x, secondButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(optionsButtonText, thirdButtonRect.x - 1, thirdButtonRect.y + 17);
            g2D.setColor(thirdButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(optionsButtonText, thirdButtonRect.x, thirdButtonRect.y + 18);
        }

        g2D.setColor(Color.BLACK);
        g2D.drawString(isOptionsMenuSetVisible // || isStartGameMenuSetVisible
                ? backButtonText : exitButtonText, exitButtonRect.x - 1, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(isOptionsMenuSetVisible // || isStartGameMenuSetVisible
                ? backButtonText : exitButtonText, exitButtonRect.x, exitButtonRect.y + 18);
    }

    private void drawSettingsPart(Graphics2D g2D, int partIndex) {
        Font font = g2D.getFont();

        if (area == null || area.getBounds().getHeight() != getBounds().getHeight()) {
            area = new Area(getBounds());
            area.subtract(new Area(super.getLeftGrayMenuPoly()));
        }

        audiosPane.setVisible(partIndex == 0);
        videosPane.setVisible(partIndex == 1);
        hotkeysPane.setVisible(partIndex == 2);
        gameplayPane.setVisible(partIndex == 3);
        heroCreatingPane.setVisible(partIndex == 4);
        worldCreatingPane.setVisible(partIndex == 5);
        worldsListPane.setVisible(partIndex == 6);
        heroesListPane.setVisible(partIndex == 7);

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.RED);
            g2D.draw(area);
        }

        // restore font:
        g2D.setFont(font);
    }

    private void drawHeader(Graphics2D g2D, String headerTitle) {
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

    private void init() {
        reloadShapes(this);

        downInfoString1 = gameController.getGameConfig().getAppCompany();
        downInfoString2 = gameController.getGameConfig().getAppName().concat(" v.").concat(gameController.getGameConfig().getAppVersion());

        startGameButtonText = "Начать игру";
        startNewGameButtonText = "Начать новую игру";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        exitButtonText = "← Выход";
        backButtonText = "← В главное меню";

        continueGameButtonText = "Продолжить";
        createNewButtonText = "Создать";

        audioSettingsButtonText = "Настройки звука";
        videoSettingsButtonText = "Настройки графики";
        hotkeysSettingsButtonText = "Управление";
        gameplaySettingsButtonText = "Геймплей";

//        completeButtonText = "Принять";
        randomButtonText = "Случайно";
        resetButtonText = "Сброс";

        try {
            Constants.CACHE.addIfAbsent("backMenuImage",
                    ImageIO.read(new File("./resources/images/menu.png")));
            Constants.CACHE.addIfAbsent("backMenuImageShadowed",
                    ImageIO.read(new File("./resources/images/menu_shadowed.png")));
            recreateBackImage();
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        recalculateMenuRectangles();

        setVisible(true);
        this.initialized = true;
    }

    private void recalculateSettingsPanes() {
        // удаляем старые панели с фрейма:
        dropOldPanesFromLayer();

        // создаём новые панели:
        audiosPane = new AudioSettingsPane(this);
        videosPane = new VideoSettingsPane(this);
        hotkeysPane = new HotkeysSettingsPane(this);
        gameplayPane = new GameplaySettingsPane(this);
        heroCreatingPane = new HeroCreatingPane(this);
        worldCreatingPane = new WorldCreatingPane(this);
        worldsListPane = new WorldsListPane(this);
        heroesListPane = new HeroesListPane(this);

        // добавляем панели на слой:
        try {
            parentFrame.getLayeredPane().add(audiosPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(videosPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(hotkeysPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(gameplayPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(heroCreatingPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(worldCreatingPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(worldsListPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(heroesListPane, PALETTE_LAYER);
        } catch (Exception e) {
            log.error("Ошибка при добавлении панелей на слой: {}", ExceptionUtils.getFullExceptionMessage(e));
            recalculateSettingsPanes();
        }
    }

    private void dropOldPanesFromLayer() {
        try {
            parentFrame.getLayeredPane().remove(audiosPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма audiosPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(videosPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма videosPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(hotkeysPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма hotkeysPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(gameplayPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма gameplayPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(heroCreatingPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма heroCreatingPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(worldCreatingPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма worldCreatingPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(worldsListPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма worldsListPane, которой там нет.");
        }
        try {
            parentFrame.getLayeredPane().remove(heroesListPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма heroesListPane, которой там нет.");
        }
    }

    private void recreateBackImage() {
        backMenuImage = createVolatileImage(getWidth(), getHeight());
        Graphics2D g2D = backMenuImage.createGraphics();
        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED, // todo: будет ли тут иметь вообще значение рендер?
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        g2D.drawImage((BufferedImage) (isShadowBackNeeds() ? Constants.CACHE.get("backMenuImageShadowed") : Constants.CACHE.get("backMenuImage")),
                0, 0, getWidth(), getHeight(), this);

        // fill left gray polygon:
        g2D.setColor(isOptionsMenuSetVisible ? Constants.getMainMenuBackgroundColor2() : Constants.getMainMenuBackgroundColor());
        g2D.fillPolygon(getLeftGrayMenuPoly());

        // down right corner text:
        g2D.setFont(Constants.INFO_FONT);
        g2D.setColor(Color.WHITE);
        g2D.drawString(downInfoString1,
                (int) (getWidth() - Constants.FFB.getStringBounds(g2D, downInfoString1).getWidth() - 6), getHeight() - 9);
        g2D.drawString(downInfoString2,
                (int) (getWidth() - Constants.FFB.getStringBounds(g2D, downInfoString2).getWidth() - 6), getHeight() - 25);

        // player`s info:
        if (!isOptionsMenuSetVisible && !isCreatingNewHeroSetVisible && !isChooseWorldMenuVisible) {
            if (pAvatar == null) {
                pAvatar = gameController.getCurrentPlayer().getAvatar();
            }
            g2D.setColor(Color.BLACK);
            g2D.setStroke(new BasicStroke(5f));
            g2D.drawImage(pAvatar, avatarRect.x, avatarRect.y, avatarRect.width, avatarRect.height, this);
            g2D.drawRoundRect(avatarRect.x, avatarRect.y, avatarRect.width, avatarRect.height, 16, 16);
            g2D.setFont(Constants.DEBUG_FONT);
            g2D.drawString(gameController.getCurrentPlayer().getNickName(),
                    (int) (avatarRect.getCenterX() - Constants.FFB.getHalfWidthOfString(g2D, gameController.getCurrentPlayer().getNickName())),
                    avatarRect.height + 24);
        }

        if (isAudioSettingsMenuVisible) {
            drawSettingsPart(g2D, 0);
        } else if (isVideoSettingsMenuVisible) {
            drawSettingsPart(g2D, 1);
        } else if (isHotkeysSettingsMenuVisible) {
            drawSettingsPart(g2D, 2);
        } else if (isGameplaySettingsMenuVisible) {
            drawSettingsPart(g2D, 3);
        } else if (isCreatingNewHeroSetVisible) {
            drawSettingsPart(g2D, 4);
        } else if (isCreatingNewWorldSetVisible) {
            drawSettingsPart(g2D, 5);
        } else if (isChooseWorldMenuVisible) {
            drawSettingsPart(g2D, 6);
        } else if (isChooseHeroMenuVisible) {
            drawSettingsPart(g2D, 7);
        }

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.CYAN);
            g2D.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        }

        g2D.dispose();
    }

    private boolean isShadowBackNeeds() {
        return isOptionsMenuSetVisible || isCreatingNewHeroSetVisible || isCreatingNewWorldSetVisible || isChooseWorldMenuVisible
                || isChooseHeroMenuVisible;
    }

    private void recalculateMenuRectangles() {
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

    @Override
    public void mousePressed(MouseEvent e) {

    }

    private void onExitBack() {
        if (isOptionsMenuSetVisible) { // || isStartGameMenuSetVisible
            isOptionsMenuSetVisible = false;
            isAudioSettingsMenuVisible = false;
            isVideoSettingsMenuVisible = false;
            isHotkeysSettingsMenuVisible = false;
            isGameplaySettingsMenuVisible = false;
            isCreatingNewWorldSetVisible = false;
            isCreatingNewHeroSetVisible = false;
        } else if (isCreatingNewHeroSetVisible) {
            isCreatingNewHeroSetVisible = false;
        } else if (isCreatingNewWorldSetVisible) {
            isCreatingNewWorldSetVisible = false;
        } else if (isChooseWorldMenuVisible) {
            isChooseWorldMenuVisible = false;
        } else if (isChooseHeroMenuVisible) {
            isChooseHeroMenuVisible = false;
        } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
            gameController.exitTheGame(null);
        }

        // в любом случае, любая панель тут скрывается:
        hidePanelIfNotNull(audiosPane);
        hidePanelIfNotNull(videosPane);
        hidePanelIfNotNull(hotkeysPane);
        hidePanelIfNotNull(gameplayPane);
        hidePanelIfNotNull(heroCreatingPane);
        hidePanelIfNotNull(worldCreatingPane);
        hidePanelIfNotNull(worldsListPane);
        hidePanelIfNotNull(heroesListPane);
    }

    private void hidePanelIfNotNull(JPanel panel) {
        if (panel != null) {
            panel.setVisible(false);
        }
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
        dropOldPanesFromLayer();
        this.backMenuImage.flush();
        this.backMenuImage.getGraphics().dispose();
        setVisible(false);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        firstButtonOver = firstButtonRect != null && firstButtonRect.contains(e.getPoint());
        secondButtonOver = secondButtonRect != null && secondButtonRect.contains(e.getPoint());
        thirdButtonOver = thirdButtonRect != null && thirdButtonRect.contains(e.getPoint());
        fourthButtonOver = fourthButtonRect != null && fourthButtonRect.contains(e.getPoint());
        exitButtonOver = exitButtonRect != null && exitButtonRect.contains(e.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void componentResized(ComponentEvent e) {
        onResize();
    }

    private void onResize() {
        if (resizeThread != null && resizeThread.isAlive()) {
            return;
        }

        resizeThread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ...
            }

            log.debug("Resizing of menu canvas...");

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(parentFrame.getSize());
            } else if (!getSize().equals(parentFrame.getRootPane().getSize())) {
                setSize(parentFrame.getRootPane().getSize());
            }

            reloadShapes(this);
            recalculateMenuRectangles();

            boolean rect0IsVisible = audiosPane != null && audiosPane.isVisible();
            boolean rect1IsVisible = videosPane != null && videosPane.isVisible();
            boolean rect2IsVisible = hotkeysPane != null && hotkeysPane.isVisible();
            boolean rect3IsVisible = gameplayPane != null && gameplayPane.isVisible();
            boolean rect4IsVisible = heroCreatingPane != null && heroCreatingPane.isVisible();
            boolean rect5IsVisible = worldCreatingPane != null && worldCreatingPane.isVisible();
            boolean rect6IsVisible = worldsListPane != null && worldsListPane.isVisible();
            boolean rect7IsVisible = heroesListPane != null && heroesListPane.isVisible();

            recalculateSettingsPanes();

            isAudioSettingsMenuVisible = rect0IsVisible;
            isVideoSettingsMenuVisible = rect1IsVisible;
            isHotkeysSettingsMenuVisible = rect2IsVisible;
            isGameplaySettingsMenuVisible = rect3IsVisible;
            isCreatingNewHeroSetVisible = rect4IsVisible;
            isCreatingNewWorldSetVisible = rect5IsVisible;
            isChooseWorldMenuVisible = rect6IsVisible;
            isChooseHeroMenuVisible = rect7IsVisible;
        });
        resizeThread.start();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void createNewWorldAndCloseThatPanel(WorldCreatingPane newWorldTemplate) {
        aNewWorldMemory = new WorldDTO();
        aNewWorldMemory.setTitle(newWorldTemplate.getWorldName());
        aNewWorldMemory.setLevel(newWorldTemplate.getHardnessLevel());
        aNewWorldMemory.setNetAvailable(newWorldTemplate.isNetAvailable());
        aNewWorldMemory.setPasswordHash(newWorldTemplate.getNetPasswordHash());

        aNewWorldMemory = gameController.saveNewWorld(aNewWorldMemory);
        isCreatingNewWorldSetVisible = false;
        isCreatingNewHeroSetVisible = true;
    }

    public void createNewHeroForNewWorldAndCloseThatPanel(HeroCreatingPane newHeroTemplate) {
        HeroDTO aNewHeroDto = gameController.saveNewHero(HeroDTO.builder()
                .heroName(newHeroTemplate.getHeroName())
                .ownedPlayer(gameController.getCurrentPlayer())
                .worldUid(newHeroTemplate.getWorldUid())
                .build());

        aNewWorldMemory.addHero(aNewHeroDto);
        aNewWorldMemory = gameController.updateWorld(aNewWorldMemory);
        gameController.setCurrentWorld(aNewWorldMemory);

        playWithThisHero(aNewHeroDto.getUid());
    }

    public List<WorldDTO> getExistsWorlds() {
        return gameController.getExistingWorlds();
    }

    public void createNewHeroForExistsWorldAndCloseThatPanel(UUID selectedWorldUuid) {
        gameController.setLastPlayedWorldUuid(selectedWorldUuid);
        aNewWorldMemory = gameController.getCurrentWorld();

        if (getCurrentWorldHeroes().isEmpty()) {
            isCreatingNewHeroSetVisible = true;
        } else {
            isChooseHeroMenuVisible = true;
        }

        isChooseWorldMenuVisible = false;
        worldsListPane.setVisible(false);
    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameController.deleteWorld(worldUid);
    }

    public Set<HeroDTO> getCurrentWorldHeroes() {
        return gameController.getCurrentWorld() == null
                ? Collections.emptySet() : gameController.getCurrentWorld().getHeroes();
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        gameController.deleteCurrentPlayerHero(heroUid);
    }

    public void playWithThisHero(UUID heroUid) {
        isCreatingNewHeroSetVisible = false;
        heroCreatingPane.setVisible(false);

        gameController.setCurrentHero(heroUid);
        gameController.getCurrentPlayer().setLastPlayedWorldUid(aNewWorldMemory.getUid());
        gameController.loadScreen(ScreenType.GAME_SCREEN, aNewWorldMemory);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (firstButtonOver) {
            if (isOptionsMenuSetVisible) {
                if (!isAudioSettingsMenuVisible) {
                    isAudioSettingsMenuVisible = true;
                    isVideoSettingsMenuVisible = false;
                    isHotkeysSettingsMenuVisible = false;
                    isGameplaySettingsMenuVisible = false;
                }
            } else if (isCreatingNewHeroSetVisible) {
                Constants.showNFP();
            } else if (isChooseWorldMenuVisible) {
                isChooseWorldMenuVisible = false;
                isCreatingNewWorldSetVisible = true;
            } else if (isChooseHeroMenuVisible) {
                isChooseHeroMenuVisible = false;
                isCreatingNewHeroSetVisible = true;
            } else {
                // Попытка начать новую игру:
                List<WorldDTO> worlds = gameController.getExistingWorlds();
                if (worlds.isEmpty()) {
                    // миров нет - создаём:
                    isCreatingNewWorldSetVisible = true;
                    return;
                }
                // отображаем существующие миры для игры:
                isChooseWorldMenuVisible = true;
            }
        }

        if (secondButtonOver) {
            if (isOptionsMenuSetVisible) {
                // нажато Настройки графики:
                if (!isVideoSettingsMenuVisible) {
                    isVideoSettingsMenuVisible = true;
                    isAudioSettingsMenuVisible = false;
                    isHotkeysSettingsMenuVisible = false;
                    isGameplaySettingsMenuVisible = false;
                }
            } else if (isCreatingNewHeroSetVisible) {
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }

        if (thirdButtonOver) {
            if (!isOptionsMenuSetVisible && !isCreatingNewHeroSetVisible && !isChooseWorldMenuVisible) {
                isOptionsMenuSetVisible = true;
                isAudioSettingsMenuVisible = true;
            } else if (isCreatingNewHeroSetVisible) {
                Constants.showNFP();
            } else if (isOptionsMenuSetVisible) {
                if (!isHotkeysSettingsMenuVisible) {
                    isHotkeysSettingsMenuVisible = true;
                    isVideoSettingsMenuVisible = false;
                    isAudioSettingsMenuVisible = false;
                    isGameplaySettingsMenuVisible = false;
                }
            } else if (isChooseWorldMenuVisible) {
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }

        if (fourthButtonOver) {
            if (isOptionsMenuSetVisible) {
                if (!isGameplaySettingsMenuVisible) {
                    isGameplaySettingsMenuVisible = true;
                    isHotkeysSettingsMenuVisible = false;
                    isVideoSettingsMenuVisible = false;
                    isAudioSettingsMenuVisible = false;
                }
            } else {
                Constants.showNFP();
            }
        }

        if (exitButtonOver) {
            onExitBack();
        }
    }

    public WorldDTO getCurrentWorld() {
        return gameController.getCurrentWorld();
    }
}
