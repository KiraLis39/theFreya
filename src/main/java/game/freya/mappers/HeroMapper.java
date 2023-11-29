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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public final class HeroMapper {
    private final ObjectMapper mapper;

    public Hero toEntity(HeroDTO dto) {
        if (dto == null) {
            return null;
        }
        Hero result = Hero.builder()
                .uid(dto.getUid())
                .heroName(dto.getHeroName())
                .level(dto.getLevel())
                .type(dto.getType())
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
                .uid(entity.getUid())
                .heroName(entity.getHeroName())
                .level(entity.getLevel())
                .type(entity.getType())
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
}
