package game.freya.repositories;

import game.freya.entities.roots.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayersRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {
    @Modifying
    @Query("update Player p set p.nickName = :nickNameNew where p.nickName = :nickNameOld")
    int updateNickNameByNickName(String nickNameNew, @NonNull String nickNameOld);

    Optional<Player> findByEmailIgnoreCase(@NonNull String email);

    Optional<Player> findByUid(@NonNull UUID uid);

    @Modifying(clearAutomatically = true)
    @Query("""
            update Player p
            set p.nickName = :nickName
            where p.uid = :puid""")
    void updateNickNameByUid(UUID puid, String nickName);
}
