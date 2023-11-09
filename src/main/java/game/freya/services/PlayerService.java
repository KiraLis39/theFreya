package game.freya.services;

import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.PlayerMapper;
import game.freya.repositories.PlayersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class PlayerService {
    private final PlayersRepository playersRepository;
    private final PlayerMapper playerMapper;

    public long count() {
        return playersRepository.count();
    }

    public List<Player> findAll() {
        return playersRepository.findAll();
    }

    public Player save(Player player) {
        if (player != null) {
            return playersRepository.save(player);
        }
        throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "player entity in PlayerService:save()");
    }

    public Optional<Player> findByUid(UUID userId) {
        return playersRepository.findByUid(userId);
    }

    public Optional<Player> findByMail(String userMail) {
        return playersRepository.findByEmailIgnoreCase(userMail);
    }

    public void updatePlayer(PlayerDTO playerDTO) {
        Optional<Player> playerToUpdate = playersRepository.findByUid(playerDTO.getUid());
        if (playerToUpdate.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.PLAYER_NOT_FOUND, playerDTO.getNickName());
        }

        // подменяем uuid текущего игрока на uuid устаревшего игрока из БД:
        playerDTO.setUid(playerToUpdate.get().getUid());
        // сохраняем нового вместо устаревшего игрока:
        save(playerMapper.toEntity(playerDTO));
    }
}
