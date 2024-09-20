package game.freya.services;

import game.freya.dto.roots.WorldDto;
import game.freya.entities.roots.World;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.interfaces.subroot.iEnvironment;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class WorldService {
    private final WorldRepository worldRepository;
    private final WorldMapper worldMapper;
    private GameControllerService gameControllerService;

    @Getter
    private WorldDto currentWorld;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void setCurrentWorld(UUID selectedWorldUuid) {
        Optional<WorldDto> selected = findByUid(selectedWorldUuid);
        if (selected.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid.toString());
        }
        gameControllerService.getPlayerService().getCurrentPlayer().setLastPlayedWorldUid(selectedWorldUuid);
        this.currentWorld = selected.get();
    }

    public WorldDto saveOrUpdate(WorldDto world) {
        World w = worldMapper.toEntity(world);
        World w2 = worldRepository.findByUid(world.getUid()).orElse(null);
        if (w2 != null) {
            BeanUtils.copyProperties(w, w2);
        } else {
            w2 = w;
        }
        return worldMapper.toDto(worldRepository.saveAndFlush(w2));
    }

    public void saveCurrent() {
        if (currentWorld == null) {
            return;
        }
        if (currentWorld.getCreatedBy() == null) {
            throw new RuntimeException();
        }
        worldRepository.saveAndFlush(worldMapper.toEntity(currentWorld));
    }

    @Transactional(readOnly = true)
    public Optional<WorldDto> findByUid(UUID uid) {
        return worldRepository.findByUid(uid).map(worldMapper::toDto);
    }

    @Modifying
    @Transactional
    public boolean deleteByUid(UUID worldUid) {
        if (worldRepository.existsById(worldUid)) {
            worldRepository.deleteById(worldUid);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<WorldDto> findAllByNetAvailable(boolean isNetAvailable) {
        return worldMapper.toDto(worldRepository.findAllByIsNetAvailableIs(isNetAvailable));
    }

    public Set<iEnvironment> getEnvironmentsFromRectangle(Rectangle2D.Double rectangle) {
        return currentWorld.getEnvironments().stream()
                .filter(e -> e.isInSector(rectangle))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean isWorldExist(UUID worldUid) {
        return worldRepository.existsById(worldUid);
    }
}
