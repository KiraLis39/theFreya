package game.freya.mappers;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDTO;
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
@HeroDataBuilder
public final class HeroMapper {

    public Hero toEntity(HeroDTO dto) {
        if (dto == null) {
            return null;
        }

        return Hero.builder()
                .uid(dto.getHeroUid())
                .heroName(dto.getHeroName())
                .baseColor(dto.getBaseColor())
                .secondColor(dto.getSecondColor())
                .corpusType(dto.getCorpusType())
                .peripheralType(dto.getPeripheralType())
                .peripheralSize(dto.getPeripheralSize())
                .level(dto.getLevel())
                .heroType(dto.getHeroType())
                .power(dto.getPower())
                .experience(dto.getExperience())
                .curHealth(dto.getHealth())
                .maxHealth(dto.getMaxHealth())
                .curOil(dto.getOil())
                .maxOil(dto.getMaxOil())
                .speed(dto.getSpeed())
                .location(dto.getLocation())
                .hurtLevel(dto.getHurtLevel())
                .createDate(dto.getCreateDate())
                .worldUid(dto.getWorldUid())
                .ownerUid(dto.getAuthor())
                .inGameTime(dto.getInGameTime())
                .lastPlayDate(dto.getLastPlayDate())
                .inventory(dto.getInventory())
                .buffs(dto.getBuffs())
                .build();
    }

    public HeroDTO toDto(Hero entity) {
        if (entity == null) {
            return null;
        }

        return HeroDTO.builder()
                .baseColor(entity.getBaseColor())
                .secondColor(entity.getSecondColor())
                .corpusType(entity.getCorpusType())
                .peripheralType(entity.getPeripheralType())
                .peripheralSize(entity.getPeripheralSize())
                .heroType(entity.getHeroType())
                .worldUid(entity.getWorldUid())
                .inGameTime(entity.getInGameTime())
                .lastPlayDate(entity.getLastPlayDate())
                .buffs(entity.getBuffs())
                .inventory(entity.getInventory())
                .ownerUid(entity.getOwnerUid())
                .heroName(entity.getHeroName())
                .level(entity.getLevel())
                .power(entity.getPower())
                .experience(entity.getExperience())
                .curHealth(entity.getCurHealth())
                .maxHealth(entity.getMaxHealth())
                .curOil(entity.getCurOil())
                .maxOil(entity.getMaxOil())
                .speed(entity.getSpeed())
                .location(entity.getLocation())
                .createDate(entity.getCreateDate())
                .build();
    }

    public Set<Hero> toEntities(Set<HeroDTO> heroes) {
        if (heroes == null) {
            return Collections.emptySet();
        }
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public List<HeroDTO> toDtos(List<Hero> heroes) {
        if (heroes == null) {
            return Collections.emptyList();
        }
        return heroes.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ClientDataDTO heroToCli(HeroDTO hero, PlayerDTO currentPlayer) {
        return hero == null ? null : ClientDataDTO.builder()
                .dataType(NetDataType.EVENT)
                .dataEvent(NetDataEvent.HERO_REGISTER)
                .content(EventHeroRegister.builder()
                        .playerUid(currentPlayer.getUid())
                        .playerName(currentPlayer.getNickName())

                        .heroUid(hero.getHeroUid())
                        .heroName(hero.getHeroName())
                        .heroType(hero.getHeroType())

                        .baseColor(hero.getBaseColor())
                        .secondColor(hero.getSecondColor())
                        .corpusType(hero.getCorpusType())
                        .periferiaType(hero.getPeripheralType())
                        .periferiaSize(hero.getPeripheralSize())

                        .level(hero.getLevel())
                        .hp(hero.getHealth())
                        .maxHp(hero.getMaxHealth())
                        .oil(hero.getOil())
                        .maxOil(hero.getMaxOil())
                        .speed(hero.getSpeed())
                        .vector(hero.getVector())
                        .positionX(hero.getLocation().x)
                        .positionY(hero.getLocation().y)

                        .worldUid(hero.getWorldUid())

                        // временно отключил:
//                    .createDate(hero.getCreateDate())
//                    .power(getCurrentHeroPower())
//                    .experience(getCurrentHeroExperience())
//                    .buffsJson(getCurrentHeroBuffsJson())
//                    .inventoryJson(getCurrentHeroInventoryJson())
//                    .inGameTime(readed.inGameTime())
                        .build())
                .build();
    }

    public HeroDTO cliToHero(ClientDataDTO cli) {
        if (cli == null) {
            return null;
        }

        EventHeroRegister heroRegister = (EventHeroRegister) cli.content();
        HeroDTO result = HeroDTO.builder()
                .heroType(heroRegister.heroType())

                .baseColor(heroRegister.baseColor())
                .secondColor(heroRegister.secondColor())
                .corpusType(heroRegister.corpusType())
                .peripheralType(heroRegister.periferiaType())
                .peripheralSize(heroRegister.periferiaSize())

                .worldUid(heroRegister.worldUid())

                // временно отключил:
//                .power(heroRegister.power())
//                .experience(heroRegister.experience())
//                .inGameTime(heroRegister.inGameTime())
//                .lastPlayDate(heroRegister.lastPlayDate())
//                .createDate(heroRegister.createDate())

                .build();

        // временно отключил:
//            try {
//                Backpack bPack = mapper.readValue(readed.inventoryJson(), Backpack.class);
//                hero.setInventory(bPack);
//            } catch (Exception e) {
//                log.error("Проблема при парсинге инвентаря Героя {}: {}", readed.heroName(), ExceptionUtils.getFullExceptionMessage(e));
//            }
//            try {
//                for (Buff buff : mapper.readValue(readed.buffsJson(), Buff[].class)) {
//                    hero.addBuff(buff);
//                }
//            } catch (Exception e) {
//                log.error("Проблема при парсинге бафов Героя {}: {}", readed.heroName(), ExceptionUtils.getFullExceptionMessage(e));
//            }

        result.setAuthor(heroRegister.playerUid());
        result.setHeroUid(heroRegister.heroUid());
        result.setHeroName(heroRegister.heroName());
        result.setSpeed(heroRegister.speed());
        result.setLocation(heroRegister.positionX(), heroRegister.positionY());
        result.setHealth(heroRegister.hp());
        result.setMaxHealth(heroRegister.maxHp());
        result.setOil(heroRegister.oil());
        result.setMaxOil(heroRegister.maxOil());
        result.setLevel(heroRegister.level());

        return result;
    }
}
