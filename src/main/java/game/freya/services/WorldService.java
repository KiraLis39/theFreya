package game.freya.services;

import game.freya.entities.World;
import game.freya.entities.dto.WorldDTO;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class WorldService {
    private final WorldRepository worldRepository;
    private final WorldMapper worldMapper;

    public List<WorldDTO> findAll() {
        return worldMapper.toDto(worldRepository.findAll());
    }

    public WorldDTO save(WorldDTO world) {
        World w = worldMapper.toEntity(world);
        World w2 = worldRepository.findByUid(world.getUid()).orElse(null);
        if (w2 != null) {
            BeanUtils.copyProperties(w, w2);
        } else {
            w2 = w;
        }
        w2 = worldRepository.save(w2);
        return worldMapper.toDto(w2);
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

    public void delete(UUID worldUid) {
        worldRepository.deleteById(worldUid);
    }
}
