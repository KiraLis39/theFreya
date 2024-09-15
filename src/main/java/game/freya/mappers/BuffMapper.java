package game.freya.mappers;

import game.freya.dto.NegativeBuffDto;
import game.freya.dto.PositiveBuffDto;
import game.freya.dto.roots.BuffDto;
import game.freya.entities.NegativeBuff;
import game.freya.entities.PositiveBuff;
import game.freya.entities.roots.prototypes.Buff;
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

        return switch (entity) {
            case PositiveBuff p -> posToPosDto(p);
            case NegativeBuff n -> negToNegDto(n);
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    private NegativeBuffDto negToNegDto(NegativeBuff entity) {
        return (NegativeBuffDto) new NegativeBuffDto()
                .uid(entity.uid())
                .name(entity.name());
    }

    private PositiveBuffDto posToPosDto(PositiveBuff entity) {
        return (PositiveBuffDto) new PositiveBuffDto()
                .uid(entity.uid())
                .name(entity.name());
    }

    public Buff toEntity(BuffDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case PositiveBuffDto p -> posDtoToPos(p);
            case NegativeBuffDto n -> negDtoToNeg(n);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    private NegativeBuff negDtoToNeg(NegativeBuffDto entity) {
        return (NegativeBuff) new NegativeBuff()
                .uid(entity.uid())
                .name(entity.name());
    }

    private PositiveBuff posDtoToPos(PositiveBuffDto entity) {
        return (PositiveBuff) new PositiveBuff()
                .uid(entity.uid())
                .name(entity.name());
    }

    public List<BuffDto> toDto(List<Buff> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<Buff> toEntity(List<BuffDto> entities) {
        return entities.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
