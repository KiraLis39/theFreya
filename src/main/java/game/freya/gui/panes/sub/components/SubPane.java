package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;

public class SubPane extends JPanel {

    public SubPane(String title) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));
        setOpaque(false);
        setIgnoreRepaint(true);
        setDoubleBuffered(false);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                title, 1, 2, Constants.DEBUG_FONT, Color.WHITE));
    }
}
