package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;

public class SubPane extends JPanel {
    private ZLabel headerLabel;

    private FButton connButton;

    private transient WorldDTO worldDTO;

    public SubPane(String title) {
        this(title, Color.DARK_GRAY);
    }

    public SubPane(String title, Color borderColor) {
        setLayout(new BorderLayout(3, 3));
        setOpaque(false);
//        setIgnoreRepaint(true);
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

    public ZLabel getHeaderLabel() {
        return headerLabel;
    }

    public void setHeaderLabel(ZLabel headerLabel) {
        this.headerLabel = headerLabel;
    }

    public FButton getConnButton() {
        return connButton;
    }

    public void setConnButton(FButton connButton) {
        this.connButton = connButton;
    }

    public WorldDTO getWorld() {
        return worldDTO;
    }

    public void setWorld(WorldDTO worldDTO) {
        this.worldDTO = worldDTO;
    }
}
