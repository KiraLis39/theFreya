package game.freya.entities;

import game.freya.entities.logic.Buff;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeripheralType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.items.containers.Backpack;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "heroes", uniqueConstraints = @UniqueConstraint(name = "uc_names_in_world", columnNames = {"hero_name", "world_uid"}))
public class Hero {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @Column(name = "hero_name", length = 16, nullable = false)
    private String heroName;

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

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Backpack inventory;

    @Column(name = "level", nullable = false)
    private short level;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hero_type", nullable = false)
    private HeroType heroType;

    @Builder.Default
    @Column(name = "power")
    private float power = 1f;

    @Builder.Default
    @Column(name = "experience")
    private long experience = 0;

    @Builder.Default
    @Column(name = "cur_health", nullable = false)
    private int curHealth = 100;

    @Builder.Default
    @Column(name = "max_health", nullable = false)
    private int maxHealth = 100;

    @Builder.Default
    @Column(name = "cur_oil", nullable = false)
    private int curOil = 100;

    @Builder.Default
    @Column(name = "max_oil", nullable = false)
    private int maxOil = 100;

    @Builder.Default
    @Column(name = "speed", nullable = false)
    private byte speed = 6;

    @NotNull
    @NotEmpty
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Buff> buffs = new ArrayList<>(9);

    @Column(name = "location", nullable = false)
    private Point2D.Double location;

    @Builder.Default
    @Column(name = "in_game_time", columnDefinition = "bigint default 0", nullable = false)
    private long inGameTime = 0;

    @NotNull
    @Column(name = "world_uid", nullable = false)
    private UUID worldUid;

    @NotNull
    @Column(name = "owner_uid", nullable = false)
    private UUID ownerUid;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreatedDate
    @CreationTimestamp
    @Column(name = "create_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp", nullable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "last_play_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    private LocalDateTime lastPlayDate;

    @Column(name = "is_online", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOnline;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hurt_level")
    private HurtLevel hurtLevel;
}
