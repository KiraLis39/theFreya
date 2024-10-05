package game.freya.mappers;

import game.freya.dto.roots.WorldDto;
import game.freya.entities.roots.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class WorldMapper {
    private final EnvironmentMapper environmentMapper;
    private final CharacterMapper characterMapper;

    public WorldDto toDto(World entity) {
        if (entity == null) {
            return null;
        }

        return WorldDto.builder()
                .uid(entity.getUid())
                .createdBy(entity.getCreatedBy())
                .name(entity.getName())
                .hardnessLevel(entity.getHardnessLevel())
                .size(new Dimension(entity.getSize().width, entity.getSize().height))
                .isNetAvailable(entity.isNetAvailable())
                .password(entity.getPassword()) // bcrypt
                .createdDate(entity.getCreatedDate())
                .createdBy(entity.getCreatedBy())
                .isLocal(entity.isLocal())
                .address(entity.getAddress())
                .cacheKey(entity.getCacheKey())
                .heroes(characterMapper.toDto(entity.getHeroes()))
                .environments(environmentMapper.toDto(entity.getEnvironments()))
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
                .password(dto.getPassword()) // bcrypt
                .size(dto.getSize())
                .hardnessLevel(dto.getHardnessLevel())
                .createdDate(dto.getCreatedDate())
                .createdBy(dto.getCreatedBy())
                .isLocal(dto.isLocal())
                .address(dto.getAddress())
                .cacheKey(dto.getCacheKey())
                .heroes(characterMapper.toEntity(dto.getHeroes()))
                .environments(environmentMapper.toEntity(dto.getEnvironments()))
                .build();
    }

    public List<WorldDto> toDto(List<World> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
