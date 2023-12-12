package game.freya.interfaces;

import game.freya.GameController;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.GameCanvas;
import game.freya.items.prototypes.Environment;

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

    void addEnvironment(Environment o);

    Set<Environment> getEnvironments();

    boolean isLocalWorld();

    boolean isNetAvailable();

    LocalDateTime getCreateDate();

    HardnessLevel getHardnesslevel();

    void generate();
}
