package game.freya.services;

import game.freya.entities.dto.WorldDTO;
import game.freya.mappers.WorldMapper;
import game.freya.repositories.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class WorldService {
    private final WorldRepository worldRepository;
    private final WorldMapper worldMapper;

    public long count() {
        return worldRepository.count();
    }

    public List<WorldDTO> findAll() {
        return worldMapper.toDto(worldRepository.findAll());
    }

    public WorldDTO save(WorldDTO world) {
        return worldMapper.toDto(worldRepository.save(worldMapper.toEntity(world)));
    }
}
