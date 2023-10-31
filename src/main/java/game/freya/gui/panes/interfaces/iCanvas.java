package game.freya.gui.panes.interfaces;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface iCanvas extends MouseListener, MouseMotionListener, Runnable {
    void incrementFramesCounter();

    void stop();
}
