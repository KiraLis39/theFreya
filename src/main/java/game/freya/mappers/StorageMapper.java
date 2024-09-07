package game.freya.mappers;

import game.freya.dto.roots.StorageDto;
import game.freya.entities.roots.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class StorageMapper {
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
                .build();
    }

    public Set<Storage> toEntities(Set<StorageDto> heroes) {
        if (heroes == null) {
            return Collections.emptySet();
        }
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public List<StorageDto> toDto(List<Storage> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toList());
    }
}
