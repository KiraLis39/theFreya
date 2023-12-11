package game.freya.gui.panes.sub.templates;

import game.freya.enums.other.HardnessLevel;

import javax.swing.JPanel;

public abstract class WorldCreator extends JPanel {
    public abstract String getWorldName();

    public abstract HardnessLevel getHardnessLevel();

    public abstract boolean isNetAvailable();

    public abstract int getNetPasswordHash();
}
