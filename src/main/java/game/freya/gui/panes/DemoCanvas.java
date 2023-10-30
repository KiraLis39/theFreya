package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Slf4j
public class DemoCanvas extends Canvas implements MouseListener, Runnable {
    private final transient WorldDTO worldDTO;
    private boolean isGameActive;
    private int frames = 0;
    private long timeStamp = System.currentTimeMillis();

    public DemoCanvas(GraphicsConfiguration gConf, WorldDTO worldDTO) {
        super(gConf);
        this.worldDTO = worldDTO;

        setBackground(Color.DARK_GRAY);

        addMouseListener(this);

        new Thread(this).start();
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
                        worldDTO.draw(g2D, this);
                        frames++;
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

            if (System.currentTimeMillis() - timeStamp >= 1000L) {
                Constants.setRealFreshRate(frames);
                timeStamp = System.currentTimeMillis();
                frames = 0;
            }
        }
    }

    public void setGameActive(boolean gameActive) {
        this.isGameActive = gameActive;
    }
}
