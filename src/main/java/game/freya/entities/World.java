package game.freya.entities;

import game.freya.entities.roots.Environment;
import game.freya.enums.other.HardnessLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worlds", uniqueConstraints = @UniqueConstraint(name = "uc_title_n_uid_world", columnNames = {"id", "title"}))
public class World {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
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
    @Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    @Column(name = "local_world", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isLocalWorld = true;

    @Column(name = "network_address")
    private String networkAddress;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Environment> environments = new HashSet<>();
}
