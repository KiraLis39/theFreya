package game.freya.gui.panes;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;

@Slf4j
public class GameCanvas extends FoxCanvas { // уже включает в себя MouseListener, MouseMotionListener, Runnable
    private final transient WorldDTO worldDTO;
    private boolean isGameActive;

    public GameCanvas(WorldDTO worldDTO) {
        super(Constants.getGraphicsConfiguration());
        this.worldDTO = worldDTO;

        setBackground(Color.MAGENTA.darker().darker());
        addMouseListener(this);

        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isGameActive = true;

        while (isGameActive) {
            if (getParent() == null || Constants.isPaused() || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(3);
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);
                        worldDTO.draw(g2D, this);
                        super.drawDebugInfo(g2D, worldDTO.getTitle()); // отладочная информация
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
}
