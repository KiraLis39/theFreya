package game.freya.entities;

import game.freya.enums.HardnessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worlds")
public class World implements Serializable {
    @Builder.Default
    @ElementCollection
    private final Set<String> environments = HashSet.newHashSet(100);

    @Id
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private UUID uid = UUID.randomUUID();

    @Column(name = "author")
    private UUID author;

    @Column(name = "title", length = 64, unique = true, nullable = false)
    private String title;

    @Builder.Default
    @Column(name = "is_net_available", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isNetAvailable = false;

    @Column(name = "password_hash")
    private int passwordHash;

    @Builder.Default
    @Column(name = "dimension_w")
    private int dimensionWidth = 64; // 64 = 2048 cells | 128 = 4096 cells

    @Builder.Default
    @Column(name = "dimension_h")
    private int dimensionHeight = 32; // 32 = 1024 cells | 128 = 4096 cells

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HardnessLevel level = HardnessLevel.EASY;

    @Builder.Default
    @CreatedDate
    @CreationTimestamp
    @Column(name = "create_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_local_world", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isLocalWorld = true;

    @Column(name = "network_address")
    private String networkAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        World world = (World) o;
        return Objects.equals(getUid(), world.getUid()) && Objects.equals(getCreateDate(), world.getCreateDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getCreateDate());
    }
}
