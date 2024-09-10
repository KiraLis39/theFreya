package game.freya.mappers;

import game.freya.dto.roots.EnvironmentDto;
import game.freya.entities.roots.Environment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class EnvironmentMapper {

    public EnvironmentDto toDto(Environment entity) {
        if (entity == null) {
            return null;
        }

        return EnvironmentDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .createdBy(entity.getCreatedBy())
                .ownerUid(entity.getOwnerUid())
                .worldUid(entity.getWorldUid())
                .shape(entity.getShape())
                .size(entity.getSize())
                .location(entity.getLocation())
                .collider(entity.getCollider())
                .isVisible(entity.isVisible())
//                .hasCollision(entity.isHasCollision()) // динамика через collider
                .cacheKey(entity.getCacheKey())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    public Environment toEntity(EnvironmentDto dto) {
        if (dto == null) {
            return null;
        }

        return Environment.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .createdBy(dto.getCreatedBy())
                .ownerUid(dto.getOwnerUid())
                .worldUid(dto.getWorldUid())
                .shape(dto.getShape())
                .size(dto.getSize())
                .location(dto.getLocation())
                .collider(dto.getCollider())
                .isVisible(dto.isVisible())
//                .hasCollision(dto.hasCollision()) // динамика через collider
                .cacheKey(dto.getCacheKey())
                .build();
    }

    public Set<EnvironmentDto> toDto(Set<Environment> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toSet());
    }

    public Set<Environment> toEntity(Set<EnvironmentDto> entities) {
        return entities.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
