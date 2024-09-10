package game.freya.mappers;

import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.ItemStack;
import game.freya.dto.roots.StorageDto;
import game.freya.entities.roots.Storage;
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

        return Storage.builder()
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
                .items(dto.getStacks() == null ? null
                        : itemsMapper.toEntities(dto.getStacks().stream().map(ItemStack::getItemDto).collect(Collectors.toSet())))
                .build();
    }

    public StorageDto toDto(Storage entity) {
        if (entity == null) {
            return null;
        }

        StorageDto result = StorageDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .ownerUid(entity.getOwnerUid())
                .location(entity.getLocation())
                .shape(entity.getShape())
                .size(entity.getSize())
                .collider(entity.getCollider())
//                .hasCollision(entity.isHasCollision()) // динамика через collider
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

    public Set<Storage> toEntities(Set<StorageDto> heroes) {
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public Set<StorageDto> toDtos(Set<Storage> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toSet());
    }
}
