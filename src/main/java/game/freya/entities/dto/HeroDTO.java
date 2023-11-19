package game.freya.entities.dto;

import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iHero;
import game.freya.enums.HeroType;
import game.freya.enums.HurtLevel;
import game.freya.enums.MovingVector;
import game.freya.items.containers.Backpack;
import game.freya.items.interfaces.iEntity;
import game.freya.items.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HeroDTO implements iHero {
    @NotNull
    @Setter
    private UUID uid;

    @NotNull
    private String heroName;

    @Setter
    @Builder.Default
    private Backpack inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"));

    @Builder.Default
    private short level = 1;

    @Builder.Default
    private HeroType type = HeroType.VOID;

    @Builder.Default
    private float currentAttackPower = 1.0f;

    @Builder.Default
    private float experience = 0f;

    @Builder.Default
    private short health = 100;

    @Builder.Default
    private short maxHealth = 100;

    @Builder.Default
    private short speed = 6;

    @Builder.Default
    private Point2D.Double position = new Point2D.Double(384d, 384d);

    @Builder.Default
    private MovingVector vector = MovingVector.NONE;

    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Builder.Default
    private List<Buff> buffs = new ArrayList<>(9);

    @Setter
    private UUID worldUid;

    @Setter
    private PlayerDTO ownedPlayer;

    @Getter
    @Builder.Default
    private LocalDateTime createDate = LocalDateTime.now();

    @Setter
    @Builder.Default
    private long inGameTime = 0;

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public void hurt(float hurtPoints) {
        this.health -= hurtPoints;
        if (this.health < 0) {
            this.health = 0;
        }
        recheckHurtLevel();
    }

    @Override
    public void heal(float healPoints) {
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", getHeroName());
            return;
        }
        this.health += healPoints;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
        recheckHurtLevel();
    }

    @Override
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            entity.hurt(currentAttackPower);
        } else {
            log.warn("Player {} can`t attack itself!", getHeroName());
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        Shape playerShape = new Ellipse2D.Double(
                (int) this.position.x - Constants.MAP_CELL_DIM / 4d,
                (int) this.position.y - Constants.MAP_CELL_DIM / 4d,
                Constants.MAP_CELL_DIM / 2d, Constants.MAP_CELL_DIM / 2d);

        // draw shadow:
        g2D.setColor(new Color(0, 0, 0, 36));
        g2D.fillOval(playerShape.getBounds().x + 3, playerShape.getBounds().y + 6,
                playerShape.getBounds().width, playerShape.getBounds().height);

        // fill body:
        g2D.setColor(Color.GREEN);
        g2D.fill(playerShape);

        // draw border:
//        g2D.setStroke(new BasicStroke(2f));
        g2D.setColor(Color.ORANGE);
        g2D.draw(playerShape);
    }

    private void recheckHurtLevel() {
        if (this.health <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
            decreaseExp(getExperience() - getExperience() * 0.1f); // -10% exp by death
        } else if (this.health <= maxHealth * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.health <= maxHealth * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.health <= maxHealth * 0.9f) {
            this.hurtLevel = HurtLevel.LIGHT_HURT;
        } else {
            this.hurtLevel = HurtLevel.HEALTHFUL;
        }
    }

    @Override
    public BufferedImage getAvatar() {
        try (InputStream avatarResource = getClass().getResourceAsStream(Constants.DEFAULT_AVATAR_URL)) {
            if (avatarResource != null) {
                return ImageIO.read(avatarResource);
            }
            throw new IOException(Constants.DEFAULT_AVATAR_URL);
        } catch (IOException e) {
            log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
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
    public void setLevel(short level) {
        this.level = level;
    }

    @Override
    public PlayerDTO getOwnedPlayer() {
        return this.ownedPlayer;
    }

    private void recheckPlayerLevel() {
        // level
    }

    public void setCurrentAttackPower(float currentAttackPower) {
        this.currentAttackPower = currentAttackPower;
    }

    private void moveUp() {
        this.position.setLocation(position.x, position.y - 1);
    }

    private void moveDown() {
        this.position.setLocation(position.x, position.y + 1);
    }

    private void moveLeft() {
        this.position.setLocation(position.x - 1, position.y);
    }

    private void moveRight() {
        this.position.setLocation(position.x + 1, position.y);
    }

    @Override
    public String toString() {
        return "HeroDTO{"
                + "uid=" + uid
                + ", heroName='" + heroName + '\''
                + ", level=" + level
                + ", experience=" + experience
                + ", maxHealth=" + maxHealth
                + ", speed=" + speed
                + ", position=" + position
                + ", hurtLevel=" + hurtLevel
                + '}';
    }

    public Icon getIcon() {
        log.warn("Иконки типов героев ещё не заведены!");
        return null;
    }

    public void move() {
        switch (vector) {
            case UP -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveUp();
                }
            }
            case DOWN -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveDown();
                }
            }
            case LEFT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveLeft();
                }
            }
            case RIGHT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveRight();
                }
            }
            case NONE -> {}
            default -> log.warn("Неизвестное направление вектора героя {}", vector);
        }
    }

    public void setVector(MovingVector movingVector) {
        this.vector = movingVector;
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }
}
