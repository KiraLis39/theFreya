package game.freya.services;

import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.PlayerMapper;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class WorldService {
    private final WorldRepository worldRepository;
    private final PlayerMapper playerMapper;
    private final WorldMapper worldMapper;

    public long count() {
        return worldRepository.count();
    }

    public List<WorldDTO> findAll() {
        return worldMapper.toDto(worldRepository.findAll());
    }

    public WorldDTO save(WorldDTO world) {
        World saved = worldRepository.save(worldMapper.toEntity(world));
        return worldMapper.toDto(saved);
    }

    public WorldDTO addPlayerToWorld(WorldDTO worldDTO, PlayerDTO currentPlayer) {
        Optional<World> worldOpt = worldRepository.findByUidWithPlayers(worldDTO.getUid());
        if (worldOpt.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, worldDTO.getUid());
        }
        World w = worldOpt.get();
        Player p = playerMapper.toEntity(currentPlayer);
        w = w.addPlayer(p);
        w = worldRepository.save(w);
        return worldMapper.toDto(w);
    }
}
