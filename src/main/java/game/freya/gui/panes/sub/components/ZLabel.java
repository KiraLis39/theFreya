package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.Icon;
import javax.swing.JLabel;
import java.awt.Color;

public class ZLabel extends JLabel {

    public ZLabel(String text, Icon icon) {
        super(text, icon, LEFT);
        setFont(Constants.DEBUG_FONT);
        setForeground(Color.WHITE);
        setVerticalAlignment(TOP);
    }
}
