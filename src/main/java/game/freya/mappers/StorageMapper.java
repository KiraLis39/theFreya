package game.freya.mappers;

import game.freya.dto.BackpackDto;
import game.freya.dto.LittleChestDto;
import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.ItemStack;
import game.freya.dto.roots.StorageDto;
import game.freya.entities.Backpack;
import game.freya.entities.LittleChest;
import game.freya.entities.roots.prototypes.Item;
import game.freya.entities.roots.prototypes.Storage;
import game.freya.services.StorageToItemsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageMapper {
    private final StorageToItemsService storageToItemsService;
    private ItemsMapper itemsMapper;

    @Autowired
    public void setMappers(@Lazy ItemsMapper itemsMapper) {
        this.itemsMapper = itemsMapper;
    }

    public Storage toEntity(StorageDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case LittleChestDto ch -> lchestDtoToLchest(ch);
            case BackpackDto b -> backpackDtoToBackpack(b);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    public StorageDto toDto(Storage entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case LittleChest ch -> lchestToLchestDto(ch);
            case Backpack b -> backpackToBackpackDto(b);
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    private BackpackDto backpackToBackpackDto(Backpack entity) {
        BackpackDto result = BackpackDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .ownerUid(entity.getOwnerUid())
                .location(entity.getLocation())
                .shape(entity.getShape())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .worldUid(entity.getWorldUid())
                .capacity(entity.getCapacity())
                .build();

        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemDto dto : itemsMapper.toDtos(entity.getItems())) {
                int count = storageToItemsService.findCountByItemUidAndStorageUid(dto.getUid(), result.getUid());
                // добавляем полные стаки предметов:
                while (count > dto.getStackCount()) {
                    stacks.add(new ItemStack()
                            .itemUid(dto.getUid())
                            .itemDto(dto)
                            .itemName(dto.getName())
                            .count(dto.getStackCount()));
                    count -= dto.getStackCount();
                }
                // добавляем последний (единственный), неполный стак:
                stacks.add(new ItemStack()
                        .itemUid(dto.getUid())
                        .itemDto(dto)
                        .itemName(dto.getName())
                        .count(count));
            }
            result.setStacks(stacks);
        }

        return result;
    }

    private Backpack backpackDtoToBackpack(BackpackDto dto) {
        Set<Item> items = dto.getStacks() == null || dto.getStacks().isEmpty() ? null
                : itemsMapper.toEntities(dto.getStacks().stream().map(ItemStack::getItemDto).collect(Collectors.toSet()));

        return Backpack.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .createdBy(dto.getCreatedBy())
                .ownerUid(dto.getOwnerUid())
                .worldUid(dto.getWorldUid())
                .size(dto.getSize())
                .location(dto.getLocation())
                .capacity(dto.getCapacity())
                .shape(dto.getShape())
                .collider(dto.getCollider())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .items(items)
                .build();
    }

    private LittleChest lchestDtoToLchest(LittleChestDto dto) {
        Set<Item> items = dto.getStacks() == null || dto.getStacks().isEmpty() ? null
                : itemsMapper.toEntities(dto.getStacks().stream().map(ItemStack::getItemDto).collect(Collectors.toSet()));

        return LittleChest.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .createdBy(dto.getCreatedBy())
                .ownerUid(dto.getOwnerUid())
                .worldUid(dto.getWorldUid())
                .size(dto.getSize())
                .location(dto.getLocation())
                .capacity(dto.getCapacity())
                .shape(dto.getShape())
                .collider(dto.getCollider())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .items(items)
                .build();
    }

    private LittleChestDto lchestToLchestDto(LittleChest entity) {
        LittleChestDto result = LittleChestDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .ownerUid(entity.getOwnerUid())
                .location(entity.getLocation())
                .shape(entity.getShape())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .worldUid(entity.getWorldUid())
                .capacity(entity.getCapacity())
                .build();

        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemDto dto : itemsMapper.toDtos(entity.getItems())) {
                int count = storageToItemsService.findCountByItemUidAndStorageUid(dto.getUid(), result.getUid());
                // добавляем полные стаки предметов:
                while (count > dto.getStackCount()) {
                    stacks.add(new ItemStack()
                            .itemUid(dto.getUid())
                            .itemDto(dto)
                            .itemName(dto.getName())
                            .count(dto.getStackCount()));
                    count -= dto.getStackCount();
                }
                // добавляем последний (единственный), неполный стак:
                stacks.add(new ItemStack()
                        .itemUid(dto.getUid())
                        .itemDto(dto)
                        .itemName(dto.getName())
                        .count(count));
            }
            result.setStacks(stacks);
        }

        return result;
    }

    public Set<Storage> toEntities(Set<StorageDto> dtos) {
        return dtos.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public Set<StorageDto> toDtos(Set<Storage> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toSet());
    }
}
