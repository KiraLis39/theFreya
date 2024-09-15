package game.freya.entities.roots;

import game.freya.entities.roots.prototypes.Character;
import game.freya.entities.roots.prototypes.Environment;
import game.freya.enums.other.HardnessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@Table(name = "worlds", uniqueConstraints = @UniqueConstraint(name = "uc_title_n_uid_world", columnNames = {"uid", "name"}))
public class World {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @NotNull
    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "owner_uid")
    private UUID ownerUid;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @NotNull
    @Column(name = "size", nullable = false)
    private Dimension size;

    @Column(name = "is_net_available", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isNetAvailable;

    @Column(name = "password")
    private String password; // bcrypt

    @NotNull
    @Enumerated(EnumType.STRING)
    private HardnessLevel hardnessLevel;

    @Column(name = "is_local", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isLocal;

    @Column(name = "address")
    private String address;

    @Column(name = "cache_key", length = 32)
    private String cacheKey;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "world_uid", unique = true)
    private Set<Environment> environments;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.LAZY)
    @JoinTable(name = "worlds_heroes", joinColumns = @JoinColumn(name = "world_uid"), inverseJoinColumns = @JoinColumn(name = "hero_uid"))
    private Set<Character> heroes;
}
