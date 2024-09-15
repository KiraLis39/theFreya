package game.freya.entities.roots;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@Entity
@RequiredArgsConstructor
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;

    @Column(name = "nick_name", length = 16, unique = true)
    private String nickName;

    @Column(name = "email", length = 16, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 128)
    private String avatarUrl;

    @Column(name = "last_played_world_uid")
    private UUID lastPlayedWorldUid;
}
