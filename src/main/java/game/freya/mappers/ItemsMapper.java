package game.freya.mappers;

import game.freya.dto.roots.ItemDto;
import game.freya.entities.roots.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ItemsMapper {
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
                .hasCollision(dto.isHasCollision())
                .cacheKey(dto.getCacheKey())
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
                .hasCollision(entity.isHasCollision())
                .cacheKey(entity.getCacheKey())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    public List<Item> toEntities(List<ItemDto> heroes) {
        if (heroes == null) {
            return Collections.emptyList();
        }
        return heroes.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<ItemDto> toDto(List<Item> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toList());
    }
}
