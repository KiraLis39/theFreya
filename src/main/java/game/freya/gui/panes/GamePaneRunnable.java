package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.UserConfig.HotKeys;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameWindowController;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.gui.panes.handlers.UIHandler;
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
import java.awt.geom.Point2D;
import java.time.Duration;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, KeyListener, Runnable
public class GamePaneRunnable extends RunnableCanvasPanel {
    private final transient GameControllerService gameControllerService;
    private final transient GameWindowController frameController;
    private transient Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();

    private boolean isControlsMapped = false, isMovingKeyActive = false;

    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;

    private double parentHeightMemory = 0;

    private transient Thread resizeThread = null;


    public GamePaneRunnable(
            UIHandler uiHandler,
            JFrame parentFrame,
            GameControllerService gameControllerService,
            CharacterService characterService,
            GameWindowController frameController,
            ApplicationProperties props
    ) {
        super("GameCanvas", gameControllerService, characterService, parentFrame, uiHandler, props);

        this.gameControllerService = gameControllerService;
        this.frameController = frameController;
        setParentFrame(parentFrame);

        setSize(parentFrame.getSize());
        setBackground(Color.BLACK);
        getRootPane().setBackground(Color.RED);
        setIgnoreRepaint(true);
        setOpaque(false);
//        setFocusable(false);

        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);
        addKeyListener(this);
//        addComponentListener(this);

        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            if (gameControllerService.getWorldService().getCurrentWorld().isLocal() && !gameControllerService.getServer().isOpen()) {
                frameController.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в локальной сетевой игре, но наш Сервер не запущен!");
            }

