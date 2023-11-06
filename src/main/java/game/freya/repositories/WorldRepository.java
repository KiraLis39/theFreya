package game.freya.repositories;

import game.freya.entities.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorldRepository extends JpaRepository<World, UUID>, JpaSpecificationExecutor<World> {
    Optional<World> findByUid(UUID uid);

    @Query("""
            select w from World w left join fetch w.players p
            where w.uid = :uuid""")
    Optional<World> findByUidWithPlayers(UUID uuid);

}
