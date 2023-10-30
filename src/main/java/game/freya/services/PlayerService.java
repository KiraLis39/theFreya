package game.freya.services;

import game.freya.entities.Player;
import game.freya.repositories.PlayersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class PlayerService {
    private final PlayersRepository playersRepository;

    public long count() {
        return playersRepository.count();
    }

    public List<Player> findAll() {
        return playersRepository.findAll();
    }

    public Player save(Player player) {
        return playersRepository.save(player);
    }
}
