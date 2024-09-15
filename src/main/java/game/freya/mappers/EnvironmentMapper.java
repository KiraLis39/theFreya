package game.freya.mappers;

import game.freya.dto.MockEnvironmentWithStorageDto;
import game.freya.dto.roots.EnvironmentDto;
import game.freya.entities.MockEnvironmentWithStorage;
import game.freya.entities.roots.prototypes.Environment;
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

        return switch (entity) {
            case MockEnvironmentWithStorage m -> mockToDto(m);
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    public Environment toEntity(EnvironmentDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case MockEnvironmentWithStorageDto m -> mockToEntity(m);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    private MockEnvironmentWithStorage mockToEntity(MockEnvironmentWithStorageDto dto) {
        return MockEnvironmentWithStorage.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .createdBy(dto.getCreatedBy())
                .ownerUid(dto.getOwnerUid())
                .worldUid(dto.getWorldUid())
                .collider(dto.getCollider())
                .size(dto.getSize())
                .location(dto.getLocation())
                .shape(dto.getShape())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .build();
    }

    private MockEnvironmentWithStorageDto mockToDto(MockEnvironmentWithStorage entity) {
        return MockEnvironmentWithStorageDto.builder()
                .uid(entity.getUid())
                .name(entity.getName())
                .createdBy(entity.getCreatedBy())
                .ownerUid(entity.getOwnerUid())
                .worldUid(entity.getWorldUid())
                .collider(entity.getCollider())
                .size(entity.getSize())
                .location(entity.getLocation())
                .shape(entity.getShape())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    public Set<EnvironmentDto> toDto(Set<Environment> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toSet());
    }

    public Set<Environment> toEntity(Set<EnvironmentDto> entities) {
        return entities.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
