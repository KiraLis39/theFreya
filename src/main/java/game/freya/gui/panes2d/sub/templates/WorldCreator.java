package game.freya.gui.panes2d.sub.templates;

import game.freya.enums.other.HardnessLevel;

import javax.swing.*;

public abstract class WorldCreator extends JPanel {
    public abstract String getWorldName();

    public abstract HardnessLevel getHardnessLevel();

    public abstract boolean isNetAvailable();

    public abstract String getNetPassword(); // bcrypt
}
