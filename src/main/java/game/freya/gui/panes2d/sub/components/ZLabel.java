package game.freya.gui.panes2d.sub.components;

import game.freya.config.Constants;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class ZLabel extends JLabel {

    public ZLabel(String text, Icon icon) {
        super(text, icon, LEFT);
        setFont(Constants.DEBUG_FONT);
        setForeground(Color.WHITE);
        setVerticalAlignment(TOP);
        setBorder(new EmptyBorder(0, 3, 0, 0));
    }
}
