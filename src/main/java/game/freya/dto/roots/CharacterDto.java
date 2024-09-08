package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.dto.BackpackDto;
import game.freya.enums.player.HeroCorpusType;
import game.freya.enums.player.HeroPeripheralType;
import game.freya.enums.player.HeroType;
import game.freya.enums.player.HurtLevel;
import game.freya.enums.player.MovingVector;
import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import game.freya.utils.ExceptionUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.awt.*;
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
import java.util.UUID;

import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@Getter
@Setter
@SuperBuilder
public class CharacterDto implements iGameObject, iHero {
    @Schema(description = "UUID героя")
    private UUID uid;

    @NotNull
    @Schema(description = "Имя героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Builder.Default
    @Schema(description = "Размеры периферии героя")
    private short peripheralSize = 50;

    @NotNull
    @Builder.Default
    @Schema(description = "Инвентарь героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private BackpackDto inventory = BackpackDto.builder().build();

    @Min(1)
    @Builder.Default
    @Schema(description = "Уровень героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private short level = 1;

    @Builder.Default
    @Schema(description = "Тип корпуса героя")
    private HeroType heroType = HeroType.VOID;

    @Min(0)
    @Builder.Default
    @Schema(description = "Накопленный опыт героя")
    private long experience = 0;

    @Min(0)
    @Builder.Default
    @Schema(description = "Текущее здоровье героя")
    private int health = 100;

    @Min(100)
    @Builder.Default
    @Schema(description = "Максимальное здоровье героя")
    private int maxHealth = 100;

    @Min(0)
    @Builder.Default
    @Schema(description = "Текущий запас масла героя")
    private int oil = 100;

    @Min(100)
    @Builder.Default
    @Schema(description = "Максимальный запас масла героя")
    private int maxOil = 100;

    @Builder.Default
    @Schema(description = "Мощность героя")
    private float power = 1f;

    @Builder.Default
    @Schema(description = "The Player`s hurt level")
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Min(0)
    @Max(18)
    @Builder.Default
    @Schema(description = "Скорость героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private byte speed = 6;

    @NotNull
    @Builder.Default
    @Schema(description = "Вектор направления героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private MovingVector vector = MovingVector.UP;

    @Builder.Default
    @Schema(description = "Главный цвет раскраски корпуса героя")
    private Color baseColor = Color.DARK_GRAY;

    @Builder.Default
    @Schema(description = "Второстепенный цвет раскраски корпуса героя")
    private Color secondColor = Color.ORANGE;

    @NotNull
    @Builder.Default
    @Schema(description = "Тип корпуса героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @NotNull
    @Builder.Default
    @Schema(description = "Тип периферии героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private HeroPeripheralType peripheralType = HeroPeripheralType.COMPACT;

    @Schema(description = "Текущее оружие в руках героя")
    private WeaponDto currentWeapon;

    @NotNull
    @NotEmpty
    @Builder.Default
    @Schema(description = "Бафы наложенные на игрока", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<BuffDto> buffs = new ArrayList<>(9);

    @NotNull
    @Schema(description = "Позиция героя на карте", requiredMode = Schema.RequiredMode.REQUIRED)
    private Point2D.Double location;

    @Min(0)
    @Builder.Default
    @Schema(description = "Время, проведенное в игре")
    private long inGameTime = 0;

    @NotNull
    @Schema(description = "Мир героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID worldUid;

    @NotNull
    @Schema(description = "Игрок создавший героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID ownerUid;

    @NotNull
    @Schema(description = "Создатель героя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID createdBy;

    @NotNull
    @Builder.Default
    @Schema(description = "Дата создания героя")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createDate = LocalDateTime.now();

    @Schema(description = "Дата последнего входа в игру")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastPlayDate;

    @Builder.Default
    @Schema(description = "Is on-line now")
    private boolean isOnline = false;

    @Transient
    @JsonIgnore
    @Schema(description = "The Player`s avatar")
    private Image heroViewImage;

    @Schema(description = "The Player`s real box shape")
    private Rectangle shape;

    @Schema(description = "The Player`s collider")
    private Rectangle collider;

    @Schema(description = "The Player`s dimension")
    private Dimension size;

    @Schema(description = "Image name into cache")
    private String cacheKey;

    @Schema(description = "Is Player visible?")
    private boolean isVisible;

    @Schema(description = "Is Player has collision?")
    private boolean hasCollision;

    @Override
    public void draw(Graphics2D g2D) {
        if (getCollider() == null || getShape() == null) {
            resetCollider(getLocation());
        }

        if (heroViewImage == null) {
            recolorHeroView();
        }

        AffineTransform tr = g2D.getTransform();
        g2D.rotate(ONE_TURN_PI * getVector().ordinal(),
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

            g2D.setColor(Color.YELLOW);
            g2D.fillOval((int) (getCenterPoint().x - 3), (int) (getCenterPoint().y - 3), 6, 6);
        }
    }

    @Override
    public void addBuff(BuffDto buff) {
        log.info("Герою {} добавлен бафф {}", getName(), buff.getName());
        buffs.add(buff);
        buff.activate(this);
    }

    @Override
    public void removeBuff(BuffDto buff) {
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

    private void recolorHeroView() {
        int hexColor = (int) Long.parseLong("%02x%02x%02x%02x".formatted(223, // 191
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()), 16);
        heroViewImage = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(Constants.CACHE.getBufferedImage("player").getSource(), new RGBImageFilter() {
                    @Override
                    public int filterRGB(final int x, final int y, final int rgb) {
                        return rgb & hexColor;
                    }
                }));
    }

    private void move(MovingVector vector) {
        setLocation(getLocation().x + vector.getX(), getLocation().y + vector.getY());
        resetCollider(getLocation());
    }

    public Icon getIcon() {
        log.warn("Иконки типов героев ещё не заведены!");
        return null;
    }

    public void move() {
        move(getVector());
    }

    public void setOnline(boolean b) {
        this.isOnline = b;
    }

    public BackpackDto getInventory() {
        if (this.inventory == null) {
            this.inventory = BackpackDto.builder()
                    .name("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"))
                    .createdBy(getUid())
                    .location(getLocation())
                    .size(getSize())
                    .cacheKey("hero_backpack")
                    .build();
        }
        return this.inventory;
    }

    @Override
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            entity.hurt(currentWeapon == null ? power : currentWeapon.getAttackPower());
        } else {
            log.warn("Player {} can`t attack itself!", getName());
        }
    }

    protected void resetCollider(Point2D position) {
        setShape(new Rectangle((int) position.getX() - getSize().width / 2,
                (int) position.getY() - getSize().height / 2, getSize().width, getSize().height));
        setCollider(new Rectangle(getShape().x + 3, getShape().y + 3, getShape().width - 6, getShape().height - 6));
    }

    @Override
    public void setLocation(double x, double y) {
        if (this.location == null) {
            this.location = new Point2D.Double(x, y);
        }
        this.location.setLocation(x, y);
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

    public void setHealth(int health) {
        this.health = health;
        recheckHurtLevel();
    }

    @Override
    public void heal(float healPoints) {
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", getName());
            return;
        }
        setHealth(getHealth() + (int) healPoints);
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
    public String getName() {
        return name;
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
