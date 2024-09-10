package game.freya.mappers;

import game.freya.dto.BackpackDto;
import game.freya.dto.PlayerDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.Backpack;
import game.freya.entities.roots.Character;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroRegister;
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
public final class CharMapper {
    private final BuffMapper buffMapper;
    private final StorageMapper storageMapper;

    public Character toEntity(CharacterDto dto) {
        if (dto == null) {
            return null;
        }

        return Character.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .baseColor(dto.getBaseColor())
                .secondColor(dto.getSecondColor())
                .corpusType(dto.getCorpusType())
                .peripheralType(dto.getPeripheralType())
                .peripheralSize(dto.getPeripheralSize())
                .level(dto.getLevel())
                .heroType(dto.getHeroType())
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
                .lastPlayDate(dto.getLastPlayDate())
                .inventory((Backpack) storageMapper.toEntity(dto.getInventory()))
                .buffs(buffMapper.toEntity(dto.getBuffs()))
                .build();
    }

    public CharacterDto toDto(Character entity) {
        if (entity == null) {
            return null;
        }

        return CharacterDto.builder()
                .baseColor(entity.getBaseColor())
                .secondColor(entity.getSecondColor())
                .corpusType(entity.getCorpusType())
                .peripheralType(entity.getPeripheralType())
                .peripheralSize(entity.getPeripheralSize())
                .heroType(entity.getHeroType())
                .worldUid(entity.getWorldUid())
                .inGameTime(entity.getInGameTime())
                .lastPlayDate(entity.getLastPlayDate())
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

    public Set<Character> toEntities(Set<CharacterDto> heroes) {
        if (heroes == null) {
            return Collections.emptySet();
        }
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public List<CharacterDto> toDto(List<Character> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ClientDataDto heroToCli(CharacterDto hero, PlayerDto currentPlayer) {
        if (hero == null) {
            return null;
        }

        return ClientDataDto.builder()
                .dataType(NetDataType.EVENT)
                .dataEvent(NetDataEvent.HERO_REGISTER)
                .content(EventHeroRegister.builder()
                        .ownerUid(currentPlayer.getUid())
                        .playerName(currentPlayer.getNickName())
                        .heroUid(hero.getUid())
                        .heroName(hero.getName())
                        .heroType(hero.getHeroType())
                        .baseColor(hero.getBaseColor())
                        .secondColor(hero.getSecondColor())
                        .corpusType(hero.getCorpusType())
                        .peripheryType(hero.getPeripheralType())
                        .peripherySize(hero.getPeripheralSize())
                        .level(hero.getLevel())
                        .hp(hero.getHealth())
                        .maxHp(hero.getMaxHealth())
                        .oil(hero.getOil())
                        .maxOil(hero.getMaxOil())
                        .speed(hero.getSpeed())
                        .vector(hero.getVector())
                        .positionX(hero.getLocation().x)
                        .positionY(hero.getLocation().y)
                        .worldUid(hero.getWorldUid()).build())
                .power(hero.getPower())
                .experience(hero.getExperience())
//                .buffs(hero.getBuffs())
//                .inventory(hero.getInventory())
//                .inGameTime(hero.inGameTime())
                .build();
    }

    public CharacterDto cliToHero(ClientDataDto cli) {
        if (cli == null || cli.content() == null) {
            return null;
        }

        EventHeroRegister heroRegister = (EventHeroRegister) cli.content();
        return CharacterDto.builder()
                .heroType(heroRegister.heroType())

                .baseColor(heroRegister.baseColor())
                .secondColor(heroRegister.secondColor())
                .corpusType(heroRegister.corpusType())
                .peripheralType(heroRegister.peripheryType())
                .peripheralSize(heroRegister.peripherySize())

                .worldUid(heroRegister.worldUid())

//                .power(heroRegister.power())
//                .experience(heroRegister.experience())
//                .inGameTime(heroRegister.inGameTime())
//                .lastPlayDate(heroRegister.lastPlayDate())
//                .createdDate(heroRegister.createdDate())

//                .inventory(heroRegister.getInventory())
//                .buffs(heroRegister.getBuffs())

//                .setAuthor(heroRegister.playerUid())
//                .setUid(heroRegister.heroUid())
//                .setName(heroRegister.heroName())
//                .setSpeed(heroRegister.speed())
//                .setLocation(heroRegister.positionX(), heroRegister.positionY())
//                .setHealth(heroRegister.hp())
//                .setMaxHealth(heroRegister.maxHp())
//                .setOil(heroRegister.oil())
//                .setMaxOil(heroRegister.maxOil())
//                .setLevel(heroRegister.level())
                .build();
    }
}
