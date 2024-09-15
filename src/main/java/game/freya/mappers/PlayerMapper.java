package game.freya.mappers;

import game.freya.dto.roots.PlayerDto;
import game.freya.entities.roots.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public final class PlayerMapper {

    public Player toEntity(PlayerDto dto) {
        if (dto == null) {
            return null;
        }
        return Player.builder()
                .uid(dto.getUid())
                .nickName(dto.getNickName())
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .lastPlayedWorldUid(dto.getLastPlayedWorldUid())
                .build();
    }

    public PlayerDto toDto(Player entity) {
        if (entity == null) {
            return null;
        }
        return PlayerDto.builder()
                .uid(entity.getUid())
                .nickName(entity.getNickName())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .lastPlayedWorldUid(entity.getLastPlayedWorldUid())
                .build();
    }

    public Set<Player> toEntities(Map<String, PlayerDto> players) {
        return players.values().stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
