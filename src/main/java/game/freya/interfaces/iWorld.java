package game.freya.interfaces;

import game.freya.GameController;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.GameCanvas;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface iWorld {
    UUID getUid();

    UUID getAuthor();

    void init(GameCanvas canvas, GameController controller);

    Dimension getDimension();

    void draw(Graphics2D g2D) throws AWTException;

    void addEnvironment(iEnvironment o);

    Set<iEnvironment> getEnvironments();

    boolean isLocalWorld();

    boolean isNetAvailable();

    LocalDateTime getCreateDate();

    HardnessLevel getHardnesslevel();
}
