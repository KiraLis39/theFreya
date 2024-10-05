package game.freya.gui.panes2d.sub.components;

import fox.render.fox_lfui.MyButtonUI;
import game.freya.config.Constants;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;

public class FButton extends JButton {

    public FButton() {
        this(null, null);
    }

    public FButton(String text) {
        this(text, null);
    }

    public FButton(String text, Icon icon) {
        super(text, icon);

        setUI(new MyButtonUI());
        setFocusPainted(false);
        setDoubleBuffered(false);
        setForeground(Color.WHITE);
        setBackground(Color.DARK_GRAY);
        setFont(Constants.DEBUG_FONT);
    }
}
