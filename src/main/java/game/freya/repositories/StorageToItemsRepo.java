package game.freya.repositories;

import game.freya.entities.middles.StorageToItems;
import game.freya.entities.middles.StorageToItemsPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageToItemsRepo extends JpaRepository<StorageToItems, UUID>, JpaSpecificationExecutor<StorageToItems> {
    @Query("""
            select sti.haveCount from StorageToItems sti
            where sti.id.itemUid    = :itemUid
              and sti.id.storageUid = :storageUid""")
    int findById_ItemUidAndId_StorageUid(UUID itemUid, UUID storageUid);

    @Modifying
    @Query("""
            update StorageToItems sti
            set sti.haveCount = :count
            where sti.id.itemUid        = :itemUid
                and sti.id.storageUid   = :storageUid""")
    int updateById_ItemUidAndId_StorageUid(UUID itemUid, UUID storageUid, int count);

    boolean existsById(StorageToItemsPK id);

    @Modifying
    @Query("""
            delete from StorageToItems sti where sti.id.itemUid = :itemUid and sti.id.storageUid = :storageUid""")
    void deleteByItemUidAndStorageUid(UUID itemUid, UUID storageUid);
}
