package game.freya.services;

import game.freya.entities.World;
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

    public long count() {
        return worldRepository.count();
    }

    public List<World> findAll() {
        return worldRepository.findAll();
    }

    public World save(World world) {
        return worldRepository.save(world);
    }
}
