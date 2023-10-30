package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;

@Slf4j
public class MenuCanvas extends FoxCanvas { // уже включает в себя MouseListener, Runnable
    private boolean isMenuActive;

    public MenuCanvas() {
        super(Constants.getGraphicsConfiguration());
        setBackground(Color.DARK_GRAY.darker());
        addMouseListener(this);

        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isMenuActive = true;

        while (isMenuActive) {
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
                        drawMenu(g2D);
                        super.drawDebugInfo(g2D, null);
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

    private void drawMenu(Graphics2D g2D) {
        g2D.clearRect(0, 0, getWidth(), getHeight());
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
        this.isMenuActive = false;
    }
}
