package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JComponent;
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
import java.util.Optional;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, KeyListener, Runnable
public class GameCanvas extends FoxCanvas {
    private static final String backToGameButtonText = "Вернуться";
    private static final String optionsButtonText = "Настройки";
    private static final String saveButtonText = "Сохранить";
    private static final String exitButtonText = "Выйти в меню";
    private final transient GameController gameController;
    private final transient UIHandler uiHandler;
    private final transient WorldDTO worldDTO;
    private transient Rectangle2D viewPort;
    private Rectangle backToGameButtonRect, optionsButtonRect, saveButtonRect, exitButtonRect;
    private Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();
    private boolean isGameActive = false;
    private boolean isControlsMapped = false;
    private boolean isMovingKeyActive = false;
    private boolean backToGameButtonOver = false, optionsButtonOver = false, saveButtonOver = false, exitButtonOver = false;
    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;
    @Getter
    private boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;

    public GameCanvas(WorldDTO worldDTO, UIHandler uiHandler, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas");
        this.gameController = gameController;
        this.uiHandler = uiHandler;

        this.worldDTO = worldDTO;

        setBackground(Color.BLACK);

        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        addMouseListener(this);
        addKeyListener(this);

        if (this.worldDTO.getPlayers().isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "this.worldDTO.getPlayers()");
        }

