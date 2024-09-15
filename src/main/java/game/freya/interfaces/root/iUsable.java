package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;

import java.io.Serializable;

/**
 * Интерфейс для предметов взаимодействия
 */
@RootInterface
public interface iUsable extends Serializable {
    void use();
}
