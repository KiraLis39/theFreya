package game.freya.interfaces;

import game.freya.entities.roots.Storage;

import java.util.UUID;

/**
 * Интерфейс складов
 */
public interface iStorage {

    short capacity();

    void put(iStorable storable);

    iStorable remove(UUID storableUid);

    void translate(Storage dst, UUID storableUid);

    boolean has(UUID storableUid);

    boolean has(String storableName);

    boolean isEmpty();

    boolean isFull();

    void removeAll();
}
