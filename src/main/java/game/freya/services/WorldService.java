package game.freya.services;

import game.freya.entities.Player;
import game.freya.entities.World;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.HardnessLevel;
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
import java.util.Random;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WorldService {
    private final WorldRepository worldRepository;
    private final PlayerService playerService;
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
        p.setLastPlayedWorld(w.getUid());

        w = w.addPlayer(p);
        w = worldRepository.save(w);
        return worldMapper.toDto(w);
    }

    public Optional<World> findByUid(UUID uid) {
        return worldRepository.findByUid(uid);
    }

    public boolean existsByUuid(UUID lpw) {
        if (lpw == null) {
            return false;
        }
        return worldRepository.existsById(lpw);
    }

    public WorldDTO createDefaultWorld(PlayerDTO player) {
        World w = World.builder()
                .uid(UUID.randomUUID())
                .title("A new world " + new Random().nextInt(100))
                .level(HardnessLevel.EASY)
                .dimensionWidth(32)
                .dimensionHeight(32)
                .passwordHash(-1)
                .build();

        w = worldRepository.save(w);
        player.setLastPlayedWorld(w.getUid());

        w.addPlayer(playerMapper.toEntity(player));
        w = worldRepository.saveAndFlush(w);

        return worldMapper.toDto(worldRepository.findByUid(w.getUid()).get());
    }
}
