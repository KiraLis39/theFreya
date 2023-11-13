package game.freya.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
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
    @Column(name = "id", nullable = false, insertable = false, updatable = false, unique = true)
    private UUID uid = UUID.randomUUID();

    @Column(name = "nick_name", length = 16, unique = true)
    private String nickName;

    @Column(name = "email", length = 16, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 128)
    private String avatarUrl;

    @Builder.Default
    @Column(name = "in_game_time", columnDefinition = "bigint default 0")
    private long inGameTime = 0;

    @Column(name = "last_played_world")
    private UUID lastPlayedWorld;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Hero> heroes = HashSet.newHashSet(3);
}
