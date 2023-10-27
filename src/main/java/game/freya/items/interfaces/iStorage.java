package game.freya.items.interfaces;

import game.freya.items.prototypes.Storable;
import game.freya.items.prototypes.Storage;

/**
 * Интерфейс для складируемых айтемов
 */
public interface iStorage {
    short size(); // size of the storage

    void put(Storable storable);

    Storable get(Storable storable);

    void translate(Storage aim, Storable storable);

    boolean has(Storable storable);
}
