package game.freya.entities;

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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
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
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false, unique = true)
    private UUID uid = UUID.randomUUID();

    @Comment("Имя героя")
    @Column(name = "hero_name", length = 16)
    private String heroName;

    @Comment("Инвентарь героя")
    @Column(name = "inventory_json", length = 1024)
    private String inventoryJson;

    @Builder.Default
    @Comment("Уровень героя")
    @Column(name = "level")
    private short level = 1;

    @Builder.Default
    @Comment("Тип корпуса героя")
    @Enumerated(EnumType.STRING)
    private HeroType type = HeroType.VOID;

    @Builder.Default
    @Comment("Мощность героя")
    @Column(name = "power")
    private float power = 1.0f;

    @Builder.Default
    @Comment("Накопленный опыт героя")
    @Column(name = "experience")
    private float experience = 0f;

    @Builder.Default
    @Comment("Текущее здоровье героя")
    @Column(name = "cur_health")
    private short curHealth = 100;

    @Builder.Default
    @Comment("Максимальное здоровье героя")
    @Column(name = "max_health")
    private short maxHealth = 100;

    @Builder.Default
    @Comment("Текущий запас масла героя")
    @Column(name = "cur_oil")
    private short curOil = 100;

    @Builder.Default
    @Comment("Максимальный запас масла героя")
    @Column(name = "max_oil")
    private short maxOil = 100;

    @Builder.Default
    @Comment("Скорость героя")
    @Column(name = "speed")
    private byte speed = 6;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Comment("Имеющиеся бафы героя")
    @Column(name = "buffs_json", length = 1024)
    private String buffsJson;

    @Comment("Позиция героя на карте по горизонтали")
    @Column(name = "position_x", columnDefinition = "double precision DEFAULT 256")
    private double positionX;

    @Comment("Позиция героя на карте по вертикали")
    @Column(name = "position_y", columnDefinition = "double precision DEFAULT 256")
    private double positionY;

    @Builder.Default
    @Comment("Время, проведенное в игре")
    @Column(name = "in_game_time", columnDefinition = "bigint default 0")
    private long inGameTime = 0;

    @Comment("Мир героя")
    @Column(name = "world_uid")
    private UUID worldUid;

    @Comment("Создатель героя")
    @Column(name = "owner_uid")
    private UUID ownerUid;

    @Builder.Default
    @CreatedDate
    @CreationTimestamp
    @Comment("Дата создания героя")
    @Column(name = "create_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    @Comment("Дата последнего входа в игру")
    @Column(name = "last_play_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastPlayDate = LocalDateTime.now();


    // custom fields:
    @Transient
    private boolean isOnline;


    // methods:
    @Override
    public String toString() {
        return "Hero{"
                + "uid=" + uid
                + ", heroName='" + heroName + '\''
                + ", inventoryJson='" + inventoryJson + '\''
                + ", level=" + level
                + ", type=" + type
                + ", power=" + power
                + ", experience=" + experience
                + ", curHealth=" + curHealth
                + ", maxHealth=" + maxHealth
                + ", speed=" + speed
                + ", hurtLevel=" + hurtLevel
                + ", buffsJson='" + buffsJson + '\''
                + ", positionX=" + positionX
                + ", positionY=" + positionY
                + ", inGameTime=" + inGameTime
                + ", worldUid=" + worldUid
                + ", ownerUuid=" + ownerUid
                + '}';
    }
}
