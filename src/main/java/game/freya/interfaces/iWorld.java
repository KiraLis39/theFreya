package game.freya.interfaces;

import game.freya.dto.roots.EnvironmentDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.GameCanvas;
import game.freya.services.GameControllerService;

import java.awt.*;
import java.util.Set;

public interface iWorld extends iGameObject {
    void init(GameCanvas canvas, GameControllerService controller);

    Dimension getSize();

    void addEnvironment(EnvironmentDto o);

    Set<EnvironmentDto> getEnvironments();

    boolean isLocalWorld();

    boolean isNetAvailable();

    HardnessLevel getHardnesslevel();

    void generate();
}
