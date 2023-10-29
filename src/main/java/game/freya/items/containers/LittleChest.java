package game.freya.items.containers;

import game.freya.items.prototypes.Storage;

import java.util.UUID;

public class LittleChest extends Storage {

    public LittleChest() {
        this("The little chest");
    }

    public LittleChest(String name) {
        super(UUID.randomUUID(), name);
    }
}
