package game.freya.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players")
public class Player {
    @Id
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID uid = UUID.randomUUID();

    @Column(name = "nick_name", length = 16, unique = true)
    private String nickName;

    @Column(name = "email", length = 16, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 128)
    private String avatarUrl;

    @Column(name = "last_played_world_uid")
    private UUID lastPlayedWorldUid;
}
