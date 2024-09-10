package game.freya.entities.roots;

import game.freya.enums.other.HardnessLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@Table(name = "worlds", uniqueConstraints = @UniqueConstraint(name = "uc_title_n_uid_world", columnNames = {"uid", "name"}))
public class World extends AbstractEntity {
    @Builder.Default
    @Column(name = "is_net_available", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isNetAvailable = false;

    @Column(name = "password_hash")
    private int passwordHash;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HardnessLevel level = HardnessLevel.EASY;

    @Builder.Default
    @Column(name = "local_world", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isLocalWorld = true;

    @Column(name = "network_address")
    private String networkAddress;

    @Builder.Default
//    @JoinColumn(name = "environments", referencedColumnName = "uid")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "uid")
    private Set<Environment> environments = new HashSet<>();
}
