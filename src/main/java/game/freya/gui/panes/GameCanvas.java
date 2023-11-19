package game.freya.gui.panes;

import fox.FoxPointConverter;
import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig.HotKeys;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import static game.freya.config.Constants.FFB;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, KeyListener, Runnable
public class GameCanvas extends FoxCanvas {
    private final transient JFrame parentFrame;
    private final transient UIHandler uiHandler;
    private final transient GameController gameController;
    //    private final transient WorldDTO worldDTO;
    private final double infoStrut = 58d, infoStrutHardness = 40d;
    private transient Rectangle2D viewPort;
    private transient Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();
    private boolean isGameActive = false, isControlsMapped = false, isMovingKeyActive = false;
    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;
    private double parentHeightMemory = 0;
    private transient Thread resizeThread = null;
    private int strutMod;

    public GameCanvas(UIHandler uiHandler, JFrame parentFrame, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas", gameController);
        this.gameController = gameController;
        this.uiHandler = uiHandler;
        this.parentFrame = parentFrame;
//        this.worldDTO = worldDto;

        setSize(parentFrame.getLayeredPane().getSize());
        setBackground(Color.BLACK);

        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        addMouseListener(this);
        addKeyListener(this);

        new Thread(this).start();
    }

    private void setInAc() {
//        final String frameName = "mainFrame";
        final String frameName = "game_canvas";

        // KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)
        Constants.INPUT_ACTION.add(frameName, (JComponent) getParent());

//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_down_release",
//                Constants.getUserConfig().getKeyDown(), 0, true, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseDownEdgeOver = false;
//                    }
//                });

        this.isControlsMapped = true;
    }

    @Override
    public void run() {
        setGameActive();

        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable()) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 10_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        init();

        while (isGameActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            // если изменился размер фрейма:
            if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                log.info("Resizing by parent frame...");
                onResize();
                parentHeightMemory = parentFrame.getBounds().getHeight();
            }

            Graphics2D g2D;
            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
                }

                do {
                    do {
                        g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED,
                                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

                        // draw all World`s graphic:
                        drawWorld(g2D);
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED,
                                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

                        // not-pause events and changes:
                        if (Constants.isPaused()) {
                            if (isOptionsMenuSetVisible()) {
                                showOptions(g2D);
                                addExitVariantToOptionsMenuFix(g2D);
                            } else {
                                drawPauseMode(g2D);
                            }
                        } else {
                            dragViewIfNeeds();
                        }

                        // check gameplay duration:
                        checkGameplayDuration(gameController.getCurrentHeroInGameTime());

                        // draw debug info corner if debug mode on:
                        drawLocalDebugInfo(g2D);

                        g2D.dispose();
                    } while (getBufferStrategy().contentsRestored());
                } while (getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
                Thread.yield();
            }

            if (Constants.isFrameLimited() && Constants.getDiscreteDelay() > 1) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("Thread of Game canvas is finalized.");
    }

    private void addExitVariantToOptionsMenuFix(Graphics2D g2D) {
        g2D.setColor(Color.BLACK);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x - 1, getExitButtonRect().y + 17);
        g2D.setColor(isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(isOptionsMenuSetVisible()
                ? getBackButtonText() : getExitButtonText(), getExitButtonRect().x, getExitButtonRect().y + 18);
    }

    private void drawLocalDebugInfo(Graphics2D g2D) {
        if (Constants.isDebugInfoVisible()) {
            super.drawDebugInfo(g2D, gameController.getCurrentWorldTitle()); // отладочная информация

            int leftShift = 320;
            Point2D.Double playerPos = gameController.getCurrentHeroPosition();
            Shape playerShape = new Ellipse2D.Double(
                    (int) playerPos.x - Constants.MAP_CELL_DIM / 2d,
                    (int) playerPos.y - Constants.MAP_CELL_DIM / 2d,
                    Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);

            if (Constants.isLowFpsAlarm()) {
                g2D.setColor(Color.RED);
            }
            g2D.drawString("Delay fps: " + Constants.getDelay(), getWidth() - leftShift, getHeight() - 130);

            g2D.setColor(Color.GRAY);
            g2D.drawString("Hero pos: " + playerShape.getBounds2D().getCenterX() + "x" + playerShape.getBounds2D().getCenterY(),
                    getWidth() - leftShift, getHeight() - 110);

            g2D.drawString("Hero speed: " + gameController.getCurrentHeroSpeed(),
                    getWidth() - leftShift, getHeight() - 90);

            g2D.drawString("GameMap WxH: " + gameController.getCurrentWorldMap().getWidth() + "x" + gameController.getCurrentWorldMap().getHeight(),
                    getWidth() - leftShift, getHeight() - 70);

            g2D.drawString("Canvas XxY-WxH: " + getBounds().x + "x" + getBounds().y + "-"
                    + getBounds().width + "x" + getBounds().height, getWidth() - leftShift, getHeight() - 50);

            g2D.drawString("ViewPort XxY-WxH: " + viewPort.getBounds().x + "x" + viewPort.getBounds().y + "-"
                    + viewPort.getBounds().width + "x" + viewPort.getBounds().height, getWidth() - leftShift, getHeight() - 30);
        }
    }

    private void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса текущего мира:
        gameController.initCurrentWorld(this);

        reloadShapes(this);
        recalculateMenuRectangles();

        // если не создан вьюпорт - создаём:
        if (this.viewPort == null) {
            recreateViewPort();
        }

        // пересчитываем ректанглы пунктов меню:
