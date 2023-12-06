package game.freya.entities.logic;

import game.freya.entities.dto.HeroDTO;

public abstract class Buff {
    public abstract void activate(HeroDTO playerDTO);

    public abstract void deactivate(HeroDTO playerDTO);
}
