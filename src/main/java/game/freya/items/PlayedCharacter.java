package game.freya.items;

import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.interfaces.iEntity;
import game.freya.items.prototypes.GameCharacter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("played")
public abstract class PlayedCharacter extends GameCharacter {
    @NotNull
    private UUID createdBy;

    @NotNull
    @Setter
    @Getter
    private UUID heroUid;

    @Setter
    @Getter
    @NotNull
    private String heroName;

    @Getter
    @Setter
    @Builder.Default
    private short level = 1;

    @Builder.Default
    private int health = 100;

    @Setter
    @Builder.Default
    private int oil = 100;

    @Getter
    @Setter
    @Builder.Default
    private int maxHealth = 100;

    @Getter
    @Setter
    @Builder.Default
    private int maxOil = 100;

    @Getter
    @Setter
    @Builder.Default
    private float power = 1.0f;

    @Getter
    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Getter
    @Setter
    @Builder.Default
    private byte speed = 6;

    @Getter
    @Builder.Default
    private MovingVector vector = MovingVector.UP;

    @Getter
    @Builder.Default
    private long experience = 0;

    @Setter
    private UUID ownerUid;

    @Setter
    private String cacheKey;

    @Builder.Default
    private LocalDateTime createDate = LocalDateTime.now();

    @Getter
    @Setter
    @Builder.Default
    private boolean isVisible = true;

    @Override
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        recheckHurtLevel();
    }

    @Override
    public int getOil() {
        return oil;
    }

    @Override
    public void heal(float healPoints) {
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", heroName);
            return;
        }
        this.health += (int) healPoints;
        if (getHealth() > maxHealth) {
            setHealth(maxHealth);
        }
    }

    @Override
    public void hurt(float hurtPoints) {
        this.health -= (int) hurtPoints;
        if (getHealth() < 0) {
            setHealth(0);
        }
    }

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    private void recheckHurtLevel() {
        if (getHealth() <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
            decreaseExp(experience - experience * 0.1f); // -10% exp by death
        } else if (getHealth() <= maxHealth * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (getHealth() <= maxHealth * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (getHealth() <= maxHealth * 0.9f) {
            this.hurtLevel = HurtLevel.LIGHT_HURT;
        } else {
            this.hurtLevel = HurtLevel.HEALTHFUL;
        }
    }

    @Override
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            entity.hurt(power);
        } else {
            log.warn("Player {} can`t attack itself!", getHeroName());
        }
    }

    @Override
    public UUID getUid() {
        return this.heroUid;
    }

    @Override
    public UUID getAuthor() {
        return ownerUid;
    }

    public void setAuthor(UUID authorUid) {
        ownerUid = authorUid;
    }

    @Override
    public String getName() {
        return this.heroName;
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(getLocation().x, getLocation().y);
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return getCollider().intersects(sector);
    }

    @Override
    public void increaseExp(float increaseValue) {
        if (isDead()) {
            return;
        }
        this.experience += (long) increaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void decreaseExp(float decreaseValue) {
        this.experience -= (long) decreaseValue;
        recheckPlayerLevel();
    }

    @Override
    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        if (this.createDate == null) {
            this.createDate = createDate;
        }
    }

    @Override
    public void setVector(MovingVector movingVector) {
        this.vector = movingVector;
    }

    private void recheckPlayerLevel() {
        this.level = (short) (this.experience / 1000);
    }

    public void setExperience(long experience) {
        if (this.experience == 0) {
            this.experience = experience;
        }
    }
}
