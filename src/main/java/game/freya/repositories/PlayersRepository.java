package game.freya.repositories;

import game.freya.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayersRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {
    @Transactional
    @Modifying
    @Query("update Player p set p.nickName = ?1 where p.nickName = ?2")
    int updateNickNameByNickName(String nickName, @NonNull String nickName1);

    Optional<Player> findByEmailIgnoreCase(@NonNull String email);

    Optional<Player> findByUid(@NonNull UUID uid);

    @Modifying(clearAutomatically = true)
    @Query("""
            update Player p
            set p.nickName = :nickName
            where p.uid = :puid""")
    void updateNickNameByUid(UUID puid, String nickName);
}
