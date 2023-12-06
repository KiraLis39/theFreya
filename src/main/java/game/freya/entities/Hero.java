package game.freya.entities;

import game.freya.enums.HeroCorpusType;
import game.freya.enums.HeroPeriferiaType;
import game.freya.enums.HeroType;
import game.freya.enums.HurtLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "heroes", uniqueConstraints = @UniqueConstraint(name = "uc_names_in_world", columnNames = {"hero_name", "world_uid"}))
public class Hero {
    @Id
    @Comment("UUID героя")
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false, unique = true, insertable = false)
    private UUID uid;

    @Comment("Имя героя")
    @Column(name = "hero_name", length = 16, nullable = false)
    private String heroName;

    @Comment("Главный цвет раскраски корпуса героя")
    @Column(name = "base_color", nullable = false)
    private Color baseColor;

    @Comment("Второстепенный цвет раскраски корпуса героя")
    @Column(name = "second_color", nullable = false)
    private Color secondColor;

    @Builder.Default
    @Comment("Тип корпуса героя")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @Builder.Default
    @Comment("Тип периферии героя")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroPeriferiaType periferiaType = HeroPeriferiaType.COMPACT;

    @Comment("Размеры периферии героя")
    @Column(name = "periferia_size", columnDefinition = "SMALLINT DEFAULT 50", nullable = false)
    private short periferiaSize;

    @Comment("Инвентарь героя")
    @Column(name = "inventory_json", length = 1024, nullable = false)
    private String inventoryJson;

    @Builder.Default
    @Comment("Уровень героя")
    @Column(name = "level", nullable = false)
    private short level = 1;

    @Builder.Default
    @Comment("Тип корпуса героя")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroType type = HeroType.VOID;

    @Builder.Default
    @Comment("Мощность героя")
    @Column(name = "power", nullable = false)
    private float power = 1.0f;

    @Builder.Default
    @Comment("Накопленный опыт героя")
    @Column(name = "experience", nullable = false)
    private long experience = 0;

    @Builder.Default
    @Comment("Текущее здоровье героя")
    @Column(name = "cur_health", nullable = false)
    private int curHealth = 100;

    @Builder.Default
    @Comment("Максимальное здоровье героя")
    @Column(name = "max_health", nullable = false)
    private int maxHealth = 100;

    @Builder.Default
    @Comment("Текущий запас масла героя")
    @Column(name = "cur_oil", nullable = false)
    private int curOil = 100;

    @Builder.Default
    @Comment("Максимальный запас масла героя")
    @Column(name = "max_oil", nullable = false)
    private int maxOil = 100;

    @Builder.Default
    @Comment("Скорость героя")
    @Column(name = "speed", nullable = false)
    private byte speed = 6;

    @Comment("Имеющиеся бафы героя")
    @Column(name = "buffs_json", length = 1024, nullable = false)
    private String buffsJson;

    @Comment("Позиция героя на карте по горизонтали")
    @Column(name = "position_x", columnDefinition = "double precision DEFAULT 256", nullable = false)
    private double positionX;

    @Comment("Позиция героя на карте по вертикали")
    @Column(name = "position_y", columnDefinition = "double precision DEFAULT 256", nullable = false)
    private double positionY;

    @Builder.Default
    @Comment("Время, проведенное в игре")
    @Column(name = "in_game_time", columnDefinition = "bigint default 0", nullable = false)
    private long inGameTime = 0;

    @Comment("Мир героя")
    @Column(name = "world_uid", nullable = false, updatable = false)
    private UUID worldUid;

    @Comment("Создатель героя")
    @Column(name = "owner_uid", nullable = false)
    private UUID ownerUid;

    @Builder.Default
    @CreatedDate
    @CreationTimestamp
    @Comment("Дата создания героя")
    @Column(name = "create_date", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    @Comment("Дата последнего входа в игру")
    @Column(name = "last_play_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastPlayDate = LocalDateTime.now();

    // custom fields:
    @Transient
    private boolean isOnline;

    @Transient
    private HurtLevel hurtLevel;

    // methods:
    @Override
    public String toString() {
        return "Hero{"
                + "uid=" + uid
                + ", heroName='" + heroName + '\''
                + ", level=" + level
                + ", type=" + type
                + ", power=" + power
                + ", experience=" + experience
                + ", curHealth=" + curHealth
                + ", maxHealth=" + maxHealth
                + ", speed=" + speed
                + ", positionX=" + positionX
                + ", positionY=" + positionY
                + ", worldUid=" + worldUid
                + ", ownerUuid=" + ownerUid
                + '}';
    }
}
