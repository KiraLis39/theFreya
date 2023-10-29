package game.freya.services;

import game.freya.repositories.PlayersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class PlayerService {
    private final PlayersRepository playersRepository;


}
