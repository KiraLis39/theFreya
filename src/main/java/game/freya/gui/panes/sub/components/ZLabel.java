package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ZLabel extends JLabel {

    public ZLabel(String text, Icon icon) {
        super(text, icon, LEFT);
        setFont(Constants.DEBUG_FONT);
        setForeground(Color.WHITE);
        setVerticalAlignment(TOP);
        setBorder(new EmptyBorder(0, 3, 0, 0));
    }
}
