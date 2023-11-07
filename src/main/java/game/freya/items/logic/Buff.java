package game.freya.items.logic;

import game.freya.entities.dto.PlayerDTO;

public abstract class Buff {
    public abstract void activate(PlayerDTO playerDTO);

    public abstract void deactivate(PlayerDTO playerDTO);
}
