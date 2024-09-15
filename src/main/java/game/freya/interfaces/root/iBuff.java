package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;
import game.freya.dto.roots.CharacterDto;

import java.io.Serializable;

@RootInterface
public interface iBuff extends Serializable {
    void activate(CharacterDto characterDto);

    void deactivate(CharacterDto characterDto);
}
