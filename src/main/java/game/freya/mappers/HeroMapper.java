package game.freya.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.utils.ExceptionUtils;
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
    private final ObjectMapper mapper;

    public Hero toEntity(HeroDTO dto) {
        if (dto == null) {
            return null;
        }
        Hero result = Hero.builder()
                .uid(dto.getCharacterUid())
                .heroName(dto.getCharacterName())
                .baseColor(dto.getBaseColor())
                .secondColor(dto.getSecondColor())
                .corpusType(dto.getCorpusType())
                .periferiaType(dto.getPeriferiaType())
                .periferiaSize(dto.getPeriferiaSize())
                .level(dto.getLevel())
                .type(dto.getHeroType())
                .power(dto.getPower())
                .experience(dto.getExperience())
                .curHealth(dto.getHealth())
                .maxHealth(dto.getMaxHealth())
                .curOil(dto.getOil())
                .maxOil(dto.getMaxOil())
                .speed(dto.getSpeed())
                .positionX(dto.getLocation().x)
                .positionY(dto.getLocation().y)
                .hurtLevel(dto.getHurtLevel())
                .createDate(dto.getCreateDate())
                .worldUid(dto.getWorldUid())
                .ownerUid(dto.getAuthor())
                .inGameTime(dto.getInGameTime())
                .lastPlayDate(dto.getLastPlayDate())
                .build();

        try {
            result.setInventoryJson(mapper.writeValueAsString(dto.getInventory()));
            result.setBuffsJson(mapper.writeValueAsString(dto.getBuffs()));
        } catch (JsonProcessingException e) {
            log.error("Err in hero mapper: {}", ExceptionUtils.getFullExceptionMessage(e));
            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERR);
        }

        return result;
    }

    public HeroDTO toDto(Hero entity) {
        if (entity == null) {
            return null;
        }

        HeroDTO result = new HeroDTO();
        result.setPeriferiaType(entity.getPeriferiaType());
        result.setPeriferiaSize(entity.getPeriferiaSize());
        result.setCorpusType(entity.getCorpusType());
        result.setHeroType(entity.getType());
        result.setWorldUid(entity.getWorldUid());
        result.setInGameTime(entity.getInGameTime());
        result.setBaseColor(entity.getBaseColor());
        result.setSecondColor(entity.getSecondColor());
        result.setLastPlayDate(entity.getLastPlayDate());

        result.setCharacterUid(entity.getUid());
        result.setCharacterName(entity.getHeroName());
        result.setLevel(entity.getLevel());
        result.setPower(entity.getPower());
        result.setExperience(entity.getExperience());
        result.setHealth(entity.getCurHealth());
        result.setMaxHealth(entity.getMaxHealth());
        result.setOil(entity.getCurOil());
        result.setMaxOil(entity.getMaxOil());
        result.setSpeed(entity.getSpeed());
        result.setLocation(entity.getPositionX(), entity.getPositionY());
        result.setCreateDate(entity.getCreateDate());
        result.setOwnerUid(entity.getOwnerUid());

//        try {
//            result.getBuffs().clear();
//            for (Buff buff : mapper.readValue(entity.getBuffsJson(), Buff[].class)) {
//                result.addBuff(buff);
//            }
//            result.setInventory(mapper.readValue(entity.getInventoryJson(), Backpack.class));
//        } catch (Exception e) {
//            log.error("Err in hero mapper: {}", ExceptionUtils.getFullExceptionMessage(e));
//            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERR);
//        }

        return result;
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

                        .heroUid(hero.getCharacterUid())
                        .heroName(hero.getCharacterName())
                        .heroType(hero.getHeroType())

                        .baseColor(hero.getBaseColor())
                        .secondColor(hero.getSecondColor())
                        .corpusType(hero.getCorpusType())
                        .periferiaType(hero.getPeriferiaType())
                        .periferiaSize(hero.getPeriferiaSize())

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
        HeroDTO result = new HeroDTO();
        result.setHeroType(heroRegister.heroType());
        result.setBaseColor(heroRegister.baseColor());
        result.setSecondColor(heroRegister.secondColor());
        result.setCorpusType(heroRegister.corpusType());
        result.setPeriferiaType(heroRegister.periferiaType());
        result.setPeriferiaSize(heroRegister.periferiaSize());
        result.setWorldUid(heroRegister.worldUid());

        result.setOwnerUid(heroRegister.playerUid());
        result.setCharacterUid(heroRegister.heroUid());
        result.setCharacterName(heroRegister.heroName());
        result.setSpeed(heroRegister.speed());
        result.setLocation(heroRegister.positionX(), heroRegister.positionY());
        result.setHealth(heroRegister.hp());
        result.setMaxHealth(heroRegister.maxHp());
        result.setOil(heroRegister.oil());
        result.setMaxOil(heroRegister.maxOil());
        result.setLevel(heroRegister.level());

        // временно отключил:
//      result.power(heroRegister.power())
//      result.experience(heroRegister.experience())
//      result.inGameTime(heroRegister.inGameTime())
//      result.lastPlayDate(heroRegister.lastPlayDate())
//      result.createDate(heroRegister.createDate())

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

        return result;
    }
}
