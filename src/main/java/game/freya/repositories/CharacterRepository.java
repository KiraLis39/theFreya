package game.freya.repositories;

import game.freya.entities.PlayCharacter;
import game.freya.entities.roots.prototypes.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID>, JpaSpecificationExecutor<Character> {
    Optional<PlayCharacter> findByNameAndWorldUid(String characterName, UUID worldUid);

    void deleteByUid(UUID uid);

    Optional<PlayCharacter> findByUid(UUID heroId);

    List<PlayCharacter> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUuid);

    Set<PlayCharacter> findAllByWorldUid(UUID uid);
}
