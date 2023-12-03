package game.freya.entities.dto;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iHero;
import game.freya.enums.HeroCorpusType;
import game.freya.enums.HeroPeriferiaType;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HeroDTO implements iHero {
    @NotNull
    @Builder.Default
    private final List<Buff> buffs = new ArrayList<>(9);

    @NotNull
    @Setter
    private UUID uid;

    @NotNull
    private String heroName;

    @Setter
    private Color baseColor;

    @Setter
    private Color secondColor;

    @Setter
    @NotNull
    @Builder.Default
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @Setter
    @NotNull
    @Builder.Default
    private HeroPeriferiaType periferiaType = HeroPeriferiaType.COMPACT;

    @Setter
    @Builder.Default
    private short periferiaSize = 50;

    @Builder.Default
    private short level = 1;

    @Builder.Default
    private HeroType type = HeroType.VOID;

    @Builder.Default
    private float power = 1.0f;

    @Builder.Default
    private float experience = 0f;

    @Builder.Default
    private short curHealth = 100;

    @Builder.Default
    private short maxHealth = 100;

    @Builder.Default
    private short curOil = 100;

    @Builder.Default
    private short maxOil = 100;

    @Builder.Default
    private byte speed = 6;

    @Builder.Default
    private Point2D.Double position = new Point2D.Double(384d, 384d);

    @Builder.Default
    private MovingVector vector = MovingVector.UP;

    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Setter
    private UUID worldUid;

    @Setter
    private UUID ownerUid;

    @Getter
    @Builder.Default
    private LocalDateTime createDate = LocalDateTime.now();

    @Getter
    @Setter
    @Builder.Default
    private LocalDateTime lastPlayDate = LocalDateTime.now();

    @Builder.Default
    private Backpack inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"));

    @Setter
    @Builder.Default
    private long inGameTime = 0;

    @Getter
    @Builder.Default
    private boolean isOnline = false;

    private BufferedImage image;

    private void recheckHurtLevel() {
        if (this.curHealth <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
            decreaseExp(getExperience() - getExperience() * 0.1f); // -10% exp by death
        } else if (this.curHealth <= maxHealth * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.curHealth <= maxHealth * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.curHealth <= maxHealth * 0.9f) {
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
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public void hurt(float hurtPoints) {
        this.curHealth -= hurtPoints;
        if (this.curHealth < 0) {
            this.curHealth = 0;
        }
        recheckHurtLevel();
    }

    @Override
    public void heal(float healPoints) {
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", getHeroName());
            return;
        }
        this.curHealth += healPoints;
        if (this.curHealth > maxHealth) {
            this.curHealth = maxHealth;
        }
        recheckHurtLevel();
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
    public void draw(Graphics2D g2D) {
        Shape playerShape = new Ellipse2D.Double(
                (int) this.position.x - Constants.MAP_CELL_DIM / 3d,
                (int) this.position.y - Constants.MAP_CELL_DIM / 3d,
                Constants.MAP_CELL_DIM / 1.5d, Constants.MAP_CELL_DIM / 1.5d);

        if (image == null) {
            BufferedImage pre = (BufferedImage) Constants.CACHE.get("player_0");
            image = new BufferedImage(pre.getWidth(), pre.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D i2D = image.createGraphics();
            Constants.RENDER.setRender(i2D, FoxRender.RENDER.HIGH,
                    Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());
            i2D.drawImage(pre, 0, 0, null);
            i2D.dispose();
        }

        AffineTransform tr = g2D.getTransform();
        g2D.rotate(ONE_TURN_PI * vector.ordinal(),
                playerShape.getBounds().x + playerShape.getBounds().width / 2d,
                playerShape.getBounds().y + playerShape.getBounds().height / 2d);

        g2D.drawImage(image,
                playerShape.getBounds().x, playerShape.getBounds().y,
                playerShape.getBounds().width, playerShape.getBounds().height, null);
        g2D.setTransform(tr);
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

    private void recheckPlayerLevel() {
        // level
    }

    public void setPower(float power) {
        this.power = power;
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
            case UP_RIGHT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveUp();
                    moveRight();
                }
            }
            case RIGHT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveRight();
                }
            }
            case RIGHT_DOWN -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveRight();
                    moveDown();
                }
            }
            case DOWN -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveDown();
                }
            }
            case DOWN_LEFT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveDown();
                    moveLeft();
                }
            }
            case LEFT -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveLeft();
                }
            }
            case LEFT_UP -> {
                for (int i = 0; i < getSpeed(); i++) {
                    moveLeft();
                    moveUp();
                }
            }
            default -> log.warn("Неизвестное направление вектора героя {}", vector);
        }
    }

    public void setVector(MovingVector movingVector) {
        this.vector = movingVector;
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public void setOnline(boolean b) {
        this.isOnline = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeroName(), getWorldUid(), getOwnerUid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeroDTO heroDTO = (HeroDTO) o;
        return Objects.equals(getHeroName(), heroDTO.getHeroName())
                && Objects.equals(getWorldUid(), heroDTO.getWorldUid()) && Objects.equals(getOwnerUid(), heroDTO.getOwnerUid());
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

    public void setInventory(Backpack inventory) {
        this.inventory = inventory;
    }
}
