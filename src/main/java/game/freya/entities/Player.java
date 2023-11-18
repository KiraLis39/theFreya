package game.freya.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
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

    @Column(name = "last_played_world_uid")
    private UUID lastPlayedWorldUid;

    @Override
    public String toString() {
        return "Player{"
                + "uid=" + uid
                + ", nickName='" + nickName + '\''
                + ", email='" + email + '\''
                + ", avatarUrl='" + avatarUrl + '\''
                + ", lastPlayedWorldUid=" + lastPlayedWorldUid
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(getUid(), player.getUid())
                && Objects.equals(getNickName(), player.getNickName()) && Objects.equals(getEmail(), player.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getNickName(), getEmail());
    }
}
