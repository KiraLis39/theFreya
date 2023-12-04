package game.freya.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.enums.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.items.containers.Backpack;
import game.freya.items.logic.Buff;
import game.freya.net.data.ClientDataDTO;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
                .uid(dto.getHeroUid())
                .heroName(dto.getHeroName())
                .baseColor(dto.getBaseColor())
                .secondColor(dto.getSecondColor())
                .corpusType(dto.getCorpusType())
                .periferiaType(dto.getPeriferiaType())
                .periferiaSize(dto.getPeriferiaSize())
                .level(dto.getLevel())
                .type(dto.getHeroType())
                .power(dto.getPower())
                .experience(dto.getExperience())
                .curHealth(dto.getCurHealth())
                .maxHealth(dto.getMaxHealth())
                .curOil(dto.getCurOil())
                .maxOil(dto.getMaxOil())
                .speed(dto.getSpeed())
                .positionX(dto.getPosition().x)
                .positionY(dto.getPosition().y)
                .hurtLevel(dto.getHurtLevel())
                .createDate(dto.getCreateDate())
                .worldUid(dto.getWorldUid())
                .ownerUid(dto.getOwnerUid())
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

        HeroDTO result = HeroDTO.builder()
                .heroUid(entity.getUid())
                .heroName(entity.getHeroName())
                .baseColor(entity.getBaseColor())
                .secondColor(entity.getSecondColor())
                .corpusType(entity.getCorpusType())
                .periferiaType(entity.getPeriferiaType())
                .periferiaSize(entity.getPeriferiaSize())
                .level(entity.getLevel())
                .heroType(entity.getType())
                .power(entity.getPower())
                .experience(entity.getExperience())
                .curHealth(entity.getCurHealth())
                .maxHealth(entity.getMaxHealth())
                .curOil(entity.getCurOil())
                .maxOil(entity.getMaxOil())
                .speed(entity.getSpeed())
                .position(new Point2D.Double(entity.getPositionX(), entity.getPositionY()))
                .hurtLevel(entity.getHurtLevel())
                .createDate(entity.getCreateDate())
                .worldUid(entity.getWorldUid())
                .ownerUid(entity.getOwnerUid())
                .inGameTime(entity.getInGameTime())
                .lastPlayDate(entity.getLastPlayDate())
                .build();

        try {
            result.getBuffs().clear();
            for (Buff buff : mapper.readValue(entity.getBuffsJson(), Buff[].class)) {
                result.addBuff(buff);
            }
            result.setInventory(mapper.readValue(entity.getInventoryJson(), Backpack.class));
        } catch (Exception e) {
            log.error("Err in hero mapper: {}", ExceptionUtils.getFullExceptionMessage(e));
            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERR);
        }

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

    public ClientDataDTO heroToCli(HeroDTO hero, PlayerDTO currentPlayer, NetDataType dataType) {
        if (hero == null) {
            return null;
        }
        ClientDataDTO result = null;
        if (dataType.equals(NetDataType.HERO_REQUEST)) {
            result = ClientDataDTO.builder()
                    .id(UUID.randomUUID())
                    .type(dataType)

                    .playerUid(currentPlayer.getUid())
                    .playerName(currentPlayer.getNickName())

                    .heroUuid(hero.getHeroUid())
                    .heroName(hero.getHeroName())
                    .heroType(hero.getHeroType())

                    .baseColor(hero.getBaseColor())
                    .secondColor(hero.getSecondColor())
                    .corpusType(hero.getCorpusType())
                    .periferiaType(hero.getPeriferiaType())
                    .periferiaSize(hero.getPeriferiaSize())

                    .level(hero.getLevel())
                    .hp(hero.getCurHealth())
                    .maxHp(hero.getMaxHealth())
                    .oil(hero.getCurOil())
                    .maxOil(hero.getMaxOil())
                    .speed(hero.getSpeed())
                    .vector(hero.getVector())
                    .positionX(hero.getPosition().x)
                    .positionY(hero.getPosition().y)

                    // временно отключил:
//                    .createDate(hero.getCreateDate())
//                    .power(getCurrentHeroPower())
//                    .experience(getCurrentHeroExperience())
//                    .buffsJson(getCurrentHeroBuffsJson())
//                    .inventoryJson(getCurrentHeroInventoryJson())
//                    .inGameTime(readed.inGameTime())

                    .worldUid(hero.getWorldUid())
                    .build();
        } else if (dataType.equals(NetDataType.SYNC)) {
            result = ClientDataDTO.builder()
                    .id(UUID.randomUUID())
                    .type(dataType)

                    .playerUid(currentPlayer.getUid())
                    .playerName(currentPlayer.getNickName())

                    .heroUuid(hero.getHeroUid())
                    .heroName(hero.getHeroName())
                    .playerName(currentPlayer.getNickName())
                    .heroType(hero.getHeroType())

                    .hp(hero.getCurHealth())
                    .maxHp(hero.getMaxHealth())
                    .oil(hero.getCurOil())
                    .maxOil(hero.getMaxOil())

                    .level(hero.getLevel())
                    .speed(hero.getSpeed())
                    .vector(hero.getVector())
                    .positionX(hero.getPosition().x)
                    .positionY(hero.getPosition().y)

                    // временно отключил:
//                    .createDate(hero.getCreateDate())
//                    .power(getCurrentHeroPower())
//                    .experience(getCurrentHeroExperience())
//                    .buffsJson(getCurrentHeroBuffsJson())
//                    .inventoryJson(getCurrentHeroInventoryJson())
//                    .inGameTime(readed.inGameTime())

                    .build();
        }
        return result;
    }

    public HeroDTO cliToHero(ClientDataDTO cli) {
        return cli == null ? null : HeroDTO.builder()
                .ownerUid(cli.playerUid())

                .heroUid(cli.heroUuid())
                .heroName(cli.heroName())
                .heroType(cli.heroType())

                .baseColor(cli.baseColor())
                .secondColor(cli.secondColor())
                .corpusType(cli.corpusType())
                .periferiaType(cli.periferiaType())
                .periferiaSize(cli.periferiaSize())

                .speed(cli.speed())
                .position(new Point2D.Double(cli.positionX(), cli.positionY()))
                .curHealth(cli.hp())
                .maxHealth(cli.maxHp())
                .curOil(cli.oil())
                .maxOil(cli.maxOil())
                .worldUid(cli.worldUid())
                .ownerUid(cli.playerUid())
                .level(cli.level())

                // временно отключил:
//                .power(readed.power())
//                .experience(readed.experience())
//                .inGameTime(readed.inGameTime())
//                .lastPlayDate(readed.lastPlayDate())
//                .createDate(readed.createDate())

                .worldUid(cli.worldUid())

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
    }
}
