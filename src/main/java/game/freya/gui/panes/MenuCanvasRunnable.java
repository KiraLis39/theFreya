package game.freya.gui.panes;

import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.NetworkListPane;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvasRunnable extends RunnableCanvasPanel {
    private final GameControllerService gameControllerService;
    private final CharacterService characterService;

    private Thread resizeThread = null;

    private volatile boolean isMenuActive, initialized = false;

    private double parentHeightMemory = 0;

    public MenuCanvasRunnable(
            UIHandler uiHandler,
            JFrame parentFrame,
            GameControllerService gameControllerService,
            CharacterService characterService,
            ApplicationProperties props
    ) {
        super("MenuCanvas", gameControllerService, characterService, parentFrame, uiHandler, props);
        this.gameControllerService = gameControllerService;
        this.characterService = characterService;
        setParentFrame(parentFrame);

        setSize(parentFrame.getSize());
        setBackground(Color.DARK_GRAY.darker());
        setIgnoreRepaint(true);
        setOpaque(false);
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        if (gameControllerService.getServer() != null && gameControllerService.getServer().isOpen()) {
            gameControllerService.closeServer();
            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
        }
        if (gameControllerService.getLocalSocketConnection() != null && gameControllerService.getLocalSocketConnection().isOpen()) {
            gameControllerService.getLocalSocketConnection().close();
            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
        }

        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        runSecondThread();
    }

    private void runSecondThread() {
        setSecondThread("Menu second thread", new Thread(() -> {
            if (!initialized) {
                init();
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (isMenuActive && !getSecondThread().isInterrupted()) {
                // если изменился размер фрейма:
                if (getParentFrame().getBounds().getHeight() != parentHeightMemory) {
                    log.debug("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = getParentFrame().getBounds().getHeight();
                }

                try {
                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Завершена работа вспомогательного потока Меню.");
        }));
        getSecondThread().setUncaughtExceptionHandler((_, e) ->
                log.error("Ошибка вспомогательного потока главного меню: {}", ExceptionUtils.getFullExceptionMessage(e)));
        getSecondThread().start();
    }

    private void inAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.set(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, frameName, "backFunction",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isVisible()) {
                            onExitBack(MenuCanvasRunnable.this);
                        }
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "enterNextFunction",
                KeyEvent.VK_ENTER, 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (getHeroesListPane().isVisible()) {
                            playWithThisHero(gameControllerService.getMyCurrentWorldHeroes().getFirst());
                            getHeroesListPane().setVisible(false);
                        } else if (getWorldsListPane().isVisible()) {
                            UUID lastWorldUid = gameControllerService.getPlayerService().getCurrentPlayer().getLastPlayedWorldUid();
                            if (gameControllerService.getWorldService().isWorldExist(lastWorldUid)) {
                                chooseOrCreateHeroForWorld(lastWorldUid);
                            } else {
                                chooseOrCreateHeroForWorld(
                                        gameControllerService.findAllWorldsByNetworkAvailable(false).getFirst().getUid());
                            }
                        } else {
                            getWorldsListPane().setVisible(true);
                        }
                    }
                });
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        long delta;

        // ждём пока компонент не станет виден:
        while (getParent() == null || !isDisplayable() || !initialized) {
            Thread.yield();
            if (System.currentTimeMillis() - lastTime > 3_000) {
                lastTime = System.currentTimeMillis();
                log.error("Не удалось запустить поток {} за отведённое время!", getName());
                if (!getSecondThread().isAlive()) {
                    runSecondThread();
                }
            }
        }

        this.isMenuActive = true;
        while (isMenuActive && !Thread.currentThread().isInterrupted()) {
            delta = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();

            try {
                if (isVisible() && isDisplayable()) {
                    drawNextFrame(delta);
                }

                // продвигаем кадры вспомогательной анимации:
                doAnimate();

                // при успешной отрисовке:
                if (getDrawErrors() > 0) {
                    decreaseDrawErrorCount();
                }
            } catch (Exception e) {
                throwExceptionAndYield(e);
            }

            if (Constants.isFpsLimited()) {
                delayDrawing(delta);
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void doAnimate() {
        if (getNetworkListPane().isVisible()) {
            if (Constants.isConnectionAwait()) {
                getNetworkListPane().repaint();
            }
            if (Constants.isPingAwait()) {
                getNetworkListPane().repaint();
            }
        }
    }

    private void drawNextFrame(long delta) {
        repaint(); // repaint(delta);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;
        try {
            super.drawBackground(g2D);
        } catch (AWTException e) {
            log.error("Ошибка отрисовки кадра игры: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
        g2D.dispose();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        onResize();
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

    private void onResize() {
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
            }

            reloadShapes(this);
            recalculateMenuRectangles();

            setRevolatileNeeds(true);
        });
        resizeThread.start();
        try {
            resizeThread.join(500);
        } catch (InterruptedException e) {
            resizeThread.interrupt();
        }
    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameControllerService.deleteWorld(worldUid);
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        gameControllerService.getCharacterService().deleteByUuid(heroUid);
    }

    public void openCreatingNewHeroPane(PlayCharacterDto template) {
        getHeroesListPane().setVisible(false);
        getHeroCreatingPane().setVisible(true);
        if (template != null) {
            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
        }
    }

    public void exitTheGame() {
        stop();
        gameControllerService.exitTheGame(null, 0);
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
        closeBackImage();
        setVisible(false);
    }

    @Override
    public void init() {
        inAc();
        setVisible(true);

        recalculateMenuRectangles();
        createSubPanes();
        reloadShapes(this);

        this.initialized = true;
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

    @Override
    public void mouseDragged(MouseEvent e) {

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
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
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
                openCreatingNewHeroPane(null);
            } else if (getNetworkListPane().isVisible()) {
                getNetworkListPane().setVisible(false);
                getNetworkCreatingPane().setVisible(true);
            } else {
                if (gameControllerService.findAllWorldsByNetworkAvailable(false).isEmpty()) {
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
                ((NetworkListPane) getNetworkListPane()).reloadNet(this);
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
            onExitBack(this);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


    // LOCAL game methods:

    /**
     * Когда создаём локальный, несетевой мир - идём сюда, для его сохранения и указания как текущий мир в контроллере.
     *
     * @param newWorld модель нового мира для сохранения.
     */
    public void saveNewLocalWorldAndCreateHero(WorldDto newWorld) {
        gameControllerService.setCurrentWorld(gameControllerService.saveNewWorld(newWorld).getUid());
        chooseOrCreateHeroForWorld(gameControllerService.getWorldService().getCurrentWorld().getUid());
    }


    // BASE game methods:

    /**
     * Приходим сюда для создания нового героя для мира.
     *
     * @param newHeroTemplate модель нового героя для игры в новом мире.
     */
    public void saveNewHeroAndPlay(HeroCreatingPane newHeroTemplate) {
        // сохраняем нового героя и...
        PlayCharacterDto aNewToSave = PlayCharacterDto.builder()
                .uid(UUID.randomUUID())
                .name(newHeroTemplate.getHeroName())
                .ownerUid(Constants.getUserConfig().getUserId())
                .createdBy(gameControllerService.getPlayerService().getCurrentPlayer().getUid())
                .worldUid(newHeroTemplate.getWorldUid())
                .baseColor(newHeroTemplate.getBaseColor())
                .secondColor(newHeroTemplate.getSecondColor())
                .corpusType(newHeroTemplate.getChosenCorpusType())
                .peripheralType(newHeroTemplate.getChosenPeriferiaType())
                .peripheralSize(newHeroTemplate.getPeriferiaSize())
                .createdDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .build();

        // проставляем как текущего:
        gameControllerService.getPlayerService().getCurrentPlayer()
                .setCurrentActiveHero(gameControllerService.getCharacterService().justSaveAnyHero(aNewToSave));

        // если подключение к Серверу уже закрылось пока мы собирались:
//        if (gameController.isCurrentWorldIsNetwork() && !gameController.isServerIsOpen()) {
//            log.warn("Сервер уже закрыт. Требуется повторное подключение.");
//            getHeroCreatingPane().setVisible(false);
//            getHeroesListPane().setVisible(false);
//            getNetworkListPane().setVisible(true);
//            return;
//        }

//        playWithThisHero(gameController.getCurrentHero());
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
        gameControllerService.setCurrentHero(hero);

        // если этот мир по сети:
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            // шлем на Сервер своего выбранного Героя:
            if (gameControllerService.getLocalSocketConnection().registerOnServer()) {
//                gameControllerService.getPlayedHeroes().addHero(characterService.getCurrentHero());
                startGame();
            } else {
                log.error("Сервер не принял нашего Героя: {}", gameControllerService.getLocalSocketConnection().getLastExplanation());
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

    public void startGame() {
        getHeroCreatingPane().setVisible(false);
        getHeroesListPane().setVisible(false);

        log.info("Подготовка к запуску игры должна была пройти успешно. Запуск игрового мира...");
//        gameController.loadScreen(ScreenType.GAME_SCREEN);
    }
}
