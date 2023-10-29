package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.utils.ExceptionUtils;
import game.freya.utils.Render;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;

@Slf4j
public class MainMenuCP extends Canvas implements MouseWheelListener, MouseListener, MouseMotionListener, Runnable {
    private final transient WorldDTO worldDTO;
    private double cellDim = 0d;
    private int oldWidth;
    private double cellMultiplier = 12d;
    private double oldCellMultiplier = cellMultiplier;
    private boolean isGameActive = false;

    public MainMenuCP(GraphicsConfiguration graphicsConfiguration, WorldDTO worldDTO) {
        super(graphicsConfiguration);
        this.worldDTO = worldDTO;

        setBackground(Color.DARK_GRAY);

        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void paint(Graphics g) {
        if (getParent() != null) {
            if (!isGameActive) {
                isGameActive = true;
            }
            if (oldWidth != getParent().getWidth() || oldCellMultiplier != cellMultiplier) {
                oldCellMultiplier = cellMultiplier;
                oldWidth = getParent().getWidth();
                cellDim = oldWidth / oldCellMultiplier;
            }

            Graphics2D g2D = (Graphics2D) g;
            g2D.setColor(Color.GREEN);
            for (double i = 1d; i < cellMultiplier; i += 1d) {
                g2D.drawLine((int) (cellDim * i), 0, (int) (cellDim * i), getHeight());
                g2D.drawLine(0, (int) (cellDim * i), getWidth(), (int) (cellDim * i));
            }
            g2D.dispose();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        switch (e.getWheelRotation()) {
            case 1 -> cellMultiplier += 0.3d;
            case -1 -> cellMultiplier -= 0.3d;
            default -> log.warn("MouseWheelEvent unknown action: {}", e.getWheelRotation());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void run() {
        while (isGameActive) {
            if (Constants.isPaused()) {
                Thread.yield();
                continue;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(3);
                }
                BufferStrategy bs = getBufferStrategy();

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) bs.getDrawGraphics();
                        Render.rendering(g2D);
                        worldDTO.draw(g2D);
                        g2D.dispose();
                    } while (bs.contentsRestored());
                } while (bs.contentsLost());
                bs.show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            try {
                Thread.sleep(1000 / Constants.getScreenDiscreteValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
