package game.freya.services;

import game.freya.entities.World;
import game.freya.entities.dto.WorldDTO;
import game.freya.items.interfaces.iEnvironment;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class WorldService {
    private final WorldRepository worldRepository;

    private final WorldMapper worldMapper;

    @Getter
    private WorldDTO currentWorld;

    @Transactional
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

    @Transactional(readOnly = true)
    public Optional<World> findByUid(UUID uid) {
        return worldRepository.findByUid(uid);
    }

    @Modifying
    @Transactional
    public void delete(UUID worldUid) {
        worldRepository.deleteById(worldUid);
    }

    @Transactional
    public void saveCurrentWorld() {
        World worldToSave = worldMapper.toEntity(currentWorld);
        if (worldToSave != null) {
            worldRepository.save(worldToSave);
        }
    }

    @Transactional(readOnly = true)
    public List<WorldDTO> findAllByNetAvailable(boolean isNetAvailable) {
        return worldMapper.toDto(worldRepository.findAllByIsNetAvailableIs(isNetAvailable));
    }

    public Set<iEnvironment> getEnvironmentsFromRectangle(Rectangle rectangle) {
        return currentWorld.getEnvironments().stream().filter(e -> rectangle.contains(e.getPosition())).collect(Collectors.toSet());
    }

    public WorldDTO setCurrentWorld(WorldDTO currentWorld) {
        this.currentWorld = currentWorld;
        return this.currentWorld;
    }

    public boolean isWorldExist(UUID worldUid) {
        return worldRepository.existsById(worldUid);
    }
}
