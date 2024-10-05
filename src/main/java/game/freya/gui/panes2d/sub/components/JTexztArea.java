package game.freya.gui.panes2d.sub.components;

import game.freya.config.Constants;

import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class JTexztArea extends JTextArea {
    public JTexztArea(String text, int rows, int columns) {
        super(text, rows, columns);
        setWrapStyleWord(true);
        setLineWrap(true);
        setBorder(new EmptyBorder(3, 3, 3, 3));
        setFocusable(false);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setForeground(Color.WHITE);
        setFont(Constants.DEBUG_FONT);
        setIgnoreRepaint(true);
    }
}
