package game.freya.items.prototypes;

import game.freya.items.interfaces.iEdible;
import lombok.Getter;

import java.util.UUID;

public abstract class Edible implements iEdible {
    @Getter
    private final UUID suid;

    protected Edible(UUID suid) {
        this.suid = suid;
    }

    @Override
    public void eat() {

    }

    @Override
    public void onRotting() {

    }

    @Override
    public boolean isPoisoned() {
        return false;
    }
}
