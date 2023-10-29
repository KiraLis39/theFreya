package game.freya.repositories;

import game.freya.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayersRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {
}
