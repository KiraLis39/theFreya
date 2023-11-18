package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Color;

public class SubPane extends JPanel {

    public SubPane(String title) {
        setLayout(new BorderLayout(3, 3));
        setOpaque(false);
        setIgnoreRepaint(true);
        setDoubleBuffered(false);

        if (title != null) {
            if (title.length() == 1) {
                setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                        title, 2, 2, Constants.LITTLE_UNICODE_FONT, Color.WHITE));
            } else {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                                title, 1, 2, Constants.DEBUG_FONT, Color.WHITE),
                        BorderFactory.createEmptyBorder(0, 3, 3, 3)
                ));
            }
        } else {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(BevelBorder.RAISED, Color.BLACK, Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(0, 0, 3, 3)
            ));
        }
    }
}
