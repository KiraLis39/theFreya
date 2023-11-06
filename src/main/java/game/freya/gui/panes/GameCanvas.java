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
import lombok.extern.slf4j.Slf4j;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, Runnable
public class GameCanvas extends FoxCanvas {
    private static final String backToGameButtonText = "Вернуться";
    private static final String optionsButtonText = "Настройки";
    private static final String saveButtonText = "Сохранить";
    private static final String exitButtonText = "Выйти в меню";
    private final transient GameController gameController;
    private final transient Rectangle2D viewPort;
    private final transient WorldDTO worldDTO;
    private Rectangle backToGameButtonRect, optionsButtonRect, saveButtonRect, exitButtonRect;
    private Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();
    private boolean isGameActive = false;
    private boolean backToGameButtonOver = false, optionsButtonOver = false, saveButtonOver = false, exitButtonOver = false;
    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;
    private double scrollSpeedX = 20, scrollSpeedY;
    private double delta;

    public GameCanvas(WorldDTO worldDTO, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas");
        this.gameController = gameController;

        this.worldDTO = worldDTO;

        setBackground(Color.BLACK);
        setFocusable(false);

        setScrollSpeed(20D);

        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        addMouseListener(this);

        if (this.worldDTO.getPlayers().isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "this.worldDTO.getPlayers()");
        }

        // проводим основную инициализацию класса мира:
        this.worldDTO.init(this);

        // если не создан вьюпорт - создаём:
        this.viewPort = new Rectangle2D.Double(
                this.worldDTO.getGameMap().getWidth() / 2D - getWidth() / 2D,
                this.worldDTO.getGameMap().getHeight() / 2D - getHeight() / 2D,
                this.worldDTO.getGameMap().getWidth() / 2D + getWidth() / 2D,
                this.worldDTO.getGameMap().getHeight() / 2D + getHeight() / 2D);

        new Thread(this).start();
    }

    @Override
    public void run() {
        setGameActive();

        while (isGameActive) {
            // ждём пока компонент не станет виден:
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            // пересчитываем ректанглы меню, когда компонент наконец стал виден:
            if (backToGameButtonRect == null) {
                recalculateMenuRectangles();
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);

                        // draw all World`s graphic:
                        drawWorld(g2D);

                        // not-pause events and changes:
                        if (Constants.isPaused()) {
                            // is needs to draw the Menu:
                            drawPauseMode(g2D);
                        } else {
                            dragViewIfNeeds();
                        }

                        // draw debug info corner if debug mode on:
                        if (Constants.isDebugInfoVisible()) {
                            super.drawDebugInfo(g2D, worldDTO.getTitle()); // отладочная информация
                        }

                        // g2D.dispose();
                    } while (getBufferStrategy().contentsRestored());
                } while (getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            if (Constants.isFrameLimited()) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("Thread of Game canvas is finalized.");
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
        UIHandler.drawUI(getBounds(), g2D);
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
    }

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString("- PAUSED -",
                (int) (getWidth() / 2D - Constants.FFB.getStringBounds(g2D, "- PAUSED -").getWidth() / 2),
                getHeight() / 2);

        // fill left gray menu polygon:
        g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
        if (getLeftGrayMenuPoly() == null) {
            reloadShapes(this);
        }
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
        log.debug("Zoom in...");

//        double vpXSrc = viewPort.getX();
//        double vpXDst = viewPort.getWidth();
        double vpWidth = viewPort.getWidth() - viewPort.getX();
