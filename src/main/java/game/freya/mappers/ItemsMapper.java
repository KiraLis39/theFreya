package game.freya.mappers;

import game.freya.dto.roots.ItemDto;
import game.freya.entities.roots.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ItemsMapper {
    private final StorageMapper storageMapper;

    public Item toEntity(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        return Item.builder()
                .uid(dto.getUid())
                .ownerUid(dto.getOwnerUid())
                .createdBy(dto.getCreatedBy())
                .worldUid(dto.getWorldUid())
                .name(dto.getName())
                .size(dto.getSize())
                .collider(dto.getCollider())
                .location(dto.getLocation())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                //.storages(storageMapper.toEntities(dto.getStorages()))
                .stackCount(dto.getStackCount())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .build();
    }

    public ItemDto toDto(Item entity) {
        if (entity == null) {
            return null;
        }

        return ItemDto.builder()
                .uid(entity.getUid())
                .ownerUid(entity.getOwnerUid())
                .createdBy(entity.getCreatedBy())
                .worldUid(entity.getWorldUid())
                .name(entity.getName())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .location(entity.getLocation())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                //.storages(storageMapper.toDtos(entity.getStorages()))
                .stackCount(entity.getStackCount())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    public Set<Item> toEntities(Set<ItemDto> heroes) {
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public Set<ItemDto> toDtos(Set<Item> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toSet());
    }
}