            if (!gameControllerService.getLocalSocketConnection().isOpen()) {
                frameController.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в сетевой игре, но соединения с Сервером не существует!");
            }
        }

        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        setSecondThread("Game second thread", new Thread(() -> {
            // ждём пока основной поток игры запустится:
            long timeout = System.currentTimeMillis();
            while (!gameControllerService.isGameActive()) {
                Thread.yield();
                if (System.currentTimeMillis() - timeout > 7_000) {
                    throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
                }
            }

            while (gameControllerService.isGameActive() && !getSecondThread().isInterrupted()) {
                // check gameplay duration:
                checkGameplayDuration(gameControllerService.getCurrentHeroInGameTime());

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

    private void setInAc() {
        final String frameName = "mainFrame";
//        final String frameName = "game_canvas";

        // KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)
//        Constants.INPUT_ACTION.add(frameName, parentFrame.getRootPane());

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "backFunction",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isVisible() && Constants.isPaused()) {
                            onExitBack(GamePaneRunnable.this);
                        } else {
                            Constants.setPaused(!Constants.isPaused());
                        }
                    }
                });

        this.isControlsMapped = true;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        long delta;

        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable()) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 15_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        // инициализируем все для игры, отображаем окно игры:
        setGameActive();

        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            log.info("Начинается трансляция данных на Сервер...");
            gameControllerService.getLocalSocketConnection().startClientBroadcast();
        }

        // старт потока рисования игры:
        while (gameControllerService.isGameActive() && !Thread.currentThread().isInterrupted()) {
            delta = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();

            if (!getParentFrame().isActive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (!Constants.isPaused()) {
                dragViewIfNeeds();
            }

            try {
                drawNextFrame();

                // при успешной отрисовке:
                if (getDrawErrors() > 0) {
                    decreaseDrawErrorCount();
                }
            } catch (Exception e) {
                try {
                    throwExceptionAndYield(e);
                } catch (GlobalServiceException gse) {
                    if (gse.getCode().equals(ErrorMessages.DRAW_ERROR.getCode())) {
                        stop();
                    } else {
                        log.error("Непредвиденная ошибка при отрисовке игры: {}", ExceptionUtils.getFullExceptionMessage(gse));
                    }
                }
            }

            if (Constants.isFpsLimited()) {
                delayDrawing(delta);
            }
        }
        log.info("Thread of Game canvas is finalized.");
    }

    /**
     * Основной цикл отрисовки игрового окна.
     */
    private void drawNextFrame() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (isDisplayable()) {
            Graphics2D g2D = (Graphics2D) g;
            try {
                super.drawBackground(g2D);
            } catch (AWTException e) {
                log.error("Game paint exception here: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
            g2D.dispose();
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void recreateViewPort() {
        setViewPort(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    private void dragViewIfNeeds() {
        if (isMouseRightEdgeOver) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragLeft(2d);
                Thread.yield();
            }
        }
        if (isMouseLeftEdgeOver) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragRight(2d);
                Thread.yield();
            }
        }
        if (isMouseUpEdgeOver) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragDown(2d);
                Thread.yield();
            }
        }
        if (isMouseDownEdgeOver) {
            for (int i = 0; i < Constants.getGameConfig().getDragSpeed() / 2; i++) {
                dragUp(2d);
                Thread.yield();
            }
        }
    }

    private void setGameActive() {
        setVisible(true);
        createSubPanes();
        init();
        gameControllerService.setGameActive(true);

        requestFocusInWindow();

        super.createChat();

        Constants.setPaused(false);
        Constants.setGameStartedIn(System.currentTimeMillis());
    }

    private void zoomIn() {
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

        moveViewToPlayer(Constants.getGameConfig().getScrollSpeed(), (int) (Constants.getGameConfig().getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));
    }

    private void zoomOut() {
        log.debug("Zoom out...");

        // если окно больше установленного лимита или и так максимального размера:
        if (!canZoomOut(getViewPort().getWidth() - getViewPort().getX(), getViewPort().getHeight() - getViewPort().getY(),
                gameControllerService.getWorldEngine().getGameMap().getWidth(), gameControllerService.getWorldEngine().getGameMap().getHeight())) {
            return;
        }

        moveViewToPlayer(-Constants.getGameConfig().getScrollSpeed(), -(int) (Constants.getGameConfig().getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));

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

    @Override
    public synchronized void stop() {
        if (gameControllerService.isGameActive() || isVisible()) {

            boolean paused = Constants.isPaused();
            boolean debug = Constants.getGameConfig().isDebugInfoVisible();

            Constants.setPaused(false);
            Constants.getGameConfig().setDebugInfoVisible(false);
            gameControllerService.doScreenShot(getParentFrame().getLocation(), getBounds());
            Constants.setPaused(paused);
            Constants.getGameConfig().setDebugInfoVisible(debug);

            getSecondThread().interrupt();
            exitToMenu(getDuration());
        }
    }

    public void exitToMenu(Duration gameDuration) {
        // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
        if (gameControllerService.isGameActive()) {
            gameControllerService.setGameActive(false);

            justSave(gameDuration);

            // если игра сетевая и локальная - останавливаем сервер при выходе из игры:
            if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
                if (gameControllerService.getWorldService().getCurrentWorld().isLocal()) {
                    if (gameControllerService.closeServer()) {
                        log.info("Сервер успешно остановлен");
                    } else {
                        log.warn("Возникла ошибка при закрытии сервера.");
                    }
                }
                if (gameControllerService.getLocalSocketConnection().isOpen()) {
                    gameControllerService.getLocalSocketConnection().close();
                }
            }

            frameController.loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    @Override
    public void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса текущего мира:
        gameControllerService.initCurrentWorld(this);

        reloadShapes(this);
        recalculateMenuRectangles();

        // если не создан вьюпорт - создаём:
        if (getViewPort() == null) {
            recreateViewPort();
        }

        if (!isControlsMapped) {
            // назначаем горячие клавиши управления:
            setInAc();
        }

        moveViewToPlayer(0, 0);

        requestFocus();
    }

    private void justSave(Duration duration) {
        gameControllerService.saveTheGame(duration);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mousePressedOnPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getAudiosPane().setVisible(true);
                getVideosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);

            } else {
                Constants.setPaused(false);
                setOptionsMenuSetVisible(false);
            }
        }
        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getVideosPane().setVisible(true);
                getAudiosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            }
        }
        if (isThirdButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getHotkeysPane().setVisible(true);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                // нет нужды в паузе здесь, просто сохраняемся:
                justSave(getDuration());
                Constants.setPaused(false);
                new FOptionPane().buildFOptionPane("Успешно", "Игра сохранена!",
                        FOptionPane.TYPE.INFO, null, Constants.getDefaultCursor(), 3, false);
            }
        }
        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getGameplayPane().setVisible(true);
                getHotkeysPane().setVisible(false);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
            } else {
                Constants.showNFP();
            }
        }
        if (isExitButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                onExitBack(this);
            } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти в главное меню?",
                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
                stop();
            }
        }

        if (gameControllerService.isGameActive()) {
            if (getMinimapShowRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(false);
            }
            if (getMinimapHideRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(true);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            Point p = e.getPoint();
            log.debug("drag: {}x{}", p.x, p.y);
            if (p.getX() < mousePressedOnPoint.getX()) {
                isMouseLeftEdgeOver = true;
            } else if (p.getX() > mousePressedOnPoint.getX()) {
                isMouseRightEdgeOver = true;
            } else if (p.getY() < mousePressedOnPoint.getY()) {
                isMouseUpEdgeOver = true;
            } else if (p.getY() > mousePressedOnPoint.getY()) {
                isMouseDownEdgeOver = true;
            }
            this.mousePressedOnPoint = p;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();

        if (Constants.isPaused()) {
            // если пауза - проверяем меню:
            setFirstButtonOver(getFirstButtonRect().contains(p));
            setSecondButtonOver(getSecondButtonRect().contains(p));
            setThirdButtonOver(getThirdButtonRect().contains(p));
            setFourthButtonOver(getFourthButtonRect().contains(p));
            setExitButtonOver(getExitButtonRect().contains(p));
        } else { // иначе мониторим наведение на край окна для прокрутки поля:
            if (!isMovingKeyActive && Constants.getUserConfig().isDragGameFieldOnFrameEdgeReached()) {
                isMouseLeftEdgeOver = p.getX() <= 15
                        && (Constants.getUserConfig().isFullscreen() || p.getX() > 1) && !getMinimapHideRect().contains(p);
                isMouseRightEdgeOver = p.getX() >= getWidth() - 15 && (Constants.getUserConfig().isFullscreen() || p.getX() < getWidth() - 1);
                isMouseUpEdgeOver = p.getY() <= 10 && (Constants.getUserConfig().isFullscreen() || p.getY() > 1);
                isMouseDownEdgeOver = p.getY() >= getHeight() - 15
                        && (Constants.getUserConfig().isFullscreen() || p.getY() < getHeight() - 1) && !getMinimapHideRect().contains(p);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (Constants.isPaused()) {
            // not work into pause
            return;
        }
        switch (e.getWheelRotation()) {
            case 1 -> zoomOut();
            case -1 -> zoomIn();
            default -> log.warn("MouseWheelEvent unknown action: {}", e.getWheelRotation());
        }
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
        log.info("Возврат фокуса на холст...");
        requestFocus();
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

            log.debug("Resizing of game canvas...");

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(getParentFrame().getSize());
            } else if (!getSize().equals(getParentFrame().getRootPane().getSize())) {
                setSize(getParentFrame().getRootPane().getSize());
            }

            if (isVisible()) {
                reloadShapes(this);
                recalculateMenuRectangles();
            }

            recreateViewPort();
            moveViewToPlayer(0, 0);
            requestFocusInWindow();

            setRevolatileNeeds(true);
        });
        resizeThread.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // hero movement:
        if (e.getKeyCode() == HotKeys.MOVE_UP.getEvent()) {
            gameControllerService.setPlayerMovingUp(true);
        } else if (e.getKeyCode() == HotKeys.MOVE_BACK.getEvent()) {
            gameControllerService.setPlayerMovingDown(true);
        }
        if (e.getKeyCode() == HotKeys.MOVE_LEFT.getEvent()) {
            gameControllerService.setPlayerMovingLeft(true);
        } else if (e.getKeyCode() == HotKeys.MOVE_RIGHT.getEvent()) {
            gameControllerService.setPlayerMovingRight(true);
        }

        // camera movement:
        if (e.getKeyCode() == HotKeys.CAM_UP.getEvent()) {
            isMovingKeyActive = true;
            isMouseUpEdgeOver = true;
        } else if (e.getKeyCode() == HotKeys.CAM_DOWN.getEvent()) {
            isMovingKeyActive = true;
            isMouseDownEdgeOver = true;
        }
        if (e.getKeyCode() == HotKeys.CAM_LEFT.getEvent()) {
            isMovingKeyActive = true;
            isMouseLeftEdgeOver = true;
        } else if (e.getKeyCode() == HotKeys.CAM_RIGHT.getEvent()) {
            isMovingKeyActive = true;
            isMouseRightEdgeOver = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveUp()) {
            gameControllerService.setPlayerMovingUp(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveDown()) {
            gameControllerService.setPlayerMovingDown(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveLeft()) {
            gameControllerService.setPlayerMovingLeft(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveRight()) {
            gameControllerService.setPlayerMovingRight(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyLookUp()) {
            isMovingKeyActive = false;
            isMouseUpEdgeOver = false;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyLookDown()) {
            isMovingKeyActive = false;
            isMouseDownEdgeOver = false;
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyLookLeft()) {
            isMovingKeyActive = false;
            isMouseLeftEdgeOver = false;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyLookRight()) {
            isMovingKeyActive = false;
            isMouseRightEdgeOver = false;
        }
    }
}
