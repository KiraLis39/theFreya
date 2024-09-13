package game.freya.entities.roots;

import game.freya.entities.Backpack;
import game.freya.enums.player.HeroCorpusType;
import game.freya.enums.player.HeroPeripheralType;
import game.freya.enums.player.HeroType;
import game.freya.enums.player.HurtLevel;
import game.freya.enums.player.MovingVector;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorColumn(name = "character_type")
@Table(name = "characters", uniqueConstraints = @UniqueConstraint(name = "uc_names_in_world", columnNames = {"name", "world_uid"}))
public class Character extends AbstractEntity {
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

    @Lob
    @Column(name = "base_color", nullable = false)
    private Color baseColor;

    @Lob
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
    @Size(max = 32)
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "uid")
    private List<Buff> buffs = new ArrayList<>(9);

    @Builder.Default
    @Column(name = "in_game_time", columnDefinition = "bigint default 0", nullable = false)
    private long inGameTime = 0;

    @UpdateTimestamp
    @Column(name = "last_play_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    private LocalDateTime lastPlayDate;

    @Column(name = "is_online", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOnline;
}
