package game.freya.services;

import game.freya.dto.roots.ItemStack;
import game.freya.dto.roots.StorageDto;
import game.freya.entities.roots.prototypes.Item;
import game.freya.entities.roots.prototypes.Storage;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.ItemsMapper;
import game.freya.mappers.StorageMapper;
import game.freya.repositories.ItemRepository;
import game.freya.repositories.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Один из основных сервисов инвентарей и прочих хранилищ, обмена предметами,
 * а так же контроллер промежуточной таблицы storageToItems (через сервис storageToItemsService)
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final ItemsMapper itemsMapper;
    private final StorageMapper storageMapper;
    private final StorageToItemsService storageToItemsService;

    public Optional<StorageDto> createStorage(StorageDto storageDto) {
//        List<ItemStack> storageStacks = storageDto.getStacks();
        storageDto.setStacks(null);

        Storage savedWithoutItems = storageRepository.save(storageMapper.toEntity(storageDto));
//        List<ItemStack> foundStacks = storageStacks.stream().filter(stack -> itemRepository.existsById(stack.getItemUid())).toList();
//        savedWithoutItems.setStacks(itemRepository.findAllById(foundStacks.stream().map(stack -> stack.getItemUid()).collect(Collectors.toSet())));

        StorageDto result = storageMapper.toDto(storageRepository.saveAndFlush(savedWithoutItems));
        return Optional.of(result);
    }

    public ResponseEntity<HttpStatus> storeTo(UUID storageUid, UUID itemUid) {
        Optional<Storage> aimStorage = storageRepository.findById(storageUid);
        Optional<Item> storedItem = itemRepository.findById(itemUid);
        if (aimStorage.isEmpty() || storedItem.isEmpty()) {
            log.error("Не был обнаружен предмет {} или целевое хранилище {}", itemUid, storageUid);
            return ResponseEntity.notFound().build();
        }

        StorageDto storageDto = storageMapper.toDto(aimStorage.get());
        if (storageDto.putItem(itemsMapper.toDto(storedItem.get()))) {
            if (storageDto.getStacks() != null) {
                HashMap<ItemStack, Integer> countsMap = new HashMap<>();
                storageDto.getStacks()
                        .forEach(stack -> countsMap.put(stack, countsMap.getOrDefault(stack, 0) + stack.count()));
                for (Map.Entry<ItemStack, Integer> entry : countsMap.entrySet()) {
                    if (storageToItemsService.updateStackCountByItemUidAndStorageUid(entry.getKey(), storageDto, entry.getValue()) < 1) {
                        log.warn("Не было обновлено ни одного стака '{}' ({}) в БД! Проверить!", entry.getKey(), entry.getKey().itemUid());
                    }
                }
            }

            return ResponseEntity.ok().build();
        }
        throw new GlobalServiceException(ErrorMessages.GAME_OPERATION_RESTRICTED, "Размещение предмета %s не прошло".formatted(storedItem.get().getName()));
    }

    public ResponseEntity<HttpStatus> deleteStorageByUid(UUID storageUid) {
        if (storageRepository.existsById(storageUid)) {
            storageRepository.deleteById(storageUid);
            log.info("Удалёно хранилище {}", storageUid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<HttpStatus> translateItems(UUID srcUid, UUID dstUid, UUID itemUid, int count) {
        Optional<Storage> srcStorageOpt = storageRepository.findById(srcUid);
        Optional<Storage> dstStorageOpt = storageRepository.findById(dstUid);
        if (srcStorageOpt.isEmpty() || dstStorageOpt.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.STORAGE_NOT_FOUND, "Items translations wants exists storages %s and %s".formatted(srcUid, dstUid));
        }

        Optional<Item> translatedItemOpt = itemRepository.findById(itemUid);
        if (translatedItemOpt.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.ITEM_NOT_FOUND, "Item %s not found in DB".formatted(itemUid));
        }

        int itemsAvailable = storageToItemsService.findCountByItemUidAndStorageUid(itemUid, srcUid);
        if (itemsAvailable < count) {
            throw new GlobalServiceException(ErrorMessages.NOT_ENOUGH_RESOURCES, "В источнике недостаточно предметов (требовалось %s)".formatted(count));
        }

        // сам перенос предмета(-ов) из src в dst:
        StorageDto srcDto = storageMapper.toDto(srcStorageOpt.get());
        StorageDto dstDto = storageMapper.toDto(dstStorageOpt.get());
        try {
            if (srcDto.removeItem(itemUid, count) != null) {
                if (dstDto.putItem(itemsMapper.toDto(translatedItemOpt.get()), count)) {
                    // если не возникло исключений - фиксируем:
                    storageToItemsService.updateStackCountByItemUidAndStorageUid(
                            new ItemStack().itemUid(translatedItemOpt.get().getUid()).itemName(translatedItemOpt.get().getName()), srcDto,
                            srcDto.getItemsHaveCount(translatedItemOpt.get().getUid()));
                    storageToItemsService.updateStackCountByItemUidAndStorageUid(
                            new ItemStack().itemUid(translatedItemOpt.get().getUid()).itemName(translatedItemOpt.get().getName()), dstDto,
                            dstDto.getItemsHaveCount(translatedItemOpt.get().getUid()));

                    // нужно ли очистить пустую ячейку:
                    clearCellIfEmpty(srcDto, translatedItemOpt.get().getUid());
                    clearCellIfEmpty(dstDto, translatedItemOpt.get().getUid());

                    return ResponseEntity.ok().build();
                }
            }
        } catch (Exception e) {
            log.error("Что-то пошло не так при передаче предметов: {}", e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Удаляет запись из промежуточной таблицы (очищает ячейку хранилища),
     * если количество предметов в ячейке исчерпано.
     *
     * @param storageDto хранилище.
     * @param itemuid    предмет, наличие которого требуется проверить.
     */
    private void clearCellIfEmpty(StorageDto storageDto, UUID itemuid) {
        if (storageDto.getItemsHaveCount(itemuid) <= 0) {
            // если предметов в стаке больше нет - удаляем стак из базы:
            storageToItemsService.deleteByItemUidAndStorageUid(itemuid, storageDto.getUid());
        }
    }
}