//        if (getFirstButtonRect() == null) {
//            recalculateMenuRectangles();
//        }

        if (!isControlsMapped) {
            // назначаем горячие клавиши управления:
            setInAc();
        }

        moveViewToPlayer(0, 0);

        requestFocus();
    }

    private void recreateViewPort() {
        this.viewPort = new Rectangle(0, 0, getWidth(), getHeight());
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

    private void drawWorld(Graphics2D g2D) {
        // рисуем мир:
        gameController.getDrawCurrentWorld(g2D);

        // рисуем данные героев:
        drawHeroesData(g2D, gameController.getCurrentWorldHeroes());

        // рисуем UI:
        drawUI(g2D);
    }

    private void drawHeroesData(Graphics2D g2D, Set<HeroDTO> heroes) {
        strutMod = (int) (infoStrut - ((viewPort.getHeight() - viewPort.getY()) / infoStrutHardness));

        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.WHITE);

        // draw heroes data:
        heroes.forEach(hero -> {
            if (gameController.isHeroActive(hero, viewPort.getBounds())) {

                // Преобразуем координаты героя из карты мира в координаты текущего холста:
                Point2D relocatedPoint = FoxPointConverter.relocateOn(viewPort, getBounds(), hero.getPosition());

                // draw hero name:
                int halfName = (int) (FFB.getStringBounds(g2D, hero.getHeroName()).getWidth() / 2d);
                g2D.drawString(hero.getHeroName(),
                        (int) (relocatedPoint.getX() - halfName),
                        (int) (relocatedPoint.getY() - strutMod));

                strutMod += 24;

                // draw hero HP:
                g2D.setColor(Color.red);
                g2D.fillRoundRect((int) (relocatedPoint.getX() - 50),
                        (int) (relocatedPoint.getY() - strutMod),
                        hero.getHealth() - 10, 9, 3, 3);
                g2D.setColor(Color.black);
                g2D.drawRoundRect((int) (relocatedPoint.getX() - 50),
                        (int) (relocatedPoint.getY() - strutMod),
                        100, 9, 3, 3);
            }
        });
    }

    private void drawUI(Graphics2D g2D) {
        uiHandler.drawUI(this, g2D);
    }

    private void setGameActive() {
        this.isGameActive = true;
        Constants.setPaused(false);
        setOptionsMenuSetVisible(false);
        Constants.setGameStartedIn(System.currentTimeMillis());

        setVisible(true);
        requestFocusInWindow();
    }

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.GAME_FONT_03);
        g2D.setColor(new Color(0, 0, 0, 63));
        g2D.drawString(getPausedString(),
                (int) (getWidth() / 2D - FFB.getHalfWidthOfString(g2D, getPausedString())), getHeight() / 2 + 3);

        g2D.setFont(Constants.GAME_FONT_02);
        g2D.setColor(Color.DARK_GRAY);
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

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.DARK_GRAY);
            g2D.drawRoundRect(getFirstButtonRect().x, getFirstButtonRect().y, getFirstButtonRect().width, getFirstButtonRect().height, 3, 3);
            g2D.drawRoundRect(getSecondButtonRect().x, getSecondButtonRect().y, getSecondButtonRect().width, getSecondButtonRect().height, 3, 3);
            g2D.drawRoundRect(getThirdButtonRect().x, getThirdButtonRect().y, getThirdButtonRect().width, getThirdButtonRect().height, 3, 3);
            g2D.drawRoundRect(getExitButtonRect().x, getExitButtonRect().y, getExitButtonRect().width, getExitButtonRect().height, 3, 3);
        }
    }

    private void zoomIn() {
        log.debug("Zoom in...");

        // если окно меньше установленного лимита:
        if (viewPort.getWidth() - viewPort.getX() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
                || viewPort.getHeight() - viewPort.getY() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
        ) {
            log.debug("Can`t zoom in: vpWidth = {}, vpHeight = {} but minSize = {}",
                    viewPort.getWidth() - viewPort.getX(), viewPort.getHeight() - viewPort.getY(),
                    Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS);
            return;
        }

        moveViewToPlayer(Constants.getScrollSpeed(), (int) (Constants.getScrollSpeed() / (getBounds().getWidth() / getBounds().getHeight())));

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
//                viewPort.getX() + sdf,
//                viewPort.getY() + sdf2,
//                viewPort.getWidth() - sdf,
//                viewPort.getHeight() - sdf2);
////        log.info("f): {}, r1): {}, r2): {}", factor, resultX, resultY);
    }

    private void zoomOut() {
        log.debug("Zoom out...");

        // если окно больше установленного лимита или и так максимального размера:
        if (!canZoomOut(viewPort.getWidth() - viewPort.getX(), viewPort.getHeight() - viewPort.getY(),
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
        if (gameController.getCurrentWorldMap() != null && this.viewPort != null) {
            Point2D.Double p = gameController.getCurrentHeroPosition();
            Rectangle viewRect = this.viewPort.getBounds();
            this.viewPort.setRect(
                    p.x - (viewRect.getWidth() - viewRect.getX()) / 2D + x,
                    p.y - (viewRect.getHeight() - viewRect.getY()) / 2D + y,
                    p.x + (viewRect.getWidth() - viewRect.getX()) / 2D - x,
                    p.y + (viewRect.getHeight() - viewRect.getY()) / 2D - y);

            checkOutOfFieldCorrection();
        }
    }

    private void checkOutOfFieldCorrection() {
        while (viewPort.getX() < 0) {
            dragLeft(1d);
        }

        while (viewPort.getWidth() > gameController.getCurrentWorldMap().getWidth()) {
            dragRight(1d);
        }

        while (viewPort.getY() < 0) {
            dragUp(1d);
        }

        while (viewPort.getHeight() > gameController.getCurrentWorldMap().getHeight()) {
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
            double newWidth = Math.min(viewPort.getWidth() + per, mapWidth);
            viewPort.setRect(viewPort.getX() + per - (newWidth == mapWidth ? Math.abs(viewPort.getWidth() + per - mapWidth) : 0),
                    viewPort.getY(), newWidth, viewPort.getHeight());
        }
    }

    public void dragRight(Double pixels) {
        if (canDragRight()) {
            log.debug("Drag right...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double newX = viewPort.getX() - per > 0 ? viewPort.getX() - per : 0;
            viewPort.setRect(newX, viewPort.getY(),
                    viewPort.getWidth() - per + (newX == 0 ? Math.abs(viewPort.getX() - per) : 0), viewPort.getHeight());
        }
    }

    public void dragUp(Double pixels) {
        if (canDragUp()) {
            log.debug("Drag up...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double mapHeight = gameController.getCurrentWorldMap().getHeight();
            double newHeight = Math.min(viewPort.getHeight() + per, mapHeight);
            viewPort.setRect(viewPort.getX(), viewPort.getY() + per - (newHeight == mapHeight
                            ? Math.abs(viewPort.getHeight() + per - mapHeight) : 0),
                    viewPort.getWidth(), newHeight);
        }
    }

    public void dragDown(Double pixels) {
        if (canDragDown()) {
            log.debug("Drag down...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double newY = viewPort.getY() - per > 0 ? viewPort.getY() - per : 0;
            viewPort.setRect(viewPort.getX(), newY, viewPort.getWidth(),
                    viewPort.getHeight() - per + (newY == 0 ? Math.abs(viewPort.getY() - per) : 0));
        }
    }

    private boolean canDragDown() {
        return viewPort.getY() > 0;
    }

    private boolean canDragUp() {
        return viewPort.getHeight() < gameController.getCurrentWorldMap().getHeight();
    }

    private boolean canDragLeft() {
        return viewPort.getWidth() < gameController.getCurrentWorldMap().getWidth();
    }

    private boolean canDragRight() {
        return viewPort.getX() > 0;
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

            // сохраняем всё и всех:
            gameController.setHeroOfflineAndSave(getDuration());
            gameController.saveCurrentWorld();

            // закрываем и выходим в меню:
            setVisible(false);
            gameController.loadScreen(ScreenType.MENU_SCREEN, null);
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
                Constants.showNFP();
            } else {
                Constants.setPaused(false);
                setOptionsMenuSetVisible(false);
            }
        }
        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                Constants.showNFP();
            } else {
                setOptionsMenuSetVisible(true);
            }
        }
        if (isThirdButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }
        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                Constants.showNFP();
            } else {
                Constants.showNFP();
            }
        }
        if (isExitButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                setOptionsMenuSetVisible(false);
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
            recreateViewPort();
            moveViewToPlayer(0, 0);
            requestFocusInWindow();
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

    public Rectangle2D getViewPort() {
        return this.viewPort;
    }
}
