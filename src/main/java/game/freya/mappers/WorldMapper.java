package game.freya.mappers;

import game.freya.entities.World;
import game.freya.entities.dto.WorldDTO;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class WorldMapper {

    public WorldDTO toDto(World entity) {
        if (entity == null) {
            return null;
        }
        return new WorldDTO(
                entity.getUid(),
                entity.getTitle(),
                entity.getLevel(),
                new Dimension(entity.getDimensionWidth(), entity.getDimensionHeight()),
                entity.getPasswordHash());
    }

    public World toEntity(WorldDTO dto) {
        if (dto == null) {
            return null;
        }
        return World.builder()
                .uid(dto.getUid())
                .title(dto.getTitle())
                .passwordHash(dto.getPasswordHash())
                .dimensionWidth(dto.getDimension().width)
                .dimensionHeight(dto.getDimension().height)
                .level(dto.getLevel())
                .players(PlayerMapper.toEntity(dto.getPlayers()))
                .build();
    }
}
