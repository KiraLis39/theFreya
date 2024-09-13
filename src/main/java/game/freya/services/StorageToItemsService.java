package game.freya.services;

import game.freya.dto.roots.ItemStack;
import game.freya.dto.roots.StorageDto;
import game.freya.entities.middles.StorageToItems;
import game.freya.entities.middles.StorageToItemsPK;
import game.freya.repositories.StorageToItemsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageToItemsService {
    private final StorageToItemsRepo storageToItemsRepo;

    @Transactional(readOnly = true)
    public int findCountByItemUidAndStorageUid(UUID itemUid, UUID storageUid) {
        return storageToItemsRepo.findById_ItemUidAndId_StorageUid(itemUid, storageUid);
    }

    @Transactional(readOnly = true)
    public boolean existsByItemUidAndStorageUid(UUID itemUid, UUID storageUid) {
        return storageToItemsRepo.existsById(StorageToItemsPK.builder().itemUid(itemUid).storageUid(storageUid).build());
    }

    /**
     * Обновляет запись о стаке предметов промежуточной таблицы между хранилищами и предметами.
     *
     * @param stack      стэк предметов.
     * @param storageDto хранилище.
     * @param count      актуальное количество указанных выше предметов.
     * @return актуальное количество указанных выше предметов либо -1 при ошибке операции.
     */
    public int updateStackCountByItemUidAndStorageUid(ItemStack stack, StorageDto storageDto, int count) {
        if (existsByItemUidAndStorageUid(stack.itemUid(), storageDto.getUid())) {
            // если стак этих предметов уже лежит в хранилище:
            return storageToItemsRepo.updateById_ItemUidAndId_StorageUid(stack.itemUid(), storageDto.getUid(), count);
        }

        try {
            // если это - первый стак этих предметов в данном хранилище:
            StorageToItems stItem = new StorageToItems()
                    .id(StorageToItemsPK.builder().itemUid(stack.itemUid()).storageUid(storageDto.getUid()).build())
                    .itemName(stack.itemName())
                    .storageName(storageDto.getName())
                    .haveCount(count);
            storageToItemsRepo.saveAndFlush(stItem);
            return count;
        } catch (Exception e) {
            log.error(e.getMessage());
            return -1;
        }
    }

    public void deleteByItemUidAndStorageUid(UUID itemUid, UUID storageUid) {
        storageToItemsRepo.deleteByItemUidAndStorageUid(itemUid, storageUid);
    }
}
