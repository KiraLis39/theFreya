package game.freya.items;

import game.freya.items.prototypes.Tool;

import java.util.UUID;

/**
 * The shovel tool example
 */
public class Shovel extends Tool {

    public Shovel() {
        this("The shovel");
    }

    public Shovel(String name) {
        super(UUID.randomUUID(), name);
        this.setName(name);
    }
}
