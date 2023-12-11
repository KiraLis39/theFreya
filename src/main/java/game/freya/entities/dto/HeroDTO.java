package game.freya.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.entities.logic.Buff;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.interfaces.iEntity;
import game.freya.items.PlayedCharacter;
import game.freya.items.containers.Backpack;
import game.freya.items.prototypes.Storage;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.persistence.Transient;
import javax.swing.Icon;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static game.freya.config.Constants.MAP_CELL_DIM;
import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HeroDTO extends PlayedCharacter {
    @NotNull
    @Builder.Default
    private final List<Buff> buffs = new ArrayList<>(9);

    @NotNull
    @Setter
    private UUID heroUid;

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
    private HeroType heroType = HeroType.VOID;

    @Builder.Default
    private float power = 1.0f;

    @Builder.Default
    private long experience = 0;

    @Builder.Default
    private int curHealth = 100;

    @Builder.Default
    private int maxHealth = 100;

    @Builder.Default
    private int curOil = 100;

    @Builder.Default
    private int maxOil = 100;

    @Builder.Default
    private byte speed = 6;

    @Builder.Default
    private Point2D.Double position = new Point2D.Double(384d, 384d);

    @Builder.Default
    private Dimension size = new Dimension(MAP_CELL_DIM, MAP_CELL_DIM);

    @Builder.Default
    private MovingVector vector = MovingVector.UP;

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

    private transient Storage inventory;

    @Setter
    @Builder.Default
    private long inGameTime = 0;

    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Builder.Default
    private boolean isOnline = false;

    @Builder.Default
    private boolean isVisible = true;

    @Transient
    @JsonIgnore
    private transient Image heroViewImage;

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
    public void hurt(float hurtPoints) {
        this.curHealth -= hurtPoints;
        if (this.curHealth < 0) {
            this.curHealth = 0;
        }
        recheckHurtLevel();
    }

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public UUID getUid() {
        return this.heroUid;
    }

    @Override
    public String getName() {
        return this.heroName;
    }

    @Override
    public Dimension getSize() {
        return this.size;
    }

    @Override
    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public boolean hasCollision() {
        return true;
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (getCollider() == null || getShape() == null) {
            resetCollider(position, size);
        }

        if (heroViewImage == null) {
            recolorHeroView();
        }

        AffineTransform tr = g2D.getTransform();
        g2D.rotate(ONE_TURN_PI * vector.ordinal(),
                getShape().x + getShape().width / 2d,
                getShape().y + getShape().height / 2d);

        g2D.drawImage(heroViewImage,
                getShape().x, getShape().y,
                getShape().width, getShape().height, null);
        g2D.setTransform(tr);

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.GREEN);
            g2D.draw(getShape());

            g2D.setColor(Color.RED);
            g2D.draw(getCollider());
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
        log.info("Герою {} добавлен бафф {}", getHeroName(), buff.getName());
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

    public BufferedImage getImage() {
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
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            entity.hurt(power);
        } else {
            log.warn("Player {} can`t attack itself!", getHeroName());
        }
    }

    private void recolorHeroView() {
        int hexColor = (int) Long.parseLong("%02x%02x%02x%02x".formatted(223, // 191
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()), 16);
        heroViewImage = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(((Image) Constants.CACHE.get("player")).getSource(), new RGBImageFilter() {
                    @Override
                    public int filterRGB(final int x, final int y, final int rgb) {
                        return rgb & hexColor;
                    }
                }));
    }

    public void setLevel(short level) {
        this.level = level;
    }

    private void recheckPlayerLevel() {
        this.level = (short) (this.experience / 1000);
    }

    public void setPower(float power) {
        this.power = power;
    }

    private void move(MovingVector vector) {
        this.position.setLocation(position.x + vector.getX(), position.y + vector.getY());
        resetCollider(position, size);
    }

    public Icon getIcon() {
        log.warn("Иконки типов героев ещё не заведены!");
        return null;
    }

    public void move() {
        move(vector);
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
                + "heroUid=" + heroUid
                + ", heroName='" + heroName + '\''
                + ", level=" + level
                + ", experience=" + experience
                + ", maxHealth=" + maxHealth
                + ", speed=" + speed
                + ", position=" + position
                + ", hurtLevel=" + hurtLevel
                + '}';
    }

    public Storage getInventory() {
        if (this.inventory == null) {
            this.inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"),
                    this.heroUid, this.position, this.size, "hero_backpack");
        }
        return this.inventory;
    }

    public void setInventory(Backpack inventory) {
        this.inventory = inventory;
    }

    @Override
    public Point2D.Double getLocation() {
        return this.position;
    }
}
