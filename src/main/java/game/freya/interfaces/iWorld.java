package game.freya.interfaces;

import game.freya.dto.roots.EnvironmentDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.GameCanvas;
import game.freya.services.GameControllerService;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface iWorld {
    UUID getUid();

    UUID getAuthor();

    void init(GameCanvas canvas, GameControllerService controller);

    Dimension getDimension();

    void draw(Graphics2D g2D) throws AWTException;

    void addEnvironment(EnvironmentDto o);

    Set<EnvironmentDto> getEnvironments();

    boolean isLocalWorld();

    boolean isNetAvailable();

    LocalDateTime getCreateDate();

    HardnessLevel getHardnesslevel();

    void generate();
}
