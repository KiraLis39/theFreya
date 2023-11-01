package game.freya.gui.panes;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, Runnable
public class GameCanvas extends FoxCanvas {
    private final transient WorldDTO worldDTO;
    private boolean isGameActive;

    public GameCanvas(WorldDTO worldDTO) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas");
        this.worldDTO = worldDTO;

        setBackground(Color.MAGENTA.darker().darker());
        addMouseListener(this);

        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isGameActive = true;

        while (isGameActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(2);
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);

                        // draw all World`s graphic:
                        worldDTO.draw(g2D, this);

                        // is needs to draw the Menu:
                        if (Constants.isPaused()) {
                            drawPauseMode(g2D);
                        }

                        // draw debug info corner if debug mode on:
                        if (Constants.isDebugInfoVisible()) {
                            super.drawDebugInfo(g2D, worldDTO.getTitle()); // отладочная информация
                        }

                        g2D.dispose();
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

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString("- PAUSED -",
                (int) (getWidth() / 2D - Constants.FFB.getStringBounds(g2D, "- PAUSED -").getWidth() / 2),
                getHeight() / 2);

        // fill left gray menu polygon:
        g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
        if (getLeftGrayMenuPoly() == null) {
            reloadShapes();
        }
        g2D.fillPolygon(getLeftGrayMenuPoly());
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
    public void stop() {
        this.isGameActive = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void componentResized(ComponentEvent e) {
        reloadShapes();
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
