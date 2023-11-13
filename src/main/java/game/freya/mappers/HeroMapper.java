package game.freya.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.items.containers.Backpack;
import game.freya.items.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class HeroMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    private HeroMapper() {
    }

    public Hero toEntity(HeroDTO dto) {
        if (dto == null) {
            return null;
        }
        Hero result = Hero.builder()
                .uid(dto.getUid())
                .heroName(dto.getHeroName())
                .level(dto.getLevel())
                .currentAttackPower(dto.getCurrentAttackPower())
                .experience(dto.getExperience())
                .health(dto.getHealth())
                .maxHealth(dto.getMaxHealth())
                .speed(dto.getSpeed())
                .positionX(dto.getPosition().x)
                .positionY(dto.getPosition().y)
                .hurtLevel(dto.getHurtLevel())
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
                .uid(entity.getUid())
                .heroName(entity.getHeroName())
                .level(entity.getLevel())
                .currentAttackPower(entity.getCurrentAttackPower())
                .experience(entity.getExperience())
                .health(entity.getHealth())
                .maxHealth(entity.getMaxHealth())
                .speed(entity.getSpeed())
                .position(new Point2D.Double(entity.getPositionX(), entity.getPositionY()))
                .hurtLevel(entity.getHurtLevel())
                .build();

        try {
            result.getBuffs().clear();
            for (Buff buff : mapper.readValue(entity.getBuffsJson(), Buff[].class)) {
                result.addBuff(buff);
            }
            result.setInventory(mapper.readValue(entity.getInventoryJson(), Backpack.class));
        } catch (JsonProcessingException e) {
            log.error("Err in hero mapper: {}", ExceptionUtils.getFullExceptionMessage(e));
            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERR);
        }

        return result;
    }

    public Set<Hero> toEntities(Map<String, HeroDTO> heroes) {
        return heroes.values().stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
