package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.sub.AudioSettingsPane;
import game.freya.gui.panes.sub.GameplaySettingsPane;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.HotkeysSettingsPane;
import game.freya.gui.panes.sub.VideoSettingsPane;
import game.freya.gui.panes.sub.WorldCreatingPane;
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

import static javax.swing.JLayeredPane.PALETTE_LAYER;

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
    private boolean isStartGameMenuSetVisible = false, isOptionsMenuSetVisible = false,
            isCreatingNewHeroSetVisible = false, isCreatingNewWorldSetVisible = false;
    private boolean isAudioSettingsMenuVisible = false, isVideoSettingsMenuVisible = false,
            isHotkeysSettingsMenuVisible = false, isGameplaySettingsMenuVisible = false;

    private Area area;
    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane;
    private double parentHeightMemory = 0;
    private byte drawErrorCount = 0;
    private transient Thread resizeThread = null;

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
                        onExitBack();
                    }
                });
    }

    @Override
    public void run() {
        this.isMenuActive = true;

        while (isMenuActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            Graphics2D g2D = null;
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
                    g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                    Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);

                    drawBackground(g2D);
                    drawMenu(g2D);

                    if (Constants.isDebugInfoVisible()) {
                        super.drawDebugInfo(g2D, null, 0);
                    }

                    if (Constants.isFpsInfoVisible()) {
                        super.drawFps(g2D);
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

        audiosPane.setVisible(partIndex == 0);
        videosPane.setVisible(partIndex == 1);
        hotkeysPane.setVisible(partIndex == 2);
        gameplayPane.setVisible(partIndex == 3);
        heroCreatingPane.setVisible(partIndex == 4);
        worldCreatingPane.setVisible(partIndex == 5);

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

        // добавляем панели на слой:
        try {
            parentFrame.getLayeredPane().add(audiosPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(videosPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(hotkeysPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(gameplayPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(heroCreatingPane, PALETTE_LAYER);
            parentFrame.getLayeredPane().add(worldCreatingPane, PALETTE_LAYER);
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
        } else if (isCreatingNewHeroSetVisible) {
            drawSettingsPart(g2D, 4);
        } else if (isCreatingNewWorldSetVisible) {
            drawSettingsPart(g2D, 5);
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
                }
            } else {
                Constants.showNFP();
            }
        }

        if (exitButtonOver) {
            onExitBack();
        }
    }

    private void onExitBack() {
        if (isOptionsMenuSetVisible || isStartGameMenuSetVisible) {
            isOptionsMenuSetVisible = false;
            isAudioSettingsMenuVisible = false;
            isVideoSettingsMenuVisible = false;
            isHotkeysSettingsMenuVisible = false;
            isGameplaySettingsMenuVisible = false;
            isCreatingNewWorldSetVisible = false;
            isCreatingNewHeroSetVisible = false;
            isStartGameMenuSetVisible = false;
        } else if (isCreatingNewHeroSetVisible) {
            isCreatingNewHeroSetVisible = false;
            isStartGameMenuSetVisible = true;
        } else if (isCreatingNewWorldSetVisible) {
            isCreatingNewWorldSetVisible = false;
            isStartGameMenuSetVisible = true;
        } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
            gameController.exitTheGame(null);
        }

        // в любом случае, любая панель тут скрывается:
        audiosPane.setVisible(false);
        videosPane.setVisible(false);
        hotkeysPane.setVisible(false);
        gameplayPane.setVisible(false);
        heroCreatingPane.setVisible(false);
        worldCreatingPane.setVisible(false);
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

            recalculateSettingsPanes();

            isAudioSettingsMenuVisible = rect0IsVisible;
            isVideoSettingsMenuVisible = rect1IsVisible;
            isHotkeysSettingsMenuVisible = rect2IsVisible;
            isGameplaySettingsMenuVisible = rect3IsVisible;
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
}
