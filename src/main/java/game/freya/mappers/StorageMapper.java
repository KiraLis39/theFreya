package game.freya.mappers;

import game.freya.dto.roots.StorageDto;
import game.freya.entities.roots.Storage;
import game.freya.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class StorageMapper {
    private final ItemsMapper itemsMapper;
    private final ItemRepository itemRepository;

    public Storage toEntity(StorageDto dto) {
        if (dto == null) {
            return null;
        }

        return Storage.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .ownerUid(dto.getOwnerUid())
                .location(dto.getLocation())
                .shape(dto.getShape())
                .size(dto.getSize())
                .collider(dto.getCollider())
                .hasCollision(dto.hasCollision())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .createdBy(dto.getCreatedBy())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .worldUid(dto.getWorldUid())
                .capacity(dto.getCapacity())
                .items(itemsMapper.toEntities(dto.getItems()))
                .build();
    }

    public StorageDto toDto(Storage entity) {
        if (entity == null) {
            return null;
        }

        return StorageDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .ownerUid(entity.getOwnerUid())
                .location(entity.getLocation())
                .shape(entity.getShape())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .hasCollision(entity.isHasCollision())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .worldUid(entity.getWorldUid())
                .capacity(entity.getCapacity())
                .items(itemsMapper.toDto(entity.getItems()))
                .build();
    }

    public List<Storage> toEntities(List<StorageDto> heroes) {
        if (heroes == null) {
            return Collections.emptyList();
        }
        return heroes.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<StorageDto> toDto(List<Storage> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toList());
    }
}