        new Thread(this).start();
    }

    private void setInAc() {
        // KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)
        Constants.INPUT_ACTION.add("game_canvas", (JComponent) getParent());

//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_left",
//                Constants.getUserConfig().getKeyLeft(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseLeftEdgeOver = true;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_right",
//                Constants.getUserConfig().getKeyRight(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseRightEdgeOver = true;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_up",
//                Constants.getUserConfig().getKeyUp(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseUpEdgeOver = true;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_down",
//                Constants.getUserConfig().getKeyDown(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseDownEdgeOver = true;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_left_release",
//                Constants.getUserConfig().getKeyLeft(), 0, true, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseLeftEdgeOver = false;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_right_release",
//                Constants.getUserConfig().getKeyRight(), 0, true, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseRightEdgeOver = false;
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, "game_canvas", "move_up_release",
//                Constants.getUserConfig().getKeyUp(), 0, true, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        isMouseUpEdgeOver = false;
//                    }
//                });
//
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
            try {
                if (getBufferStrategy() == null) {
                    log.info("Game canvas create the bs...");
                    createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED);

                        // draw all World`s graphic:
                        drawWorld(g2D);
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED);

                        // not-pause events and changes:
                        if (Constants.isPaused()) {
                            // is needs to draw the Menu:
                            drawPauseMode(g2D);
                        } else {
                            dragViewIfNeeds();
                        }

                        // draw debug info corner if debug mode on:
                        drawLocalDebugInfo(g2D);

                        if (Constants.isFpsInfoVisible()) {
                            super.drawFps(g2D);
                        }

                        g2D.dispose();
                    } while (getBufferStrategy().contentsRestored());
                } while (getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

//            log.debug("Game canvas drawing cycle end. Waiting...");
            if (Constants.isFrameLimited() && Constants.getDiscreteDelay() > 1) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
//            log.debug("Game canvas waiting done.");
        }
        log.info("Thread of Game canvas is finalized.");
    }

    private void drawLocalDebugInfo(Graphics2D g2D) {
        if (Constants.isDebugInfoVisible()) {
            super.drawDebugInfo(g2D, worldDTO.getTitle(), worldDTO.getInGameTime()); // отладочная информация

            int leftShift = 320;
            Point2D.Double playerPos = gameController.getCurrentPlayer().getPosition();
            Shape playerShape = new Ellipse2D.Double(
                    (int) playerPos.x - Constants.MAP_CELL_DIM / 2d,
                    (int) playerPos.y - Constants.MAP_CELL_DIM / 2d,
                    Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);
            g2D.drawString("Player pos: " + playerShape.getBounds2D().getCenterX() + "x" + playerShape.getBounds2D().getCenterY(),
                    getWidth() - leftShift, getHeight() - 110);

            g2D.drawString("Player speed: " + gameController.getCurrentPlayer().getSpeed(),
                    getWidth() - leftShift, getHeight() - 90);

            g2D.drawString("GameMap WxH: " + worldDTO.getGameMap().getWidth() + "x" + worldDTO.getGameMap().getHeight(),
                    getWidth() - leftShift, getHeight() - 70);

            g2D.drawString("Canvas XxY-WxH: " + getBounds().x + "x" + getBounds().y + "-"
                    + getBounds().width + "x" + getBounds().height, getWidth() - leftShift, getHeight() - 50);

            g2D.drawString("ViewPort XxY-WxH: " + viewPort.getBounds().x + "x" + viewPort.getBounds().y + "-"
                    + viewPort.getBounds().width + "x" + viewPort.getBounds().height, getWidth() - leftShift, getHeight() - 30);
        }
    }

    private void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса мира:
        this.worldDTO.init(this);

        // если не создан вьюпорт - создаём:
        if (this.viewPort == null) {
            recreateViewPort();
        }

        // пересчитываем ректанглы пунктов меню:
        if (backToGameButtonRect == null) {
            recalculateMenuRectangles();
        }

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

    private void recalculateMenuRectangles() {
        backToGameButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.17D),
                (int) (getWidth() * 0.1D), 30);
        optionsButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.22D),
                (int) (getWidth() * 0.1D), 30);
        saveButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.27D),
                (int) (getWidth() * 0.1D), 30);
        exitButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.85D),
                (int) (getWidth() * 0.1D), 30);
    }

    private void drawWorld(Graphics2D g2D) {
        // рисуем мир:
        worldDTO.draw(g2D, viewPort.getBounds());

        // рисуем UI:
        drawUI(g2D);
    }

    private void drawUI(Graphics2D g2D) {
        uiHandler.drawUI(this, g2D);
    }

    private void setGameActive() {
        Optional<PlayerDTO> currentPlayer = this.worldDTO.getPlayers().values().stream()
                .filter(p -> p.getNickName().equals(Constants.getUserConfig().getUserName())).findFirst();
        if (currentPlayer.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, currentPlayer);
        }
        currentPlayer.get().setOnline(true);
        this.isGameActive = true;
        Constants.setPaused(false);
        Constants.setGameStartedIn(System.currentTimeMillis());
    }

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.GAME_FONT_02);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString("- PAUSED -",
                (int) (getWidth() / 2D - Constants.FFB.getStringBounds(g2D, "- PAUSED -").getWidth() / 2),
                getHeight() / 2);

        // fill left gray menu polygon:
        g2D.setColor(Constants.getMainMenuBackgroundColor());
        g2D.fillPolygon(getLeftGrayMenuPoly());

        drawEscMenu(g2D);
    }

    private void drawEscMenu(Graphics2D g2D) {
        // buttons text:
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(backToGameButtonText, backToGameButtonRect.x - 1, backToGameButtonRect.y + 17);
        g2D.setColor(backToGameButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(backToGameButtonText, backToGameButtonRect.x, backToGameButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(optionsButtonText, optionsButtonRect.x - 1, optionsButtonRect.y + 17);
        g2D.setColor(optionsButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(optionsButtonText, optionsButtonRect.x, optionsButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(saveButtonText, saveButtonRect.x - 1, saveButtonRect.y + 17);
        g2D.setColor(saveButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(saveButtonText, saveButtonRect.x, saveButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(exitButtonText, exitButtonRect.x - 1, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(exitButtonText, exitButtonRect.x, exitButtonRect.y + 18);

//        if (Constants.isDebugInfoVisible()) {
//            g2D.setColor(Color.DARK_GRAY);
//            g2D.drawRoundRect(backToGameButtonRect.x, backToGameButtonRect.y, backToGameButtonRect.width, backToGameButtonRect.height, 3, 3);
//            g2D.drawRoundRect(optionsButtonRect.x, optionsButtonRect.y, optionsButtonRect.width, optionsButtonRect.height, 3, 3);
//            g2D.drawRoundRect(saveButtonRect.x, saveButtonRect.y, saveButtonRect.width, saveButtonRect.height, 3, 3);
//            g2D.drawRoundRect(exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height, 3, 3);
//        }
    }

    private void zoomIn() {
        // todo: при приближении - камера должна оставаться на игроке. Доработать!
        log.debug("Zoom in...");

        // если окно меньше установленного лимита:
        if (viewPort.getWidth() - viewPort.getX() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
                || viewPort.getHeight() - viewPort.getY() <= Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
        ) {
            log.debug("Can`t zoom in: vpWidth = {}, vpHeight = {} but minSize = {}",
                    viewPort.getWidth() - viewPort.getX(), viewPort.getHeight() - viewPort.getY(), Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS);
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
                this.worldDTO.getGameMap().getWidth(), this.worldDTO.getGameMap().getHeight())) {
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
        if (this.worldDTO.getGameMap() != null && this.viewPort != null) {
            Point2D.Double p = gameController.getCurrentPlayer().getPosition();
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

        while (viewPort.getWidth() > this.worldDTO.getGameMap().getWidth()) {
            dragRight(1d);
        }

        while (viewPort.getY() < 0) {
            dragUp(1d);
        }

        while (viewPort.getHeight() > this.worldDTO.getGameMap().getHeight()) {
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
            double mapWidth = this.worldDTO.getGameMap().getWidth();
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
            double mapHeight = this.worldDTO.getGameMap().getHeight();
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
        return viewPort.getHeight() < this.worldDTO.getGameMap().getHeight();
    }

    private boolean canDragLeft() {
        return viewPort.getWidth() < this.worldDTO.getGameMap().getWidth();
    }

    private boolean canDragRight() {
        return viewPort.getX() > 0;
    }

    @Override
    public void stop() {
        this.isGameActive = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();

        if (Constants.isPaused()) {
            // если пауза - проверяем меню:
            backToGameButtonOver = backToGameButtonRect.contains(p);
            optionsButtonOver = optionsButtonRect.contains(p);
            saveButtonOver = saveButtonRect.contains(p);
            exitButtonOver = exitButtonRect.contains(p);
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
        if (backToGameButtonOver) {
            Constants.setPaused(false);
        }
        if (optionsButtonOver) {
            // todo: доработать это говно
//            FoxTip tip = new FoxTip(Constants.RENDER, (JComponent) getParent(), FoxTip.TYPE.INFO, null, null, null);
//            tip.createFoxTip(FoxTip.TYPE.INFO, null, "1", "2", "3", (JComponent) getParent());
//            tip.showTip();
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.", FOptionPane.TYPE.INFO);
        }
        if (saveButtonOver) {
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.", FOptionPane.TYPE.INFO);
        }
        if (exitButtonOver) {
            stop();
            gameController.updateWorld(worldDTO, getDuration());
            gameController.updateCurrentPlayer();
            gameController.loadScreen(ScreenType.MENU_SCREEN);
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
        reloadShapes(this);
        recalculateMenuRectangles();
        recreateViewPort();
        moveViewToPlayer(0, 0);
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
        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveUp()) {
            isPlayerMovingUp = true;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveDown()) {
            isPlayerMovingDown = true;
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveLeft()) {
            isPlayerMovingLeft = true;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveRight()) {
            isPlayerMovingRight = true;
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyLookUp()) {
            isMovingKeyActive = true;
            isMouseUpEdgeOver = true;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyLookDown()) {
            isMovingKeyActive = true;
            isMouseDownEdgeOver = true;
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyLookLeft()) {
            isMovingKeyActive = true;
            isMouseLeftEdgeOver = true;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyLookRight()) {
            isMovingKeyActive = true;
            isMouseRightEdgeOver = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveUp()) {
            isPlayerMovingUp = false;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveDown()) {
            isPlayerMovingDown = false;
        }

        if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveLeft()) {
            isPlayerMovingLeft = false;
        } else if (e.getKeyCode() == Constants.getUserConfig().getKeyMoveRight()) {
            isPlayerMovingRight = false;
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

    public PlayerDTO getCurrentPlayer() {
        return gameController.getCurrentPlayer();
    }

    public WorldDTO getCurrentWorld() {
        return this.worldDTO;
    }
}
