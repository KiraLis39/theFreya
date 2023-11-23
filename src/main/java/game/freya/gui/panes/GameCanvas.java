package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig.HotKeys;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, KeyListener, Runnable
public class GameCanvas extends FoxCanvas {
    private final transient JFrame parentFrame;
    private final transient GameController gameController;
    private transient Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();
    private boolean isGameActive = false, isControlsMapped = false, isMovingKeyActive = false;
    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;
    private double parentHeightMemory = 0;
    private transient Thread resizeThread = null;


    public GameCanvas(UIHandler uiHandler, JFrame parentFrame, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas", gameController, parentFrame.getLayeredPane(), uiHandler);
        this.gameController = gameController;
        this.parentFrame = parentFrame;

        setSize(parentFrame.getLayeredPane().getSize());
        setBackground(Color.BLACK);
        setIgnoreRepaint(true);
//        setFocusable(false);

        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        addMouseListener(this);
        addKeyListener(this);

        new Thread(this).start();

        // запуск вспомогательного потока процессов игры:
        setSecondThread("Game second thread", new Thread(() -> {
            // ждём пока основной поток игры запустится:
            long timeout = System.currentTimeMillis();
            while (!isGameActive) {
                Thread.yield();
                if (System.currentTimeMillis() - timeout > 7_000) {
                    throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
                }
            }

            while (isGameActive && !getSecondThread().isInterrupted()) {
                // check gameplay duration:
                checkGameplayDuration(gameController.getCurrentHeroInGameTime());

                // если изменился размер фрейма:
                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                    log.debug("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = parentFrame.getBounds().getHeight();
                }

                if (!Constants.isPaused()) {
                    dragViewIfNeeds();
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
                            onExitBack(GameCanvas.this);
                        } else {
                            Constants.setPaused(!Constants.isPaused());
                        }
                    }
                });

        this.isControlsMapped = true;
    }

    @Override
    public void run() {
        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable()) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 6_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        // инициализируем все для игры, отображаем окно игры:
        setGameActive();

        // старт потока рисования игры:
        while (isGameActive) {
            if (!parentFrame.isActive()) {
                Thread.yield();
                continue;
            }

            try {
                drawNextFrame();
            } catch (Exception e) {
                throwExceptionAndYield(e);
            }

            // если текущий FPS превышает лимит:
            if (Constants.isFpsLimited()) {
                doDrawDelay();
            }

            // при успешной отрисовке:
            if (getDrawErrorCount() > 0) {
                decreaseDrawErrorCount();
            }
        }
        log.info("Thread of Game canvas is finalized.");
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

    /**
     * Основной цикл отрисовки игрового окна.
     */
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

    private void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса текущего мира:
        gameController.initCurrentWorld(this);

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

    private void recreateViewPort() {
        setViewPort(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    private void dragViewIfNeeds() {
        if (isMouseRightEdgeOver) {
            dragLeft(null);
        }
        if (isMouseLeftEdgeOver) {
            dragRight(null);
        }
        if (isMouseUpEdgeOver) {
            dragDown(null);
        }
        if (isMouseDownEdgeOver) {
            dragUp(null);
        }
    }

    private void setGameActive() {
        init();

        this.isGameActive = true;
        setVisible(true);
        requestFocusInWindow();

        createBufferStrategy(Constants.getUserConfig().getBufferedDeep());

        // если более двух буфферов не позволительно:
        if (!getBufferStrategy().getCapabilities().isMultiBufferAvailable()) {
            Constants.getUserConfig().setMaxBufferedDeep(2);
        }

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

        moveViewToPlayer(Constants.getScrollSpeed(), (int) (Constants.getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));
    }

    private void zoomOut() {
        log.debug("Zoom out...");

        // если окно больше установленного лимита или и так максимального размера:
        if (!canZoomOut(getViewPort().getWidth() - getViewPort().getX(), getViewPort().getHeight() - getViewPort().getY(),
                gameController.getCurrentWorldMap().getWidth(), gameController.getCurrentWorldMap().getHeight())) {
            return;
        }

        moveViewToPlayer(-Constants.getScrollSpeed(), -(int) (Constants.getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));

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
        if (gameController.getCurrentWorldMap() != null && getViewPort() != null) {
            Point2D.Double p = gameController.getCurrentHeroPosition();
            Rectangle viewRect = getViewPort().getBounds();
            getViewPort().setRect(
                    p.x - (viewRect.getWidth() - viewRect.getX()) / 2D + x,
                    p.y - (viewRect.getHeight() - viewRect.getY()) / 2D + y,
                    p.x + (viewRect.getWidth() - viewRect.getX()) / 2D - x,
                    p.y + (viewRect.getHeight() - viewRect.getY()) / 2D - y);

            checkOutOfFieldCorrection();
        }
    }

    private void checkOutOfFieldCorrection() {
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

    public void dragLeft(Double pixels) {
        if (canDragLeft()) {
            log.debug("Drag left...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double mapWidth = gameController.getCurrentWorldMap().getWidth();
            double newWidth = Math.min(getViewPort().getWidth() + per, mapWidth);
            getViewPort().setRect(getViewPort().getX() + per - (newWidth == mapWidth ? Math.abs(getViewPort().getWidth() + per - mapWidth) : 0),
                    getViewPort().getY(), newWidth, getViewPort().getHeight());
        }
    }

    public void dragRight(Double pixels) {
        if (canDragRight()) {
            log.debug("Drag right...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double newX = getViewPort().getX() - per > 0 ? getViewPort().getX() - per : 0;
            getViewPort().setRect(newX, getViewPort().getY(),
                    getViewPort().getWidth() - per + (newX == 0 ? Math.abs(getViewPort().getX() - per) : 0), getViewPort().getHeight());
        }
    }

    public void dragUp(Double pixels) {
        if (canDragUp()) {
            log.debug("Drag up...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double mapHeight = gameController.getCurrentWorldMap().getHeight();
            double newHeight = Math.min(getViewPort().getHeight() + per, mapHeight);
            getViewPort().setRect(getViewPort().getX(), getViewPort().getY() + per - (newHeight == mapHeight
                            ? Math.abs(getViewPort().getHeight() + per - mapHeight) : 0),
                    getViewPort().getWidth(), newHeight);
        }
    }

    public void dragDown(Double pixels) {
        if (canDragDown()) {
            log.debug("Drag down...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double newY = getViewPort().getY() - per > 0 ? getViewPort().getY() - per : 0;
            getViewPort().setRect(getViewPort().getX(), newY, getViewPort().getWidth(),
                    getViewPort().getHeight() - per + (newY == 0 ? Math.abs(getViewPort().getY() - per) : 0));
        }
    }

    private boolean canDragDown() {
        return getViewPort().getY() > 0;
    }

    private boolean canDragUp() {
        return getViewPort().getHeight() < gameController.getCurrentWorldMap().getHeight();
    }

    private boolean canDragLeft() {
        return getViewPort().getWidth() < gameController.getCurrentWorldMap().getWidth();
    }

    private boolean canDragRight() {
        return getViewPort().getX() > 0;
    }

    @Override
    public synchronized void stop() {
        if (this.isGameActive || isVisible()) {
            boolean paused = Constants.isPaused();
            boolean debug = Constants.isDebugInfoVisible();

            Constants.setPaused(false);
            Constants.setDebugInfoVisible(false);
            gameController.doScreenShot(parentFrame.getLocation(), getBounds());
            Constants.setPaused(paused);
            Constants.setDebugInfoVisible(debug);

            // останавливаем отрисовку мира:
            this.isGameActive = false;

            // если игра сетевая - останавливаем сервер:
            if (gameController.isCurrentWorldIsNetwork()) {
                if (gameController.closeNet()) {
                    log.info("Сервер успешно остановлен");
                } else {
                    log.warn("Возникла ошибка при закрытии сервера.");
                }
            }

            // сохраняем всё и всех:
            gameController.setHeroOfflineAndSave(getDuration());
            gameController.saveCurrentWorld();

            // закрываем и выходим в меню:
            setVisible(false);
            gameController.loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    private void justSave() {
        gameController.justSaveOnlineHero(getDuration());
        gameController.saveCurrentWorld();
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
                isMouseLeftEdgeOver = p.getX() <= 20 && (Constants.getUserConfig().isFullscreen() || p.getX() > 1);
                isMouseRightEdgeOver = p.getX() >= getWidth() - 20 && (Constants.getUserConfig().isFullscreen() || p.getX() < getWidth() - 1);
                isMouseUpEdgeOver = p.getY() <= 10 && (Constants.getUserConfig().isFullscreen() || p.getY() > 1);
                isMouseDownEdgeOver = p.getY() >= getHeight() - 20 && (Constants.getUserConfig().isFullscreen() || p.getY() < getHeight() - 1);
            }
        }
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
                Constants.setPaused(false);
                justSave();
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
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

            log.debug("Resizing of game canvas...");

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(parentFrame.getSize());
            } else if (!getSize().equals(parentFrame.getRootPane().getSize())) {
                setSize(parentFrame.getRootPane().getSize());
            }

            reloadShapes(this);
            recalculateMenuRectangles();
            recreateSubPanes();
            recreateViewPort();
            moveViewToPlayer(0, 0);
            requestFocusInWindow();

            setRevolatileNeeds(true);
        });
        resizeThread.start();
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

    @Override
    public void keyPressed(KeyEvent e) {
        // hero movement:
        if (e.getKeyCode() == HotKeys.MOVE_UP.getEvent()) {
            gameController.setPlayerMovingUp(true);
        } else if (e.getKeyCode() == HotKeys.MOVE_BACK.getEvent()) {
            gameController.setPlayerMovingDown(true);
        }
        if (e.getKeyCode() == HotKeys.MOVE_LEFT.getEvent()) {
            gameController.setPlayerMovingLeft(true);
        } else if (e.getKeyCode() == HotKeys.MOVE_RIGHT.getEvent()) {
            gameController.setPlayerMovingRight(true);
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
            gameController.setPlayerMovingUp(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveDown()) {
            gameController.setPlayerMovingDown(false);
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveLeft()) {
            gameController.setPlayerMovingLeft(false);
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveRight()) {
            gameController.setPlayerMovingRight(false);
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

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
