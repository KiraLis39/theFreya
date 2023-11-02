package game.freya.entities.dto;

import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.FoxCanvas;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Slf4j
@RequiredArgsConstructor
public class WorldDTO extends ComponentAdapter implements iWorld, MouseWheelListener, MouseMotionListener, MouseListener {

    private final UUID uid;

    private final String title;

    private final int passwordHash;

    private final Dimension dimension;

    private final HardnessLevel level;

//    private final double dragSpeed = 9D;

    private final Map<String, PlayerDTO> players = HashMap.newHashMap(2);

    private FoxCanvas canvas;

    private BufferedImage gameMap;

    private Rectangle2D viewPort;

    private double scrollSpeedX, scrollSpeedY;

    private boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;

    private boolean initialized = false;

    private double delta;

    private Point mousePressedOnPoint = MouseInfo.getPointerInfo().getLocation();

    public WorldDTO(String title) {
        this(UUID.randomUUID(), title, HardnessLevel.EASY, new Dimension(64, 32), -1);
    }

    public WorldDTO(String title, HardnessLevel level, int passwordHash) {
        this(UUID.randomUUID(), title, level, new Dimension(64, 32), passwordHash);
    }

    public WorldDTO(UUID uid, String title, HardnessLevel level, Dimension dimension, int passwordHash) {
        this.uid = uid;
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
    }


    @Override
    public void addPlayer(PlayerDTO playerDTO) {
        players.putIfAbsent(playerDTO.getNickName(), playerDTO);
    }

    @Override
    public void removePlayer(PlayerDTO playerDTO) {
        players.remove(playerDTO.getNickName());
    }

    private void update() {
        // если ещё не нарисована карта - рисуем:
        if (this.gameMap == null) {
            createGameMapImage();
        }

        // если не создан вьюпорт - создаём:
        if (this.viewPort == null) {
            this.viewPort = new Rectangle2D.Double(
                    gameMap.getWidth() / 2D - canvas.getWidth() / 2D,
                    gameMap.getHeight() / 2D - canvas.getHeight() / 2D,
                    gameMap.getWidth() / 2D + canvas.getWidth() / 2D,
                    gameMap.getHeight() / 2D + canvas.getHeight() / 2D);
        }
    }

    private void createGameMapImage() {
        this.gameMap = new BufferedImage(
                dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) gameMap.getGraphics();
        g2D.setColor(Color.GREEN);
        g2D.setStroke(new BasicStroke(2f));

        int n = 1;
        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {
            g2D.drawString(n + ")", i - 26, 12);
            g2D.drawLine(i, 0, i, gameMap.getHeight());

            g2D.drawString(n + ")", 6, i - 16);
            g2D.drawLine(0, i, gameMap.getWidth(), i);
            n++;
        }

        g2D.setColor(Color.RED);
        g2D.setStroke(new BasicStroke(2f));
        g2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
        g2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());

