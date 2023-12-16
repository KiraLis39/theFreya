package game.freya.entities.logic;

import game.freya.items.prototypes.GameCharacter;

public abstract class Buff {
    private String name;

    public abstract void activate(GameCharacter character);

    public abstract void deactivate(GameCharacter character);

    public String getName() {
        return this.name;
    }
}
