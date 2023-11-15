package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.JSlider;
import java.awt.Color;

public class JZlider extends JSlider {

    public JZlider(int canvasWidth, String name) {
        setName(name);
//        setPreferredSize(new Dimension(canvasWidth / 3, 35));

        setDoubleBuffered(false);
        setFocusable(false);
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
        setFont(Constants.GAME_FONT_01);

        setPaintTicks(true);
        setMinorTickSpacing(5);
        setMajorTickSpacing(25);
        setSnapToTicks(true);

        setPaintLabels(true);
        setPaintTrack(true);
        setMinimum(0);
        setMaximum(100);
    }
}
