package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CheckBokz extends JCheckBox {

    public CheckBokz(String name) {
        setName(name);

        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setFocusable(false);
        setPreferredSize(new Dimension(32, 20));
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
        setFont(Constants.GAME_FONT_01);
        setVerticalAlignment(TOP);
        setBorder(new EmptyBorder(3, 3, 3, 3));
    }
}
