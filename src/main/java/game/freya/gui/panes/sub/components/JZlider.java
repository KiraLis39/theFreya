package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class JZlider extends JSlider {

    public JZlider(String name) {
        setName(name);
        setBorder(new EmptyBorder(0, 0, 0, 3));

        setDoubleBuffered(false);
        setFocusable(false);
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
        setFont(Constants.GAME_FONT_01);

        setMinorTickSpacing(5);
        setMajorTickSpacing(15);
        setPaintTicks(true);
        setSnapToTicks(true);

        setPaintLabels(true);
        setPaintTrack(true);
        setMinimum(0);
        setMaximum(100);
    }
}
