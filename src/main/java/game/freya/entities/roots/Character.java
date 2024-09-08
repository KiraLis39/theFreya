package game.freya.entities.roots;

import game.freya.entities.AbstractEntity;
import game.freya.entities.Backpack;
import game.freya.enums.player.HeroCorpusType;
import game.freya.enums.player.HeroPeripheralType;
import game.freya.enums.player.HeroType;
import game.freya.enums.player.HurtLevel;
import game.freya.enums.player.MovingVector;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorColumn(name = "character_type")
@Table(name = "characters", uniqueConstraints = @UniqueConstraint(name = "uc_names_in_world", columnNames = {"name", "world_uid"}))
public class Character extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;

    @Min(1)
    @Column(name = "level")
    private short level;

    @Builder.Default
    @Column(name = "experience")
    private long experience = 0;

    @Column(name = "health")
    private int health;

    @Builder.Default
    @Column(name = "max_health")
    private int maxHealth = 100;

    @Builder.Default
    @Column(name = "oil")
    private int oil = 100;

    @Builder.Default
    @Column(name = "max_oil")
    private int maxOil = 100;

    @Builder.Default
    @Column(name = "power")
    private float power = 1.0f;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "hurt_level")
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Builder.Default
    @Column(name = "speed")
    private byte speed = 6;

    @Builder.Default
    @Column(name = "vector")
    private MovingVector vector = MovingVector.UP;

    @Column(name = "base_color", nullable = false)
    private Color baseColor;

    @Column(name = "second_color", nullable = false)
    private Color secondColor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroCorpusType corpusType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroPeripheralType peripheralType;

    @Column(name = "peripheral_size", columnDefinition = "SMALLINT DEFAULT 50", nullable = false)
    private short peripheralSize;

    // созданное игроком оружие в любом случае не удаляется, остаётся для лута или анализа
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.EAGER)
    private Weapon currentWeapon;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Backpack inventory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hero_type", nullable = false)
    private HeroType heroType;

    @NotNull
    @NotEmpty
    @Builder.Default
    @JoinColumn(name = "buffs", referencedColumnName = "uid")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Buff> buffs = new ArrayList<>(9);

    @Builder.Default
    @Column(name = "in_game_time", columnDefinition = "bigint default 0", nullable = false)
    private long inGameTime = 0;

    @LastModifiedDate
    @Column(name = "last_play_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    private LocalDateTime lastPlayDate;

    @Column(name = "is_online", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOnline;
}
