package game.freya.mappers;

import game.freya.dto.roots.WorldDto;
import game.freya.entities.roots.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Component
public class WorldMapper {
    private final EnvironmentMapper environmentMapper;

    public WorldDto toDto(World entity) {
        if (entity == null) {
            return null;
        }

        return WorldDto.builder()
                .uid(entity.getUid())
                .createdBy(entity.getCreatedBy())
                .name(entity.getName())
                .level(entity.getLevel())
                .size(new Dimension(entity.getSize().width, entity.getSize().height))
                .isNetAvailable(entity.isNetAvailable())
                .passwordHash(entity.getPasswordHash())
                .createdDate(entity.getCreatedDate())
                .isLocalWorld(entity.isLocalWorld())
                .networkAddress(entity.getNetworkAddress())
                .environments(environmentMapper.toDto(entity.getEnvironments()))
                .cacheKey(entity.getCacheKey())
                .collider(entity.getCollider())
                .isVisible(entity.isVisible())
//                .hasCollision(entity.isHasCollision()) // динамика через collider
                .build();
    }

    public World toEntity(WorldDto dto) {
        if (dto == null) {
            return null;
        }

        return World.builder()
                .uid(dto.getUid())
                .createdBy(dto.getCreatedBy())
                .name(dto.getName())
                .isNetAvailable(dto.isNetAvailable())
                .passwordHash(dto.getPasswordHash())
                .size(dto.getSize())
                .level(dto.getLevel())
                .createdDate(dto.getCreatedDate())
                .isLocalWorld(dto.isLocalWorld())
                .networkAddress(dto.getNetworkAddress())
                .environments(environmentMapper.toEntity(dto.getEnvironments()))
                .cacheKey(dto.getCacheKey())
                .collider(dto.getCollider())
                .isVisible(dto.isVisible())
//                .hasCollision(dto.hasCollision()) // динамика через collider
                .build();
    }

    public List<WorldDto> toDto(List<World> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
