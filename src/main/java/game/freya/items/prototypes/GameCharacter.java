package game.freya.items.prototypes;

import game.freya.entities.logic.Buff;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class GameCharacter implements iGameObject, iHero {
    private boolean isOnGround = false;

    private boolean hasCollision = true;

    @Setter
    private UUID ownerUid;

    @Setter
    @Getter
    private UUID characterUid;

    @Getter
    @Setter
    private String characterName;

    @Setter
    @Getter
    private HeroType heroType = HeroType.VOID;

    @Getter
    @Setter
    private Weapon weapon;

    @Getter
    @Setter
    private Rectangle collider;

    @Getter
    @Setter
    private Rectangle shape;

    @Setter
    private Dimension size;

    @Getter
    @Setter
    private float speed = 6;

    @Getter
    @Setter
    private float power = 1.0f;

    @Setter
    private float jumpSpeed;

    @Setter
    private float jumpForce;

    @Setter
    private float jumpAngle;

    private Point2D.Double location;

    private int health = 100;

    @Getter
    @Setter
    private int maxHealth = 100;

    @Setter
    private int oil = 100;

    @Getter
    @Setter
    private int maxOil = 100;

    @Getter
    @Setter
    private int level = 1;

    @Getter
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Getter
    private long experience = 0;

    @Getter
    private MovingVector vector = MovingVector.UP;

    private boolean isLoaded = false;

    @Getter
    @Setter
    private boolean isVisible = true;

    private LocalDateTime createDate = LocalDateTime.now();

    @Getter
    private transient List<Buff> buffs = new ArrayList<>(9);

    @Setter
    private String imageNameInCache;

    public boolean load() {
        isLoaded = true;
        return isLoaded;
    }

    public void setExperience(long experience) {
        if (this.experience == 0) {
            this.experience = experience;
        }
    }

    @Override
    public float getJumpForce() {
        return jumpForce;
    }

    @Override
    public float getAngle() {
        return jumpAngle;
    }

    @Override
    public float getVelocityX() {
        return 0;
    }

    @Override
    public float getVelocityY() {
        return 0;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getOil() {
        return oil;
    }

    @Override
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            if (weapon == null) {
                entity.hurt(getPower());
            } else {
                entity.hurt(weapon.getAttackPower());
            }
        } else {
            log.warn("Player {} can`t attack itself!", getCharacterName());
        }
    }

    @Override
    public void heal(float healPoints) {
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", characterName);
            return;
        }
        this.health += healPoints;
        if (getHealth() > maxHealth) {
            setHealth(maxHealth);
        }
    }

    @Override
    public void hurt(float hurtPoints) {
        this.health -= hurtPoints;
        if (getHealth() < 0) {
            setHealth(0);
        }
    }

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public boolean isWalking() {
        return false;
    }

    @Override
    public boolean isImmortal() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public boolean isOnGround() {
        return isOnGround;
    }

    @Override
    public long lifeTime() {
        return 0;
    }

    public void setHealth(int health) {
        this.health = health;
        recheckHurtLevel();
    }

    protected void resetCollider(Point2D position) {
        setShape(new Rectangle((int) position.getX() - getSize().width / 2,
                (int) position.getY() - getSize().height / 2, getSize().width, getSize().height));
        setCollider(new Rectangle(getShape().x + 3, getShape().y + 3, getShape().width - 6, getShape().height - 6));
    }

    @Override
    public UUID getUid() {
        return characterUid;
    }

    @Override
    public UUID getAuthor() {
        return ownerUid;
    }

    @Override
    public String getName() {
        return characterName;
    }

    @Override
    public Dimension getSize() {
        if (size == null) {
            size = new Dimension(64, 64);
        }
        return size;
    }

    @Override
    public Point2D.Double getLocation() {
        if (location == null) {
            location = new Point2D.Double(256, 256);
        }
        return location;
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(getLocation().x, getLocation().y);
    }

    @Override
    public boolean hasCollision() {
        return hasCollision;
    }

    @Override
    public String getImageNameInCache() {
        return imageNameInCache;
    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return getCollider().intersects(sector);
    }

    // чтобы рассчитать общую мощь рывка?
    public float getAbsVelocity() {
        return Math.abs(this.getVelocityX()) + Math.abs(this.getVelocityY());
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
    public void increaseExp(float increaseValue) {
        if (isDead()) {
            return;
        }
        this.experience += increaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void decreaseExp(float decreaseValue) {
        this.experience -= decreaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void addBuff(Buff buff) {
        log.info("Герою {} добавлен бафф {}", getCharacterName(), buff.getName());
        buffs.add(buff);
        for (Buff b : buffs) {
            b.activate(this);
        }
    }

    @Override
    public void removeBuff(Buff buff) {
        buffs.remove(buff);
        buff.deactivate(this);
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

    @Override
    public void setLocation(double x, double y) {
        if (this.location == null) {
            this.location = new Point2D.Double(x, y);
        }
        this.location.setLocation(x, y);
    }

    private void recheckPlayerLevel() {
        this.level = (short) (this.experience / 1000);
    }

    public void move() {
//        if (this.move) {
//            this.position.x += this.velocity.x * this.game.getDeltaTime();
//            this.position.y += this.velocity.y * this.game.getDeltaTime();
//        }

        // or

        setLocation(getLocation().x + vector.getX(), getLocation().y + vector.getY());
        resetCollider(getLocation());
    }

    public float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

//    public static float getDistance(Vector2 point1, Vector2 point2) {
//        return (float) Math.sqrt((point1x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y));
//    }
}
