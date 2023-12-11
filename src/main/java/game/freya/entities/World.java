package game.freya.entities;

import game.freya.enums.other.HardnessLevel;
import game.freya.items.prototypes.Environment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worlds", uniqueConstraints = @UniqueConstraint(name = "uc_title_n_uid_world", columnNames = {"id", "title"}))
public class World implements Serializable {

    @Id
    @NotNull
    // @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false, unique = true)
    private UUID uid;

    @NotNull
    @Column(name = "author", nullable = false, updatable = false)
    private UUID author;

    @NotNull
    @Column(name = "title", length = 64, nullable = false)
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

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HardnessLevel level = HardnessLevel.EASY;

    @NotNull
    @Builder.Default
    @CreatedDate
    @CreationTimestamp
    @Column(name = "create_date", nullable = false, columnDefinition = "TIMESTAMP", updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_local_world", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isLocalWorld = true;

    @Column(name = "network_address")
    private String networkAddress;

    @Builder.Default
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Environment> environments = new LinkedHashSet<>();

//    @OneToMany(cascade = CascadeType.ALL)
//    @LazyCollection(LazyCollectionOption.FALSE)
//    private Set<Environment> environments = new ArrayList<>();

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getCreateDate());
    }

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
}
