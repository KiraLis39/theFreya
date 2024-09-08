package game.freya.mappers;

import game.freya.dto.WorldDto;
import game.freya.entities.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Collections;
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
                .author(entity.getAuthor())
                .title(entity.getTitle())
                .level(entity.getLevel())
                .dimension(new Dimension(entity.getDimensionWidth(), entity.getDimensionHeight()))
                .isNetAvailable(entity.isNetAvailable())
                .passwordHash(entity.getPasswordHash())
                .createDate(entity.getCreatedDate())
                .isLocalWorld(entity.isLocalWorld())
                .networkAddress(entity.getNetworkAddress())
                .environments(environmentMapper.toDto(entity.getEnvironments()))
                .build();
    }

    public List<WorldDto> toDto(List<World> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public World toEntity(WorldDto dto) {
        if (dto == null) {
            return null;
        }
        return World.builder()
                .uid(dto.getUid())
                .author(dto.getAuthor())
                .title(dto.getTitle())
                .isNetAvailable(dto.isNetAvailable())
                .passwordHash(dto.getPasswordHash())
                .dimensionWidth(dto.getDimension().width)
                .dimensionHeight(dto.getDimension().height)
                .level(dto.getLevel())
                .createdDate(dto.getCreateDate())
                .isLocalWorld(dto.isLocalWorld())
                .networkAddress(dto.getNetworkAddress())
                .environments(environmentMapper.toEntity(dto.getEnvironments()))
                .build();
    }
}
