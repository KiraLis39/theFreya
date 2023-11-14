package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import fox.components.tools.VerticalFlowLayout;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvas extends FoxCanvas {
    private final transient GameController gameController;
    private final transient JFrame parentFrame;
    private boolean isMenuActive;
    private String downInfoString1, downInfoString2;
    private String startGameButtonText, startNewGameButtonText, coopPlayButtonText, optionsButtonText, exitButtonText, backButtonText,
            randomHeroButtonText, resetHeroButtonText, completeHeroButtonText;
    private String continueGameButtonText, startWithNewPlayerGameButtonText, createNewWorldGameButtonText;
    private String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;
    private Rectangle firstButtonRect, secondButtonRect, thirdButtonRect, fourthButtonRect, exitButtonRect;
    private transient VolatileImage backMenuImage;
    private transient BufferedImage pAvatar;
    private boolean initialized = false;
    private boolean isStartGameMenuSetVisible = false, isOptionsMenuSetVisible = false, isCreatingNewHeroSetVisible = false;
    private boolean isAudioSettingsMenuVisible = false, isVideoSettingsMenuVisible = false,
            isHotkeysSettingsMenuVisible = false, isGameplaySettingsMenuVisible = false;

    private Area area;
    private JPanel hotkeysPane;
    private double parentHeightMemory = 0;
    private byte drawErrorCount = 0;


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

        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isMenuActive = true;

        while (isMenuActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            try {
                if (!initialized) {
                    init();
                }

                // если изменился размер фрейма:
                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                    log.info("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = parentFrame.getBounds().getHeight();
                }

                if (getBufferStrategy() == null) {
                    createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
                }

                do {
                    Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                    // g2D.clearRect(0, 0, getWidth(), getHeight());
                    Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);
                    drawBackground(g2D);
                    drawMenu(g2D);
                    if (Constants.isDebugInfoVisible()) {
                        super.drawDebugInfo(g2D, null, 0);
                    }
                    if (Constants.isFpsInfoVisible()) {
                        super.drawFps(g2D);
                    }

                    g2D.dispose();
                } while (getBufferStrategy().contentsRestored() || getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
                drawErrorCount++;
                if (drawErrorCount > 100) {
                    new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                            "Что-то не так с графической системой. Передайте последний лог (error.*) разработчику для решения проблемы.",
                            FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
                    gameController.exitTheGame(null);
                    throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
                }
            }

            if (Constants.isFrameLimited() && Constants.getDiscreteDelay() > 1) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void drawBackground(Graphics2D g2D) {
        if (backMenuImage.validate(Constants.getGraphicsConfiguration()) != VolatileImage.IMAGE_OK) {
            recreateBackImage();
        }
        // draw background image:
        g2D.drawImage(backMenuImage, 0, 0, getWidth(), getHeight(), this);
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
        } else if (isStartGameMenuSetVisible) {
            drawHeader(g2D, "Запуск игры");

            // new game or continue buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(gameController.getLastPlayedWorldUuid() != null ? continueGameButtonText : startNewGameButtonText,
                    firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(gameController.getLastPlayedWorldUuid() != null ? continueGameButtonText : startNewGameButtonText,
                    firstButtonRect.x, firstButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(startWithNewPlayerGameButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
            g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(startWithNewPlayerGameButtonText, secondButtonRect.x, secondButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewWorldGameButtonText, thirdButtonRect.x - 1, thirdButtonRect.y + 17);
            g2D.setColor(thirdButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewWorldGameButtonText, thirdButtonRect.x, thirdButtonRect.y + 18);
        } else if (isCreatingNewHeroSetVisible) {
            drawHeader(g2D, "Создание героя");

            // creating hero buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomHeroButtonText, firstButtonRect.x - 1, firstButtonRect.y + 17);
            g2D.setColor(firstButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomHeroButtonText, firstButtonRect.x, firstButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetHeroButtonText, secondButtonRect.x - 1, secondButtonRect.y + 17);
            g2D.setColor(secondButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetHeroButtonText, secondButtonRect.x, secondButtonRect.y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(completeHeroButtonText, thirdButtonRect.x - 1, thirdButtonRect.y + 17);
            g2D.setColor(thirdButtonOver ? Color.GREEN : Color.WHITE);
            g2D.drawString(completeHeroButtonText, thirdButtonRect.x, thirdButtonRect.y + 18);
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
        g2D.drawString(isOptionsMenuSetVisible || isStartGameMenuSetVisible
                ? backButtonText : exitButtonText, exitButtonRect.x - 1, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(isOptionsMenuSetVisible || isStartGameMenuSetVisible
                ? backButtonText : exitButtonText, exitButtonRect.x, exitButtonRect.y + 18);
    }

    private void drawSettingsPart(Graphics2D g2D, int partIndex) {
        Font font = g2D.getFont();

        if (area == null || area.getBounds().getHeight() != getBounds().getHeight()) {
            area = new Area(getBounds());
            area.subtract(new Area(super.getLeftGrayMenuPoly()));
        }

//        g2D.setColor(Color.RED);
//        g2D.setStroke(new BasicStroke(2f));

        switch (partIndex) {
            case 0 -> {
                // audio:
                hotkeysPane.setVisible(false);
            }
            case 1 -> {
                // video:
                hotkeysPane.setVisible(false);
            }
            case 2 -> {
                // hotkeys:
                hotkeysPane.setVisible(true);
            }
            case 3 -> {
                // gameplay:
                hotkeysPane.setVisible(false);
            }
            default -> {
                hotkeysPane.setVisible(false);
            }
        }

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

        g2D.setColor(Color.BLACK);
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);
        g2D.drawString(headerTitle, getWidth() / 10, (int) (getHeight() * 0.034D));
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

        continueGameButtonText = "Продолжить игру";
        startWithNewPlayerGameButtonText = "Создать героя";
        createNewWorldGameButtonText = "Создать свой мир";

        audioSettingsButtonText = "Настройки звука";
        videoSettingsButtonText = "Настройки графики";
        hotkeysSettingsButtonText = "Управление";
        gameplaySettingsButtonText = "Геймплей";

        randomHeroButtonText = "Случайно";
        resetHeroButtonText = "Сброс";
        completeHeroButtonText = "Принять";

        try {
            Constants.CACHE.addIfAbsent("backMenuImage",
                    ImageIO.read(new File("./resources/images/demo_menu.jpg")));
            Constants.CACHE.addIfAbsent("backMenuImageShadowed",
                    ImageIO.read(new File("./resources/images/demo_menu_shadowed.jpg")));
            recreateBackImage();
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        recalculateMenuRectangles();

        recalculateSettingsPanes();

        this.initialized = true;
    }

    private void recalculateSettingsPanes() {
        try {
            parentFrame.getLayeredPane().remove(hotkeysPane);
        } catch (NullPointerException npe) {
            log.debug("Не удастся удалить из фрейма панель, которой там нет.");
        }
        hotkeysPane = new JPanel() {
            private BufferedImage shap;

            {
                setVisible(false);
                setSize(new Dimension((int) (MenuCanvas.this.getWidth() * 0.66d), MenuCanvas.this.getHeight() - 2));
                setLocation((int) (MenuCanvas.this.getWidth() * 0.34d), 0);
                setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 6, 6));

                for (UserConfig.HotKeys key : UserConfig.HotKeys.values()) {
                    add(new JLabel(key.getDescription()) {{
                        setSize(120, 30);
                        setLocation(9, 10);
                        setForeground(Color.WHITE);
                    }});
                    add(new JTextField(key.getMask() != 0 ? (KeyEvent.getModifiersExText(key.getMask()) + "+")
                            : KeyEvent.getKeyText(key.getEvent()), 10) {{
                        setHorizontalAlignment(CENTER);
                        setFocusable(false);
                        setEditable(false);
                        addMouseListener(MenuCanvas.this);
                    }});
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                }
            }

            @Override
            public void paintComponent(Graphics g) {
                if (shap == null) {
                    shap = backMenuImage.getSnapshot().getSubimage(
                            (int) (backMenuImage.getWidth() * 0.335d), 0,
                            (int) (backMenuImage.getWidth() - backMenuImage.getWidth() * 0.3345d), backMenuImage.getHeight());
                }
                g.drawImage(shap, 0, 0, getWidth(), getHeight(), this);
            }
        };
        parentFrame.getLayeredPane().add(hotkeysPane, Integer.valueOf(1));
        parentFrame.getLayeredPane().setPosition(hotkeysPane, 2);
        parentFrame.getLayeredPane().setPosition(parentFrame, -1);
    }

    private void recreateBackImage() {
        backMenuImage = createVolatileImage(getWidth(), getHeight());
        Graphics2D g2D = backMenuImage.createGraphics();
        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED);
        g2D.drawImage((BufferedImage) (isOptionsMenuSetVisible || isCreatingNewHeroSetVisible
                        ? Constants.CACHE.get("backMenuImageShadowed") : Constants.CACHE.get("backMenuImage")),
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
        if (!isOptionsMenuSetVisible && !isCreatingNewHeroSetVisible) {
            if (pAvatar == null) {
                pAvatar = gameController.getCurrentPlayer().getAvatar();
            }
            g2D.setColor(Color.BLACK);
            g2D.setStroke(new BasicStroke(5f));
            g2D.drawImage(pAvatar, getWidth() - 133, 6, 128, 128, this);
            g2D.drawRoundRect(getWidth() - 133, 6, 128, 128, 16, 16);
            g2D.setFont(Constants.DEBUG_FONT);
            g2D.drawString(gameController.getCurrentPlayer().getNickName(),
                    (int) (getWidth() - 66 - Constants.FFB.getStringBounds(g2D, gameController.getCurrentPlayer().getNickName()).getWidth() / 2),
                    152);
        }

        if (isAudioSettingsMenuVisible) {
            drawSettingsPart(g2D, 0);
        } else if (isVideoSettingsMenuVisible) {
            drawSettingsPart(g2D, 1);
        } else if (isHotkeysSettingsMenuVisible) {
            drawSettingsPart(g2D, 2);
        } else if (isGameplaySettingsMenuVisible) {
            drawSettingsPart(g2D, 3);
        }

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.CYAN);
            g2D.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        }

        g2D.dispose();
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
    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (firstButtonOver) {
            if (isStartGameMenuSetVisible) {
                // нажато Начать новую игру или Продолжить игру:
                if (gameController.getCurrentPlayer().getHeroes().isEmpty()) {
                    // создаём нового героя:
                    isStartGameMenuSetVisible = false;
                    isCreatingNewHeroSetVisible = true;
                } else {
                    gameController.createTheWorldIfAbsent();
                    gameController.loadScreen(ScreenType.GAME_SCREEN, gameController.getLastPlayedWorld());
                }
            } else if (isOptionsMenuSetVisible) {
                if (!isAudioSettingsMenuVisible) {
                    isAudioSettingsMenuVisible = true;
                    isVideoSettingsMenuVisible = false;
                    isHotkeysSettingsMenuVisible = false;
                    isGameplaySettingsMenuVisible = false;
                } else {
                    Constants.showNFP();
                }
            } else if (isCreatingNewHeroSetVisible) {
                Constants.showNFP();
            } else {
                // нажато Начать игру:
                isStartGameMenuSetVisible = true;
            }
        }

        if (secondButtonOver) {
            if (isStartGameMenuSetVisible) {
                if (!isCreatingNewHeroSetVisible) {
                    // создаём нового героя:
                    isCreatingNewHeroSetVisible = true;
                    isStartGameMenuSetVisible = false;
                } else {
                    // нажато Создать героя:
                    Constants.showNFP();
                    // if ((int) new FOptionPane().buildFOptionPane("Уверены?",
                    //     "Начать новую игру новым персонажем?", FOptionPane.TYPE.YES_NO_TYPE).get() == 0
                    // ) {
                    //    gameController.resetCurrentPlayer();
                    //    gameController.loadScreen(ScreenType.GAME_SCREEN, gameController.getLastPlayedWorld());
                    // }
                }
            } else if (isOptionsMenuSetVisible) {
                // нажато Настройки графики:
                if (!isVideoSettingsMenuVisible) {
                    isVideoSettingsMenuVisible = true;
                    isAudioSettingsMenuVisible = false;
                    isHotkeysSettingsMenuVisible = false;
                    isGameplaySettingsMenuVisible = false;
                } else {
                    Constants.showNFP();
                }
            } else if (isCreatingNewHeroSetVisible) {
                // нажато Сбросить героя:
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }

        if (thirdButtonOver) {
            if (!isOptionsMenuSetVisible && !isStartGameMenuSetVisible && !isCreatingNewHeroSetVisible) {
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
                } else {
                    Constants.showNFP();
                }
            }
            // FoxTip плохо подходит для Rectangle т.к. нет возможности получить абсолютные координаты:
            // new Color(102, 242, 223), new Color(157, 159, 201)
//            FoxTip tip = new FoxTip(FoxTip.TYPE.INFO, null, "Не реализовано",
//                    "Данный функционал ещё находится\nв разработке.", "Приносим свои извинения", optionsButtonRect);
//            tip.showTip();
        }

        if (fourthButtonOver) {
            if (isOptionsMenuSetVisible) {
                if (!isGameplaySettingsMenuVisible) {
                    isGameplaySettingsMenuVisible = true;
                    isHotkeysSettingsMenuVisible = false;
                    isVideoSettingsMenuVisible = false;
                    isAudioSettingsMenuVisible = false;
                } else {
                    Constants.showNFP();
                }
            } else {
                Constants.showNFP();
            }
        }

        if (exitButtonOver) {
            if (isOptionsMenuSetVisible || isStartGameMenuSetVisible) {
                isOptionsMenuSetVisible = false;
                isAudioSettingsMenuVisible = false;
                isVideoSettingsMenuVisible = false;
                isHotkeysSettingsMenuVisible = false;
                isGameplaySettingsMenuVisible = false;
                isStartGameMenuSetVisible = false;
            } else if (isCreatingNewHeroSetVisible) {
                isCreatingNewHeroSetVisible = false;
                isStartGameMenuSetVisible = true;
            } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
            ) {
                gameController.exitTheGame(null);
            }

            // в любом случае, любая панель тут скрывается:
            hotkeysPane.setVisible(false);
        }
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
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
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ...
            }
            log.debug("Resizing of menu canvas...");
            parentFrame.revalidate();

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(parentFrame.getSize());
            } else {
                setSize(parentFrame.getRootPane().getSize());
            }
            setLocation(0, 0);
            revalidate();

            reloadShapes(this);
            recalculateMenuRectangles();

            boolean rectIsVisible = hotkeysPane.isVisible();
            recalculateSettingsPanes();
            hotkeysPane.setVisible(rectIsVisible);
            isHotkeysSettingsMenuVisible = rectIsVisible;
            log.info("*** Frame in fullscreen mode: {}, Settings pane visible: {}, Optimode: {}",
                    Constants.getUserConfig().isFullscreen(), hotkeysPane.isVisible(), parentFrame.getLayeredPane().isOptimizedDrawingEnabled());
        }).start();
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
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
