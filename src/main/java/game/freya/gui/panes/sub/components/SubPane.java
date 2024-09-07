package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;
import game.freya.dto.WorldDTO;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SubPane extends JPanel {
    @Setter
    @Getter
    private ZLabel headerLabel;

    @Setter
    @Getter
    private FButton connButton;

    private transient WorldDTO worldDTO;

    public SubPane(String title) {
        this(title, Color.DARK_GRAY);
    }

    public SubPane(String title, Color borderColor) {
        setLayout(new BorderLayout(3, 3));
        setOpaque(false);
        setIgnoreRepaint(true);
        setDoubleBuffered(false);

        if (title == null) {
            setBorder(new EmptyBorder(0, 0, 3, 3));
//            setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createEtchedBorder(BevelBorder.RAISED, Color.BLACK, Color.DARK_GRAY),
//                    BorderFactory.createEmptyBorder(0, 0, 3, 3)
//            ));
        } else {
            if (title.length() == 1) {
                setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderColor, 1, true),
                        title, 2, 2, Constants.LITTLE_UNICODE_FONT, Color.WHITE));
            } else {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(borderColor, 3, true),
                                title, 1, 2, Constants.DEBUG_FONT, Color.WHITE),
                        BorderFactory.createEmptyBorder(0, 3, 3, 3)
                ));
            }
        }
    }

    public WorldDTO getWorld() {
        return worldDTO;
    }

    public void setWorld(WorldDTO worldDTO) {
        this.worldDTO = worldDTO;
    }
}
