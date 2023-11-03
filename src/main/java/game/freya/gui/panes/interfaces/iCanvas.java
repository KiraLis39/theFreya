package game.freya.gui.panes.interfaces;

import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public interface iCanvas extends MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable {
    void incrementFramesCounter();

    void stop();
}
