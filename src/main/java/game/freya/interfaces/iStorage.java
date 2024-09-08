package game.freya.interfaces;

import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.StorageDto;

import java.util.UUID;

/**
 * Интерфейс складов
 */
public interface iStorage {

    short getCapacity();

    void put(ItemDto item);

    ItemDto remove(UUID itemUid);

    void translate(StorageDto dstStorage, UUID itemUid);

    boolean has(UUID itemUid);

    boolean has(String itemName);

    boolean isEmpty();

    boolean isFull();

    void removeAll();
}
