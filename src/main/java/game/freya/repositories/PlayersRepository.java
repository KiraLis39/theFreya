package game.freya.repositories;

import game.freya.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayersRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {
    Optional<Player> findByEmailIgnoreCase(@NonNull String email);

    Optional<Player> findByUid(@NonNull UUID uid);
}
