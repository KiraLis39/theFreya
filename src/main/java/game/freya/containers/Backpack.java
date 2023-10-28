package game.freya.containers;

import game.freya.items.prototypes.Storage;

import java.util.UUID;

public class Backpack extends Storage {

    public Backpack() {
        this("The backpack");
    }

    public Backpack(String name) {
        super(UUID.randomUUID(), name);
    }
}
