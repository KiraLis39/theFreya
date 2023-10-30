package game.freya.gui.panes.interfaces;

import java.awt.event.MouseListener;

public interface iCanvas extends MouseListener, Runnable {
    void incrementFramesCounter();

    void stop();
}
