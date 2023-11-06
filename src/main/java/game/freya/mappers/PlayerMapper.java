package game.freya.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import game.freya.items.containers.Backpack;
import game.freya.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlayerMapper {
    private final ObjectMapper MAPPER = new ObjectMapper();

    private PlayerMapper() {
    }

    public Player toEntity(PlayerDTO dto) {
        if (dto == null) {
            return null;
        }
        Player result;
        try {
            result = Player.builder()
                    .uid(dto.getUid())
                    .nickName(dto.getNickName())
                    .email(dto.getEmail())
                    .avatarUrl(dto.getAvatarUrl())
                    .hurtLevel(dto.getHurtLevel())
                    .currentAttackPower(dto.getCurrentAttackPower())
                    .experience(dto.getExperience())
                    .health(dto.getHealth())
                    .level(dto.getLevel())
                    .inventoryJson(MAPPER.writeValueAsString(dto.getInventory()))
                    .buffsJson(MAPPER.writeValueAsString(dto.getBuffs()))
                    .positionX(dto.getPosition().x)
                    .positionY(dto.getPosition().y)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Player mapping exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return null;
        }
        return result;
    }

    public Set<Player> toEntities(Map<String, PlayerDTO> players) {
        return players.values().stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public PlayerDTO toDto(Player entity) {
        if (entity == null) {
            return null;
        }
        PlayerDTO result;
        try {
            result = PlayerDTO.builder()
                    .uid(entity.getUid())
                    .nickName(entity.getNickName())
                    .email(entity.getEmail())
                    .avatarUrl(entity.getAvatarUrl())
                    .hurtLevel(entity.getHurtLevel())
                    .currentAttackPower(entity.getCurrentAttackPower())
                    .experience(entity.getExperience())
                    .health(entity.getHealth())
                    .level(entity.getLevel())
                    .inventory(MAPPER.readValue(entity.getInventoryJson(), Backpack.class))
                    .position(new Point2D.Double(entity.getPositionX(), entity.getPositionY()))
                    .build();
            result.getBuffs().clear();
            for (Buff buff : MAPPER.readValue(entity.getBuffsJson(), Buff[].class)) {
                result.addBuff(buff);
            }
        } catch (JsonProcessingException e) {
            log.error("Player mapping exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return null;
        }
        return result;
    }
}
