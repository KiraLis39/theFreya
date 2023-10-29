package game.freya.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class PlayerMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PlayerMapper() {
    }

    public static Player toEntity(PlayerDTO dto) {
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
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Player mapping exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return null;
        }
        return result;
    }

    public static Set<Player> toEntity(Map<String, PlayerDTO> players) {
        return players.values().stream().map(PlayerMapper::toEntity).collect(Collectors.toSet());
    }
}
