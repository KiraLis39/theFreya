package game.freya.repositories;

import game.freya.entities.roots.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID>, JpaSpecificationExecutor<Character> {
    Optional<Character> findByNameAndWorldUid(String characterName, UUID worldUid);

    void deleteByUid(UUID uid);

    Optional<Character> findByUid(UUID heroId);

    List<Character> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUuid);

    List<Character> findAllByWorldUid(UUID uid);
}