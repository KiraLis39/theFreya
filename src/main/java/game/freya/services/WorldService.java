package game.freya.services;

import game.freya.dto.WorldDto;
import game.freya.entities.World;
import game.freya.interfaces.iEnvironment;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
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

    @Setter
    @Getter
    private WorldDto currentWorld;

    @Transactional
    public WorldDto save(WorldDto world) {
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
        if (currentWorld != null) {
            worldRepository.saveAndFlush(worldMapper.toEntity(currentWorld));
        }
    }

    @Transactional(readOnly = true)
    public List<WorldDto> findAllByNetAvailable(boolean isNetAvailable) {
        return worldMapper.toDto(worldRepository.findAllByIsNetAvailableIs(isNetAvailable));
    }

    public Set<iEnvironment> getEnvironmentsFromRectangle(Rectangle rectangle) {
        return currentWorld.getEnvironments().stream()
                .filter(e -> e.isInSector(rectangle))
                .collect(Collectors.toSet());
    }

    public boolean isWorldExist(UUID worldUid) {
        return worldRepository.existsById(worldUid);
    }
}