        g2D.dispose();
    }

    @Override
    public void draw(Graphics2D g2D) {
        // очищаем экран:
        g2D.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // not-pause events and changes:
        if (!Constants.isPaused()) {
            if (isMouseRightEdgeOver) {
                dragLeft();
            }
            if (isMouseLeftEdgeOver) {
                dragRight();
            }
            if (isMouseUpEdgeOver) {
                dragDown();
            }
            if (isMouseDownEdgeOver) {
                dragUp();
            }
        }

        if (gameMap != null) {
            // рисуем заготовленный имедж:
            g2D.drawImage(gameMap, 0, 0, canvas.getWidth(), canvas.getHeight(),

                    viewPort.getBounds().x, viewPort.getBounds().y,
                    viewPort.getBounds().width, viewPort.getBounds().height,

                    canvas);
        }
    }

    public void draw(Graphics2D g2D, FoxCanvas canvas) {
        if (!initialized) {
            init(canvas);
        }
        this.draw(g2D);
    }

    private void init(FoxCanvas mainMenu) {
        if (canvas != mainMenu) {
            canvas = mainMenu;
            canvas.addMouseWheelListener(this);
            canvas.addMouseMotionListener(this);
            canvas.addComponentListener(this);
            canvas.addMouseListener(this);

            update();
        }

        setScrollSpeed(20D);

        initialized = true;
    }

    private void zoomIn() {
        log.debug("Zoom in...");

        if (viewPort.getWidth() <= viewPort.getX() + Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
                || viewPort.getHeight() <= viewPort.getY() + Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS
        ) {
            log.warn("Can`t zoom in (1)");
            return;
        } else {
            log.info("VP w/h: {}/{} but allowed size: {}", viewPort.getWidth(), viewPort.getHeight(),
                    Constants.MAP_CELL_DIM * Constants.MIN_ZOOM_OUT_CELLS);
        }

        viewPort.setRect(viewPort.getX() + scrollSpeedX, viewPort.getY() + scrollSpeedY,
                viewPort.getWidth() - scrollSpeedX, viewPort.getHeight() - scrollSpeedY);

        log.warn("Can`t zoom in");
    }

    private void zoomOut() {
        log.debug("Zoom out...");

        double vpXSrc = viewPort.getX();
        double vpXDst = viewPort.getWidth();
        double vpWidth = vpXDst - vpXSrc;
        double vpYSrc = viewPort.getY();
        double vpYDst = viewPort.getHeight();
        double vpHeight = vpYDst - vpYSrc;
        int maxCellsSize = Constants.MAP_CELL_DIM * Constants.MAX_ZOOM_OUT_CELLS;

        // если окно больше установленного лимита:
        if (vpWidth >= maxCellsSize || vpHeight >= maxCellsSize) {
            log.warn("Can`t zoom out: vpWidth = {} and vpHeight = {} but maxCellsSize is {}", vpWidth, vpHeight, maxCellsSize);
            return;
        }

        // если окно уже максимального размера:
        if (vpWidth >= gameMap.getWidth() && vpHeight >= gameMap.getHeight()) {
            log.warn("Can`t zoom out: maximum size reached.");
            return;
        }

        if (vpXSrc <= 0) {
            if (vpYSrc <= 0) {
                // камера сверху слева:
                viewPort.setRect(0, 0, vpWidth + scrollSpeedX, vpHeight + scrollSpeedY);
            } else if (vpYDst >= gameMap.getHeight()) {
                // камера снизу слева:
                viewPort.setRect(0, vpYSrc - scrollSpeedY, vpWidth + scrollSpeedX, vpYDst);
            }
        } else if (vpXDst >= gameMap.getWidth()) {
            if (vpYSrc <= 0) {
                // камера сверху справа:
                viewPort.setRect(vpXSrc - scrollSpeedX, 0, gameMap.getWidth(), vpHeight + scrollSpeedY);
            } else if (vpYDst >= gameMap.getHeight()) {
                // камера снизу справа:
                viewPort.setRect(vpXSrc - scrollSpeedX, vpYSrc - scrollSpeedY, gameMap.getWidth(), gameMap.getHeight());
            }
        }
    }

    private void dragLeft() {
        if (canDragLeft()) {
            log.debug("Drag left...");
            viewPort.setRect(
                    viewPort.getX() + Constants.getDragSpeed(), viewPort.getY(),
                    viewPort.getWidth() + Constants.getDragSpeed(), viewPort.getHeight());
        }
    }

    private void dragRight() {
        if (canDragRight()) {
            log.debug("Drag right...");
            viewPort.setRect(viewPort.getX() - Constants.getDragSpeed(), viewPort.getY(),
                    viewPort.getWidth() - Constants.getDragSpeed(), viewPort.getHeight());
        }
    }

    private void dragUp() {
        if (canDragUp()) {
            log.debug("Drag up...");
            viewPort.setRect(viewPort.getX(), viewPort.getY() + Constants.getDragSpeed(),
                    viewPort.getWidth(), viewPort.getHeight() + Constants.getDragSpeed());
        }
    }

    private void dragDown() {
        if (canDragDown()) {
            log.debug("Drag down...");
            viewPort.setRect(viewPort.getX(), viewPort.getY() - Constants.getDragSpeed(),
                    viewPort.getWidth(), viewPort.getHeight() - Constants.getDragSpeed());
        }
    }

    private boolean canDragDown() {
        return viewPort.getY() > 0;
    }

    private boolean canDragUp() {
        return viewPort.getHeight() < gameMap.getHeight();
    }

    private boolean canDragLeft() {
        return viewPort.getWidth() < gameMap.getWidth();
    }

    private boolean canDragRight() {
        return viewPort.getX() > 0;
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
        if (UserConfig.isDragGameFieldOnFrameEdgeReached()) {
            isMouseLeftEdgeOver = p.getX() <= 20 && p.getX() > 1;
            isMouseRightEdgeOver = p.getX() >= canvas.getWidth() - 20 && p.getX() < canvas.getWidth() - 1;
            isMouseUpEdgeOver = p.getY() <= 10 && p.getY() > 1;
            isMouseDownEdgeOver = p.getY() >= canvas.getHeight() - 20 && p.getY() < canvas.getHeight() - 1;
            log.debug("isMouseLeftEdgeOver: {} | isMouseRightEdgeOver: {} | isMouseUpEdgeOver: {} | isMouseDownEdgeOver: {}",
                    isMouseLeftEdgeOver, isMouseRightEdgeOver, isMouseUpEdgeOver, isMouseDownEdgeOver);
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

    public void setScrollSpeed(double scrollSpeed) {
        this.delta = canvas.getBounds().getWidth() / canvas.getBounds().getHeight();
        this.scrollSpeedX = scrollSpeed;
        this.scrollSpeedY = scrollSpeedX / this.delta;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Component comp = e.getComponent();
        this.delta = comp.getBounds().getWidth() / comp.getBounds().getHeight();
        this.scrollSpeedY = scrollSpeedX / this.delta;

        if (gameMap != null) {
            // пересоздаёем вьюпорт под новое окно:
            this.viewPort = new Rectangle2D.Double(
                    gameMap.getWidth() / 2D - comp.getBounds().getWidth() / 2D,
                    gameMap.getHeight() / 2D - comp.getBounds().getHeight() / 2D,
                    gameMap.getWidth() / 2D + comp.getBounds().getWidth() / 2D,
                    gameMap.getHeight() / 2D + comp.getBounds().getHeight() / 2D);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mousePressedOnPoint = e.getPoint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
