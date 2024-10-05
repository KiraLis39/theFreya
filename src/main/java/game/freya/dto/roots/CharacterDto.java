package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.dto.BackpackDto;
import game.freya.dto.WeaponDto;
import game.freya.enums.player.HeroCorpusType;
import game.freya.enums.player.HeroPeripheralType;
import game.freya.enums.player.HeroType;
import game.freya.enums.player.HurtLevel;
import game.freya.enums.player.MovingVector;
import game.freya.interfaces.iHero;
import game.freya.interfaces.subroot.iEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.List;

import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public abstract class CharacterDto extends AbstractEntityDto implements iHero {
    @Min(1)
    @Builder.Default
    @Schema(description = "Уровень героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private short level = 1;

    @Min(0)
    @Builder.Default
    @Schema(description = "Накопленный опыт героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long experience = 0;

    @Min(0)
    @Builder.Default
    @Schema(description = "Текущее здоровье героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int health = 100;

    @Min(100)
    @Builder.Default
    @Schema(description = "Максимальное здоровье героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int maxHealth = 100;

    @Min(0)
    @Builder.Default
    @Schema(description = "Текущий запас масла героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int oil = 100;

    @Min(100)
    @Builder.Default
    @Schema(description = "Максимальный запас масла героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int maxOil = 100;

    @Min(0)
    @Builder.Default
    @Schema(description = "Мощность героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private float power = 1f;

    @NotNull
    @Builder.Default
    @Schema(description = "The Player`s hurt level", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Min(0)
    @Max(18)
    @Builder.Default
    @Schema(description = "Скорость героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte speed = 6;

    @NotNull
    @Builder.Default
    @Schema(description = "Вектор направления героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MovingVector vector = MovingVector.UP;

    @NotNull
    @Builder.Default
    @Schema(description = "Главный цвет раскраски корпуса героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Color baseColor = Color.DARK_GRAY;

    @NotNull
    @Builder.Default
    @Schema(description = "Второстепенный цвет раскраски корпуса героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Color secondColor = Color.ORANGE;

    @NotNull
    @Builder.Default
    @Schema(description = "Тип корпуса героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @NotNull
    @Builder.Default
    @Schema(description = "Тип периферии героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private HeroPeripheralType peripheralType = HeroPeripheralType.COMPACT;

    @Min(0)
    @Builder.Default
    @Schema(description = "Размеры периферии героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private short peripheralSize = 50;

    @NotNull
    @Builder.Default
    @Schema(description = "Тип корпуса героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private HeroType type = HeroType.VOID;

    @Min(0)
    @Builder.Default
    @Schema(description = "Время, проведенное в игре", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long inGameTime = 0;

    @Builder.Default
    @Schema(description = "Is on-line now", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isOnline = false;

    @Schema(description = "Текущее оружие в руках героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private WeaponDto currentWeapon;

    @NotNull
    @Builder.Default
    @Schema(description = "Инвентарь героя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BackpackDto inventory = BackpackDto.builder().build();

    @NotNull
    @NotEmpty
    @Size(max = 32)
    @Builder.Default
    @Schema(description = "Бафы наложенные на игрока", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<BuffDto> buffs = new ArrayList<>(9);

    @Transient
    @JsonIgnore
    @Schema(description = "The Player`s avatar", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BufferedImage heroViewImage;

    @Override
    public void draw(Graphics2D g2D) {
        if (getCollider() == null || getShape() == null) {
            setCollider(getLocation());
        }

        if (heroViewImage == null) {
            recolorHeroView();
        }

        AffineTransform tr = g2D.getTransform();
        g2D.rotate(ONE_TURN_PI * getVector().ordinal(),
                getShape().x + getShape().width / 2d,
                getShape().y + getShape().height / 2d);

        Rectangle bounds = getShape().getBounds();
        g2D.drawImage(heroViewImage, bounds.x, bounds.y, bounds.width, bounds.height, null);
        g2D.setTransform(tr);

        if (Constants.getGameConfig().isDebugInfoVisible()) {
            g2D.setColor(Color.GREEN);
            g2D.draw(getShape());

            g2D.setColor(Color.RED);
            g2D.draw(getCollider());

            g2D.setColor(Color.YELLOW);
            g2D.fillOval((int) (getLocation().x - 3), (int) (getLocation().y - 3), 6, 6);
        }
    }

    @Override
    public void addBuff(BuffDto buff) {
        log.info("Герою {} добавлен бафф {}", getName(), buff.name());
        buffs.add(buff);
        buff.activate(this);
    }

    @Override
    public void removeBuff(BuffDto buff) {
        buffs.remove(buff);
        buff.deactivate(this);
    }

    public BufferedImage getImage() {
        if (heroViewImage == null) {
            heroViewImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }
        return heroViewImage;
    }

    private void recolorHeroView() {
        int hexColor = (int) Long.parseLong("%02x%02x%02x%02x".formatted(223, // 191
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()), 16);
        FilteredImageSource fiProducer = new FilteredImageSource(
                Constants.CACHE.getBufferedImage("player").getSource(),
                new RGBImageFilter() {
                    @Override
                    public int filterRGB(final int x, final int y, final int rgb) {
                        return rgb & hexColor;
                    }
                });
        heroViewImage = (BufferedImage) Toolkit.getDefaultToolkit().createImage(fiProducer);
    }

    private void move(MovingVector vector) {
        setLocation(getLocation().x + vector.getX(), getLocation().y + vector.getY());
        setCollider(getLocation());
    }

    public Icon getIcon() {
        log.warn("Иконки типов героев ещё не заведены!");
        return null;
    }

    public void move() {
        move(getVector());
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

    private void recheckPlayerLevel() {
        this.level = (short) (this.experience / 1000);
    }

    @Override
    public boolean isDestroyed() {
        return getHealth() <= 0;
    }

    @Override
    public void onDestroy() {
        log.info("Герой '{}' ({}) был уничтожен!", getName(), getUid());
    }
}
