package game.freya.entities.roots.prototypes;

import game.freya.entities.Backpack;
import game.freya.entities.Weapon;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.util.List;

@Accessors(chain = true)
@Setter
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "character_type")
@Table(name = "characters", uniqueConstraints = {@UniqueConstraint(name = "uc_names_in_world", columnNames = {"name", "world_uid"})})
public abstract class Character extends AbstractEntity {
    @Min(1)
    @Column(name = "level", nullable = false)
    private short level;

    @Column(name = "experience", nullable = false)
    private long experience;

    @Column(name = "health", nullable = false)
    private int health;

    @Column(name = "max_health", nullable = false)
    private int maxHealth;

    @Column(name = "oil", nullable = false)
    private int oil;

    @Column(name = "max_oil", nullable = false)
    private int maxOil;

    @Column(name = "power", nullable = false)
    private float power;

    @Enumerated(EnumType.STRING)
    @Column(name = "hurt_level", nullable = false)
    private HurtLevel hurtLevel;

    @Column(name = "speed", nullable = false)
    private byte speed;

    @Column(name = "vector", nullable = false)
    private MovingVector vector;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hero_type", nullable = false)
    private HeroType type;

    @Column(name = "in_game_time", columnDefinition = "bigint default 0", nullable = false)
    private long inGameTime;

    @Column(name = "is_online", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOnline;

    // созданное игроком оружие в любом случае не удаляется, остаётся для лута или анализа
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.EAGER)
    private Weapon currentWeapon;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Backpack inventory;

    @NotNull
    @Size(max = 32)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Buff> buffs;
}
