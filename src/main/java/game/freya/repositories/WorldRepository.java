package game.freya.repositories;

import game.freya.entities.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorldRepository extends JpaRepository<World, UUID>, JpaSpecificationExecutor<World> {
    Optional<World> findByUid(UUID uid);

    List<World> findAllByIsNetAvailableIs(boolean isNetAvailable);

    World findFirstByTitleIsNotNull();
}
