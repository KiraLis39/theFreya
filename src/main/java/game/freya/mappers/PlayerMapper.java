package game.freya.mappers;

import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class PlayerMapper {

    private PlayerMapper() {
    }

    public Player toEntity(PlayerDTO dto) {
        if (dto == null) {
            return null;
        }
        return Player.builder()
                .uid(dto.getUid())
                .nickName(dto.getNickName())
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .inGameTime(dto.getInGameTime())
                .lastPlayedWorld(dto.getLastPlayedWorld())
                .heroes(dto.getHeroes())
                .build();
    }

    public Set<Player> toEntities(Map<String, PlayerDTO> players) {
        return players.values().stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public PlayerDTO toDto(Player entity) {
        if (entity == null) {
            return null;
        }
        return PlayerDTO.builder()
                .uid(entity.getUid())
                .nickName(entity.getNickName())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .inGameTime(entity.getInGameTime())
                .lastPlayedWorld(entity.getLastPlayedWorld())
                .heroes(entity.getHeroes())
                .build();
    }
}
