package game.freya.mappers;

import game.freya.dto.BackpackDto;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.Backpack;
import game.freya.entities.PlayCharacter;
import game.freya.entities.roots.prototypes.Character;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public final class CharacterMapper {
    private final BuffMapper buffMapper;
    private final StorageMapper storageMapper;

    public Character toEntity(CharacterDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case PlayCharacterDto p -> entityToCharacter(p);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    public CharacterDto toDto(Character entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case PlayCharacter p -> playCharacterToPlayDto(p);
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    private PlayCharacter entityToCharacter(PlayCharacterDto dto) {
        return PlayCharacter.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .type(dto.getType())
                .baseColor(dto.getBaseColor())
                .secondColor(dto.getSecondColor())
                .corpusType(dto.getCorpusType())
                .peripheralType(dto.getPeripheralType())
                .peripheralSize(dto.getPeripheralSize())
                .level(dto.getLevel())
                .power(dto.getPower())
                .experience(dto.getExperience())
                .health(dto.getHealth())
                .maxHealth(dto.getMaxHealth())
                .oil(dto.getOil())
                .maxOil(dto.getMaxOil())
                .speed(dto.getSpeed())
                .location(dto.getLocation())
                .hurtLevel(dto.getHurtLevel())
                .createdDate(dto.getCreatedDate())
                .worldUid(dto.getWorldUid())
                .ownerUid(dto.getOwnerUid())
                .inGameTime(dto.getInGameTime())
                .modifyDate(dto.getModifyDate())
                .inventory((Backpack) storageMapper.toEntity(dto.getInventory()))
                .buffs(buffMapper.toEntity(dto.getBuffs()))
                .build();
    }

    private PlayCharacterDto playCharacterToPlayDto(PlayCharacter entity) {
        return PlayCharacterDto.builder()
                .baseColor(entity.getBaseColor())
                .secondColor(entity.getSecondColor())
                .corpusType(entity.getCorpusType())
                .peripheralType(entity.getPeripheralType())
                .peripheralSize(entity.getPeripheralSize())
                .type(entity.getType())
                .worldUid(entity.getWorldUid())
                .inGameTime(entity.getInGameTime())
                .modifyDate(entity.getModifyDate())
                .buffs(buffMapper.toDto(entity.getBuffs()))
                .inventory((BackpackDto) storageMapper.toDto(entity.getInventory()))
                .ownerUid(entity.getOwnerUid())
                .name(entity.getName())
                .level(entity.getLevel())
                .power(entity.getPower())
                .experience(entity.getExperience())
                .health(entity.getHealth())
                .maxHealth(entity.getMaxHealth())
                .oil(entity.getOil())
                .maxOil(entity.getMaxOil())
                .speed(entity.getSpeed())
                .location(entity.getLocation())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    public Set<CharacterDto> toDto(Set<? extends Character> characters) {
        return characters.stream().map(this::toDto).collect(Collectors.toSet());
    }

    public Set<Character> toEntity(Set<CharacterDto> dtos) {
        return dtos.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public List<CharacterDto> toDtos(List<? extends Character> characters) {
        return characters.stream().map(this::toDto).collect(Collectors.toList());
    }
}