//        double vpYSrc = viewPort.getY();
//        double vpYDst = viewPort.getHeight();
        double vpHeight = viewPort.getHeight() - viewPort.getY();
        int minCellsSize = Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS;

        // если окно меньше установленного лимита:
        if (vpWidth <= minCellsSize || vpHeight <= minCellsSize) {
            log.warn("Can`t zoom in: vpWidth = {} and vpHeight = {} but minCellsSize is {}", vpWidth, vpHeight, minCellsSize);
            return;
        }

        viewPort.setRect(viewPort.getX() + scrollSpeedX, viewPort.getY() + scrollSpeedY,
                viewPort.getWidth() - scrollSpeedX, viewPort.getHeight() - scrollSpeedY);

        log.warn("Can`t zoom in");
    }

    private void zoomOut() {
        log.debug("Zoom out...");

        double mapWidth = this.worldDTO.getGameMap().getWidth();
        double mapHeight = this.worldDTO.getGameMap().getHeight();

        double viewXSrc = viewPort.getX();
        double viewXDst = viewPort.getWidth();

        double viewYSrc = viewPort.getY();
        double viewYDst = viewPort.getHeight();

        // если окно больше установленного лимита или и так максимального размера:
        if (!canZoomOut(viewXDst - viewXSrc, viewYDst - viewYSrc, mapWidth, mapHeight)) {
            return;
        }

        viewPort.setRect(viewXSrc - scrollSpeedX, viewYSrc - scrollSpeedY, viewXDst + scrollSpeedX, viewYDst + scrollSpeedY);

        // проверка на выход за края игрового поля:
        while (viewPort.getX() < 0) {
            dragLeft(1d);
        }

        while (viewPort.getWidth() > mapWidth) {
            dragRight(1d);
        }

        while (viewPort.getY() < 0) {
            dragUp(1d);
        }

        while (viewPort.getHeight() > mapHeight) {
            dragDown(1d);
        }
    }

    private boolean canZoomOut(double viewWidth, double viewHeight, double mapWidth, double mapHeight) {
        int maxCellsSize = Constants.MAP_CELL_DIM * Constants.MAX_ZOOM_OUT_CELLS;

        // если окно больше установленного лимита:
        if (viewWidth >= maxCellsSize || viewHeight >= maxCellsSize) {
            log.warn("Can`t zoom out: viewWidth = {} and viewHeight = {} but maxCellsSize is {}", viewWidth, viewHeight, maxCellsSize);
            return false;
        }

        // если окно уже максимального размера:
        if (viewWidth >= mapWidth || viewHeight >= mapHeight) {
            log.warn("Can`t zoom out: maximum size reached.");
            return false;
        }

        return true;
    }

    private void dragLeft(Double pixels) {
        if (canDragLeft()) {
            log.debug("Drag left...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double mapWidth = this.worldDTO.getGameMap().getWidth();
            double newWidth = Math.min(viewPort.getWidth() + per, mapWidth);
            viewPort.setRect(viewPort.getX() + per - (newWidth == mapWidth ? Math.abs(viewPort.getWidth() + per - mapWidth) : 0),
                    viewPort.getY(), newWidth, viewPort.getHeight());
        }
    }

    private void dragRight(Double pixels) {
        if (canDragRight()) {
            log.debug("Drag right...");
            double per = pixels != null ? pixels : Constants.getDragSpeed();
            double newX = viewPort.getX() - per > 0 ? viewPort.getX() - per : 0;
            viewPort.setRect(newX, viewPort.getY(),
                    viewPort.getWidth() - per + (newX == 0 ? Math.abs(viewPort.getX() - per) : 0), viewPort.getHeight());
        }
    }

    private void dragUp(Double pixels) {
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

    private void dragDown(Double pixels) {
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

    public void setScrollSpeed(double scrollSpeed) {
        this.delta = getBounds().getWidth() / getBounds().getHeight();
        this.scrollSpeedX = scrollSpeed;
        this.scrollSpeedY = scrollSpeedX / this.delta;
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
            if (Constants.getUserConfig().isDragGameFieldOnFrameEdgeReached()) {
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
            // saveTheGame() ?..
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
        Component comp = e.getComponent();
        if (this.worldDTO.getGameMap() != null) {
            // перекраиваем вьюпорт под новое окно:
            this.viewPort.setRect(
                    this.worldDTO.getGameMap().getWidth() / 2D - comp.getBounds().getWidth() / 2D,
                    this.worldDTO.getGameMap().getHeight() / 2D - comp.getBounds().getHeight() / 2D,
                    this.worldDTO.getGameMap().getWidth() / 2D + comp.getBounds().getWidth() / 2D,
                    this.worldDTO.getGameMap().getHeight() / 2D + comp.getBounds().getHeight() / 2D);
        }

        reloadShapes(this);

        this.delta = comp.getBounds().getWidth() / comp.getBounds().getHeight();
        this.scrollSpeedY = scrollSpeedX / this.delta;

        recalculateMenuRectangles();
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
}
