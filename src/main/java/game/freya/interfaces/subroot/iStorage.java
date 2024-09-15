package game.freya.interfaces.subroot;

import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.StorageDto;
import game.freya.interfaces.root.iDestroyable;

import java.util.UUID;

/**
 * Интерфейс складов
 */
public interface iStorage extends iDestroyable {
    short getCapacity();

    boolean putItem(ItemDto itemDto, int count);

    UUID removeItem(UUID itemUid, int count);

    boolean translate(StorageDto dstStorage, ItemDto itemDto, int count);

    boolean has(UUID itemUid);

    boolean isEmpty();

    boolean isFull();

    void removeAll();

    int getItemsHaveCount(UUID itemsUid);
}
