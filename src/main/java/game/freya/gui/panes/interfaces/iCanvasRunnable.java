package game.freya.gui.panes.interfaces;

import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public interface iCanvasRunnable extends MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable {
    void stop();

    void init();
}
