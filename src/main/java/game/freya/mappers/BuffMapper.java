package game.freya.mappers;

import game.freya.dto.roots.BuffDto;
import game.freya.entities.roots.Buff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BuffMapper {
    public BuffDto toDto(Buff entity) {
        if (entity == null) {
            return null;
        }

        return new BuffDto()
                .uid(entity.uid())
                .name(entity.name());
    }

    public Buff toEntity(BuffDto dto) {
        if (dto == null) {
            return null;
        }

        return new Buff()
                .uid(dto.uid())
                .name(dto.name());
    }

    public List<BuffDto> toDto(List<Buff> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<Buff> toEntity(List<BuffDto> entities) {
        return entities.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
