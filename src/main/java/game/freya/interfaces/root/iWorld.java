package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.EnvironmentDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.services.GameControllerService;

import java.awt.*;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@RootInterface
public interface iWorld extends Serializable {
    UUID getUid();

    UUID getCreatedBy();

    Dimension getSize();

    String getName();

    String getPassword();

    String getAddress();

    void addEnvironment(EnvironmentDto o);

    boolean isLocal();

    boolean isNetAvailable();

    HardnessLevel getHardnessLevel();

    String getCacheKey();

    Set<EnvironmentDto> getEnvironments();

    Set<CharacterDto> getHeroes();

    void generate();

    void init(GameControllerService controller);

    void draw(Graphics2D g2D) throws AWTException;
}
