package game.freya.items;

import game.freya.items.prototypes.Tools;

import java.util.UUID;

/**
 * The shovel tool example
 */
public class Shovel extends Tools {

    public Shovel() {
        this("The shovel");
    }

    public Shovel(String name) {
        super(UUID.randomUUID(), name);
        this.setName(name);
    }
}
