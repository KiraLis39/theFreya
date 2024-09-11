package game.freya.interfaces;

import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.StorageDto;

import java.util.UUID;

/**
 * Интерфейс складов
 */
public interface iStorage {

    short getCapacity();

    boolean putItem(ItemDto itemDto, int count);

    UUID removeItem(UUID itemUid, int count);

    boolean translate(StorageDto dstStorage, UUID itemUid, int count);

    boolean has(UUID itemUid);

    boolean isEmpty();

    boolean isFull();

    void removeAll();

    int getItemsHaveCount(UUID itemsUid);
}
