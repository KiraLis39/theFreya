package game.freya.gui.panes.interfaces;

import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface iCanvas extends MouseListener, MouseMotionListener, ComponentListener, Runnable {
    void incrementFramesCounter();

    void stop();
}
