package game.freya.entities;

import game.freya.enums.HurtLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "heroes")
public class Hero {
    @Id
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false, unique = true)
    private UUID uid = UUID.randomUUID();

    @Column(name = "hero_name", length = 16, unique = true)
    private String heroName;

    @Column(name = "inventory_json", length = 1024)
    private String inventoryJson;

    @Builder.Default
    @Column(name = "level")
    private short level = 1;

    @Builder.Default
    @Column(name = "attack")
    private float currentAttackPower = 1.0f;

    @Builder.Default
    @Column(name = "experience")
    private float experience = 0f;

    @Builder.Default
    @Column(name = "health")
    private short health = 100;

    @Builder.Default
    @Column(name = "max_health")
    private short maxHealth = 100;

    @Builder.Default
    @Column(name = "speed")
    private short speed = 6;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Column(name = "buffs_json", length = 1024)
    private String buffsJson;

    @Column(name = "position_x", columnDefinition = "double precision DEFAULT 256")
    private double positionX;

    @Column(name = "position_y", columnDefinition = "double precision DEFAULT 256")
    private double positionY;

    @Builder.Default
    @Column(name = "in_game_time", columnDefinition = "bigint default 0")
    private long inGameTime = 0;

    @ManyToOne(cascade = ALL, fetch = FetchType.EAGER)
    private World world;

    @ManyToOne(cascade = ALL, fetch = FetchType.EAGER)
    private Player ownedPlayer;
}
