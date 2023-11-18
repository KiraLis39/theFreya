package game.freya.repositories;

import game.freya.entities.Hero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeroRepository extends JpaRepository<Hero, UUID>, JpaSpecificationExecutor<Hero> {
    long deleteByUid(UUID uid);

    Optional<Hero> findByUid(UUID heroId);
}
