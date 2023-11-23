package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.NetworkListPane;
import game.freya.gui.panes.sub.templates.WorldCreator;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvas extends FoxCanvas {
    private final transient GameController gameController;
    private final transient JFrame parentFrame;
    private transient Thread resizeThread = null;
    private volatile boolean isMenuActive, initialized = false;
    private double parentHeightMemory = 0;

    public MenuCanvas(UIHandler uiHandler, JFrame parentFrame, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "MenuCanvas", gameController, parentFrame.getLayeredPane(), uiHandler);
        this.gameController = gameController;
        this.parentFrame = parentFrame;

        setSize(parentFrame.getLayeredPane().getSize());
        setBackground(Color.DARK_GRAY.darker());
        setIgnoreRepaint(true);
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        new Thread(this).start();

        // запуск вспомогательного потока процессов игры:
        setSecondThread("Menu second thread", new Thread(() -> {
            if (!initialized) {
                init();
            }

            while (isMenuActive && !getSecondThread().isInterrupted()) {
                // если изменился размер фрейма:
                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                    log.debug("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = parentFrame.getBounds().getHeight();
                }

                try {
                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
        getSecondThread().start();
    }

    private void init() {
        reloadShapes(this);
        recalculateMenuRectangles();
        recreateSubPanes();
        inAc();

        setVisible(true);
        createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
        this.initialized = true;
    }

    private void inAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.set(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, frameName, "backFunction",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isVisible()) {
                            onExitBack(MenuCanvas.this);
                        }
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "enterNextFunction",
                KeyEvent.VK_ENTER, 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (getHeroesListPane().isVisible()) {
                            playWithThisHero(
                                    gameController.findAllHeroesByWorldUid(gameController.getCurrentWorldUid()).get(0));
                        } else if (getWorldsListPane().isVisible()) {
                            getOrCreateHeroForSelectedWorldAndCloseThat(
                                    gameController.findAllWorldsByNetworkAvailable(false).get(0).getUid());
                        } else {
                            getWorldsListPane().setVisible(true);
                        }
                    }
                });
    }

    @Override
    public void run() {
        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable() || !initialized) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 9_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        this.isMenuActive = true;
        while (isMenuActive) {
            if (!parentFrame.isActive()) {
                Thread.yield();
                continue;
            }

            try {
                drawNextFrame();
            } catch (Exception e) {
                throwExceptionAndYield(e);
            }

            if (Constants.isFpsLimited()) {
                doDrawDelay();
            }

            // при успешной отрисовке:
            if (getDrawErrorCount() > 0) {
                decreaseDrawErrorCount();
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void drawNextFrame() {
        do {
            do {
                Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                super.drawBackground(g2D);
                g2D.dispose();
            } while (getBufferStrategy().contentsRestored());
            getBufferStrategy().show();
        } while (getBufferStrategy().contentsLost());
    }

    private void throwExceptionAndYield(Exception e) {
        log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        increaseDrawErrorCount(); // при неуспешной отрисовке
        if (getDrawErrorCount() > 100) {
            new FOptionPane().buildFOptionPane("Неизвестная ошибка:",
                    "Что-то не так с графической системой. Передайте последний лог (error.*) разработчику для решения проблемы.",
                    FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
//                    gameController.exitTheGame(null);
            throw new GlobalServiceException(ErrorMessages.DRAW_ERROR, ExceptionUtils.getFullExceptionMessage(e));
        }
        Thread.yield();
    }

    private void doDrawDelay() {
        try {
            if (Constants.getDelay() > 1) {
                Thread.sleep(Constants.getDelay());
            } else {
                Thread.yield();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
    public void componentResized(ComponentEvent e) {
        onResize();
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

            if (Constants.getUserConfig().isFullscreen()
                    && Constants.getUserConfig().getFullscreenType().equals(UserConfig.FullscreenType.EXCLUSIVE)
            ) {
                setSize(parentFrame.getSize());
            } else {
                setSize(parentFrame.getRootPane().getSize());
            }

            reloadShapes(this);
            recalculateMenuRectangles();

            boolean rect0IsVisible = getAudiosPane() != null && getAudiosPane().isVisible();
            boolean rect1IsVisible = getVideosPane() != null && getVideosPane().isVisible();
            boolean rect2IsVisible = getHotkeysPane() != null && getHotkeysPane().isVisible();
            boolean rect3IsVisible = getGameplayPane() != null && getGameplayPane().isVisible();
            boolean rect4IsVisible = getHeroCreatingPane() != null && getHeroCreatingPane().isVisible();
            boolean rect5IsVisible = getWorldCreatingPane() != null && getWorldCreatingPane().isVisible();
            boolean rect6IsVisible = getWorldsListPane() != null && getWorldsListPane().isVisible();
            boolean rect7IsVisible = getHeroesListPane() != null && getHeroesListPane().isVisible();
            boolean rect8IsVisible = getNetworkListPane() != null && getNetworkListPane().isVisible();
            boolean rect9IsVisible = getNetworkCreatingPane() != null && getNetworkCreatingPane().isVisible();

            // пересоздание доп-панелей:
            recreateSubPanes();

            getAudiosPane().setVisible(rect0IsVisible);
            getVideosPane().setVisible(rect1IsVisible);
            getHotkeysPane().setVisible(rect2IsVisible);
            getGameplayPane().setVisible(rect3IsVisible);
            getHeroCreatingPane().setVisible(rect4IsVisible);
            getWorldCreatingPane().setVisible(rect5IsVisible);
            getWorldsListPane().setVisible(rect6IsVisible);
            getHeroesListPane().setVisible(rect7IsVisible);
            getNetworkListPane().setVisible(rect8IsVisible);
            getNetworkCreatingPane().setVisible(rect9IsVisible);

            setRevolatileNeeds(true);
        });
        resizeThread.start();
        try {
            resizeThread.join(500);
        } catch (InterruptedException e) {
            resizeThread.interrupt();
        }
    }

    /**
     * Когда создаём новый мир - идём сюда, для его сохранения и указания как текущий мир в контроллере.
     *
     * @param newWorldTemplate модель нового мира для сохранения.
     */
    public void createNewWorldAndCloseThatPanel(WorldCreator newWorldTemplate) {
        WorldDTO aNewWorld = WorldDTO.builder()
                .title(newWorldTemplate.getWorldName())
                .level(newWorldTemplate.getHardnessLevel())
                .isNetAvailable(newWorldTemplate.isNetAvailable())
                .passwordHash(newWorldTemplate.getNetPasswordHash())
                .build();

        gameController.setCurrentWorld(gameController.saveNewWorld(aNewWorld));

        // скрываем панель создания мира, показываем панель создания первого героя для нового мира:
        getWorldCreatingPane().setVisible(false);
        getHeroCreatingPane().setVisible(true);
    }

    public void connectToWorldAndCloseThatPanel(@NotNull NetworkListPane connectionTemplate) {
        setConnectionAwait(true);

        // 1) приходим сюда с host:port для подключения
        String address = connectionTemplate.getAddress().trim();

        try {
            String h, password;
            Integer p;
            if (!address.isBlank()) {
                password = connectionTemplate.getPassword();
                h = address.contains(":") ? address.split(":")[0] : address;
                p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;

                // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
                if (gameController.connectToServer(h.trim(), p, password)) {
                    // здесь уже проставлен currentWorld с сервера:
                    setConnectionAwait(false);
                    getNetworkListPane().setVisible(false);

                    // 3) если у нас нет героя в этом мире - создаём
                    if (gameController.findAllHeroesByWorldUid(gameController.getCurrentWorldUid()).isEmpty()) {
                        getHeroCreatingPane().setVisible(true);
                    } else {
                        getHeroesListPane().setVisible(true);
                    }
                } else {
                    setConnectionAwait(false);
                    new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
                    throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, "connect to remote game");
                }
            }
        } catch (ConnectException ce) {
            new FOptionPane().buildFOptionPane("Не доступно:", "Адрес не доступен.", FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.warn("Server address connection failed: {}", ExceptionUtils.getFullExceptionMessage(ce));
        } catch (Exception e) {
            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка адреса подключения '%s'.\n"
                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10:13958)")
                    .formatted(e.getMessage()), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    /**
     * После выбора уже существующего мира - приходим сюда для создания нового героя или выбора
     * существующего, для игры в данном мире.
     *
     * @param selectedWorldUuid uuid выбранного для игры мира.
     */
    public void getOrCreateHeroForSelectedWorldAndCloseThat(UUID selectedWorldUuid) {
        gameController.setCurrentWorld(selectedWorldUuid);

        if (gameController.findAllHeroesByWorldUid(selectedWorldUuid).isEmpty()) {
            getHeroCreatingPane().setVisible(true);
        } else {
            getHeroesListPane().setVisible(true);
        }

        getNetworkListPane().setVisible(false);
        getWorldsListPane().setVisible(false);
    }

    /**
     * После создания нового мира - приходим сюда для создания и нового героя для игры.
     *
     * @param newHeroTemplate модель нового героя для игры в новом мире.
     */
    public void createNewHeroForNewWorldAndCloseThatPanel(HeroCreatingPane newHeroTemplate) {
        HeroDTO savedNewHeroDto = gameController.saveNewHero(HeroDTO.builder()
                .heroName(newHeroTemplate.getHeroName())
                .ownerUid(gameController.getCurrentPlayerUid())
                .worldUid(newHeroTemplate.getWorldUid())
                .build());

        gameController.setCurrentWorld(newHeroTemplate.getWorldUid()); // again?..

        // начинаем игру сохраненным и указанным как текущим героем в указанном как текущем мире:
        playWithThisHero(savedNewHeroDto);
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(HeroDTO hero) {
        hero.setLastPlayDate(LocalDateTime.now());

        gameController.setCurrentHero(hero); // again?..
        gameController.setCurrentWorld(hero.getWorldUid()); // again?..
        gameController.setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());

        getHeroCreatingPane().setVisible(false);

        // здесь у героя уже должны быть заполнены все нужные поля (мир, владелец, он-лайн и т.п.).
        gameController.loadScreen(ScreenType.GAME_SCREEN);
    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameController.deleteWorld(worldUid);
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        gameController.deleteHero(heroUid);
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
                getHeroesListPane().setVisible(false);
                getHeroCreatingPane().setVisible(true);
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

    public void exitTheGame() {
        stop();
        gameController.exitTheGame(null);
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
        dropOldPanesFromLayer();
        closeBackImage();
        setVisible(false);
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
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
