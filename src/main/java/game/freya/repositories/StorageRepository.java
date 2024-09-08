package game.freya.repositories;

import game.freya.entities.roots.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageRepository extends JpaRepository<Storage, UUID>, JpaSpecificationExecutor<Storage> {
}
