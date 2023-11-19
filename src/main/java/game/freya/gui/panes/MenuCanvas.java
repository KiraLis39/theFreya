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
    private transient Thread resizeThread = null;
    private transient WorldDTO aNewWorldMemory;
    private transient Area area;
    private boolean isMenuActive, initialized = false;
    private String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText;
    private JPanel audiosPane, videosPane, hotkeysPane, gameplayPane, heroCreatingPane, worldCreatingPane, worldsListPane, heroesListPane;
    private double parentHeightMemory = 0;
    private byte drawErrorCount = 0;

    public MenuCanvas(JFrame parentFrame, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "MenuCanvas", gameController);
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

            // если изменился размер фрейма:
            if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                log.info("Resizing by parent frame...");
                onResize();
                parentHeightMemory = parentFrame.getBounds().getHeight();
            }

            if (!initialized) {
                init();
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

        if (isOptionsMenuSetVisible()) {
            showOptions(g2D);
        } else if (isCreatingNewWorldSetVisible()) {
            drawHeader(g2D, "Создание мира");

            // creating world buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomButtonText, getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
            g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomButtonText, getFirstButtonRect().x, getFirstButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetButtonText, getSecondButtonRect().x - 1, getSecondButtonRect().y + 17);
            g2D.setColor(isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetButtonText, getSecondButtonRect().x, getSecondButtonRect().y + 18);
        } else if (isChooseWorldMenuVisible()) {
            drawHeader(g2D, "Выбор мира");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
            g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, getFirstButtonRect().x, getFirstButtonRect().y + 18);
        } else if (isChooseHeroMenuVisible()) {
            drawHeader(g2D, "Выбор героя");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
            g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, getFirstButtonRect().x, getFirstButtonRect().y + 18);
        } else if (isCreatingNewHeroSetVisible()) {
            drawHeader(g2D, "Создание героя");

            // creating hero buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomButtonText, getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
            g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomButtonText, getFirstButtonRect().x, getFirstButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetButtonText, getSecondButtonRect().x - 1, getSecondButtonRect().y + 17);
            g2D.setColor(isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetButtonText, getSecondButtonRect().x, getSecondButtonRect().y + 18);
        } else {
            // default buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(startGameButtonText, getFirstButtonRect().x - 1, getFirstButtonRect().y + 17);
            g2D.setColor(isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(startGameButtonText, getFirstButtonRect().x, getFirstButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(coopPlayButtonText, getSecondButtonRect().x - 1, getSecondButtonRect().y + 17);
            g2D.setColor(isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(coopPlayButtonText, getSecondButtonRect().x, getSecondButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(optionsButtonText, getThirdButtonRect().x - 1, getThirdButtonRect().y + 17);
            g2D.setColor(isThirdButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(optionsButtonText, getThirdButtonRect().x, getThirdButtonRect().y + 18);
        }

        g2D.setColor(Color.BLACK);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
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

    private void init() {
        reloadShapes(this);

        startGameButtonText = "Начать игру";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        createNewButtonText = "Создать";
        randomButtonText = "Случайно";
        resetButtonText = "Сброс";

        try {
            Constants.CACHE.addIfAbsent("backMenuImage",
                    ImageIO.read(new File("./resources/images/menu.png")));
            Constants.CACHE.addIfAbsent("backMenuImageShadowed",
                    ImageIO.read(new File("./resources/images/menu_shadowed.png")));
            Constants.CACHE.addIfAbsent("green_arrow",
                    ImageIO.read(new File("./resources/images/green_arrow.png")));
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
        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
        g2D.drawImage((BufferedImage) (isShadowBackNeeds() ? Constants.CACHE.get("backMenuImageShadowed") : Constants.CACHE.get("backMenuImage")),
                0, 0, getWidth(), getHeight(), this);

        // fill left gray polygon:
        drawLeftGrayPoly(g2D);

        // down right corner text:
        g2D.setFont(Constants.INFO_FONT);
        g2D.setColor(Color.WHITE);
        g2D.drawString(getDownInfoString1(),
                (int) (getWidth() - Constants.FFB.getStringBounds(g2D, getDownInfoString1()).getWidth() - 6), getHeight() - 9);
        g2D.drawString(getDownInfoString2(),
                (int) (getWidth() - Constants.FFB.getStringBounds(g2D, getDownInfoString2()).getWidth() - 6), getHeight() - 25);

        // player`s info:
        if (!isOptionsMenuSetVisible() && !isCreatingNewHeroSetVisible() && !isChooseWorldMenuVisible()) {
            if (pAvatar == null) {
                pAvatar = gameController.getCurrentPlayer().getAvatar();
            }
            g2D.setColor(Color.BLACK);
            g2D.setStroke(new BasicStroke(5f));
            g2D.drawImage(pAvatar, getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, this);
            g2D.drawRoundRect(getAvatarRect().x, getAvatarRect().y, getAvatarRect().width, getAvatarRect().height, 16, 16);
            g2D.setFont(Constants.DEBUG_FONT);
            g2D.drawString(gameController.getCurrentPlayer().getNickName(),
                    (int) (getAvatarRect().getCenterX() - Constants.FFB.getHalfWidthOfString(g2D, gameController.getCurrentPlayer().getNickName())),
                    getAvatarRect().height + 24);
        }

        if (isAudioSettingsMenuVisible()) {
            drawSettingsPart(g2D, 0);
        } else if (isVideoSettingsMenuVisible()) {
            drawSettingsPart(g2D, 1);
        } else if (isHotkeysSettingsMenuVisible()) {
            drawSettingsPart(g2D, 2);
        } else if (isGameplaySettingsMenuVisible()) {
            drawSettingsPart(g2D, 3);
        } else if (isCreatingNewHeroSetVisible()) {
            drawSettingsPart(g2D, 4);
        } else if (isCreatingNewWorldSetVisible()) {
            drawSettingsPart(g2D, 5);
        } else if (isChooseWorldMenuVisible()) {
            drawSettingsPart(g2D, 6);
        } else if (isChooseHeroMenuVisible()) {
            drawSettingsPart(g2D, 7);
        }

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.CYAN);
            g2D.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        }

        g2D.dispose();
    }

    private boolean isShadowBackNeeds() {
        return isOptionsMenuSetVisible() || isCreatingNewHeroSetVisible() || isCreatingNewWorldSetVisible()
                || isChooseWorldMenuVisible() || isChooseHeroMenuVisible();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    private void onExitBack() {
        if (isOptionsMenuSetVisible()) {
            setOptionsMenuSetVisible(false);
            setAudioSettingsMenuVisible(false);
            setVideoSettingsMenuVisible(false);
            setHotkeysSettingsMenuVisible(false);
            setGameplaySettingsMenuVisible(false);
            setCreatingNewWorldSetVisible(false);
            setCreatingNewHeroSetVisible(false);
        } else if (isCreatingNewHeroSetVisible()) {
            setCreatingNewHeroSetVisible(false);
        } else if (isCreatingNewWorldSetVisible()) {
            setCreatingNewWorldSetVisible(false);
        } else if (isChooseWorldMenuVisible()) {
            setChooseWorldMenuVisible(false);
        } else if (isChooseHeroMenuVisible()) {
            setChooseHeroMenuVisible(false);
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
        setFirstButtonOver(getFirstButtonRect() != null && getFirstButtonRect().contains(e.getPoint()));
        setSecondButtonOver(getSecondButtonRect() != null && getSecondButtonRect().contains(e.getPoint()));
        setThirdButtonOver(getThirdButtonRect() != null && getThirdButtonRect().contains(e.getPoint()));
        setFourthButtonOver(getFourthButtonRect() != null && getFourthButtonRect().contains(e.getPoint()));
        setExitButtonOver(getExitButtonRect() != null && getExitButtonRect().contains(e.getPoint()));
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
                Thread.currentThread().interrupt();
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

            setAudioSettingsMenuVisible(rect0IsVisible);
            setVideoSettingsMenuVisible(rect1IsVisible);
            setHotkeysSettingsMenuVisible(rect2IsVisible);
            setGameplaySettingsMenuVisible(rect3IsVisible);
            setCreatingNewHeroSetVisible(rect4IsVisible);
            setCreatingNewWorldSetVisible(rect5IsVisible);
            setChooseWorldMenuVisible(rect6IsVisible);
            setChooseHeroMenuVisible(rect7IsVisible);
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
        setCreatingNewWorldSetVisible(false);
        setCreatingNewHeroSetVisible(true);
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
            setCreatingNewHeroSetVisible(true);
        } else {
            setChooseHeroMenuVisible(true);
        }

        setChooseWorldMenuVisible(false);
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
        setCreatingNewHeroSetVisible(false);
        heroCreatingPane.setVisible(false);

        gameController.setCurrentHero(heroUid);
        gameController.getCurrentPlayer().setLastPlayedWorldUid(aNewWorldMemory.getUid());
        gameController.loadScreen(ScreenType.GAME_SCREEN, aNewWorldMemory);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!isAudioSettingsMenuVisible()) {
                    setAudioSettingsMenuVisible(true);
                    setVideoSettingsMenuVisible(false);
                    setHotkeysSettingsMenuVisible(false);
                    setGameplaySettingsMenuVisible(false);
                }
            } else if (isCreatingNewHeroSetVisible()) {
                Constants.showNFP();
            } else if (isChooseWorldMenuVisible()) {
                setChooseWorldMenuVisible(false);
                setCreatingNewWorldSetVisible(true);
            } else if (isChooseHeroMenuVisible()) {
                setChooseHeroMenuVisible(false);
                setCreatingNewHeroSetVisible(true);
            } else {
                // Попытка начать новую игру:
                List<WorldDTO> worlds = gameController.getExistingWorlds();
                if (worlds.isEmpty()) {
                    // миров нет - создаём:
                    setCreatingNewWorldSetVisible(true);
                    return;
                }
                // отображаем существующие миры для игры:
                setChooseWorldMenuVisible(true);
            }
        }

        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                // нажато Настройки графики:
                if (!isVideoSettingsMenuVisible()) {
                    setVideoSettingsMenuVisible(true);
                    setAudioSettingsMenuVisible(false);
                    setHotkeysSettingsMenuVisible(false);
                    setGameplaySettingsMenuVisible(false);
                }
            } else if (isCreatingNewHeroSetVisible()) {
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }

        if (isThirdButtonOver()) {
            if (!isOptionsMenuSetVisible() && !isCreatingNewHeroSetVisible() && !isChooseWorldMenuVisible()) {
                setOptionsMenuSetVisible(true);
                setAudioSettingsMenuVisible(true);
            } else if (isCreatingNewHeroSetVisible()) {
                Constants.showNFP();
            } else if (isOptionsMenuSetVisible()) {
                if (!isHotkeysSettingsMenuVisible()) {
                    setHotkeysSettingsMenuVisible(true);
                    setVideoSettingsMenuVisible(false);
                    setAudioSettingsMenuVisible(false);
                    setGameplaySettingsMenuVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!isGameplaySettingsMenuVisible()) {
                    setGameplaySettingsMenuVisible(true);
                    setHotkeysSettingsMenuVisible(false);
                    setVideoSettingsMenuVisible(false);
                    setAudioSettingsMenuVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isExitButtonOver()) {
            onExitBack();
        }
    }

    public WorldDTO getCurrentWorld() {
        return gameController.getCurrentWorld();
    }
}
