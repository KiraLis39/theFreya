package game.freya.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.entities.logic.Buff;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeripheralType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.items.PlayedCharacter;
import game.freya.items.containers.Backpack;
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
@SuperBuilder
public class HeroDTO extends PlayedCharacter {
    @Schema(description = "UUID героя")
    private UUID uid;

    @NotNull
    @Schema(description = "Имя героя")
    private String heroName;

    @Setter
    @Builder.Default
    @Schema(description = "Главный цвет раскраски корпуса героя")
    private Color baseColor = Color.DARK_GRAY;

    @Setter
    @Builder.Default
    @Schema(description = "Второстепенный цвет раскраски корпуса героя")
    private Color secondColor = Color.ORANGE;

    @Setter
    @NotNull
    @Builder.Default
    @Schema(description = "Тип корпуса героя")
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @Setter
    @NotNull
    @Builder.Default
    @Schema(description = "Тип периферии героя")
    private HeroPeripheralType peripheralType = HeroPeripheralType.COMPACT;

    @Setter
    @Builder.Default
    @Schema(description = "Размеры периферии героя")
    private short peripheralSize = 50;

    @Builder.Default
    @Schema(description = "Инвентарь героя")
    private Backpack inventory = Backpack.builder().build();

    @Builder.Default
    @Schema(description = "Уровень героя")
    private short level = 1;

    @Builder.Default
    @Schema(description = "Тип корпуса героя")
    private HeroType heroType = HeroType.VOID;

    @Builder.Default
    @Schema(description = "Мощность героя")
    private float power = 1f;

    @Builder.Default
    @Schema(description = "Накопленный опыт героя")
    private long experience = 0;

    @Builder.Default
    @Schema(description = "Текущее здоровье героя")
    private int curHealth = 100;

    @Builder.Default
    @Schema(description = "Максимальное здоровье героя")
    private int maxHealth = 100;

    @Builder.Default
    @Schema(description = "Текущий запас масла героя")
    private int curOil = 100;

    @Builder.Default
    @Schema(description = "Максимальный запас масла героя")
    private int maxOil = 100;

    @Builder.Default
    @Schema(description = "Скорость героя")
    private byte speed = 6;

    @NotNull
    @NotEmpty
    @Builder.Default
    private List<Buff> buffs = new ArrayList<>(9);

    @Schema(description = "Позиция героя на карте")
    private Point2D.Double location;

    @Setter
    @Builder.Default
    @Schema(description = "Время, проведенное в игре")
    private long inGameTime = 0;

    @Setter
    @Schema(description = "Мир героя")
    private UUID worldUid;

    @Schema(description = "Создатель героя")
    private UUID ownerUid;

    @Builder.Default
    @Schema(description = "Дата создания героя")
    private LocalDateTime createDate = LocalDateTime.now();

    @Getter
    @Setter
    @Builder.Default
    @Schema(description = "Дата последнего входа в игру")
    private LocalDateTime lastPlayDate = LocalDateTime.now();

    @Builder.Default
    @Schema(description = "Is on-line now")
    private boolean isOnline = false;

    @Builder.Default
    @Schema(description = "The Player`s hurt level")
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Transient
    @JsonIgnore
    @Schema(description = "The Player`s avatar")
    private Image heroViewImage;

    @Override
    public boolean hasCollision() {
        return true;
    }

    @Override
    public String getCacheKey() {
        return null;
    }

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

    public Backpack getInventory() {
        if (this.inventory == null) {
            this.inventory = Backpack.builder()
                    .name("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"))
                    .createdBy(getHeroUid())
                    .location(getLocation())
                    .size(getSize())
                    .cacheKey("hero_backpack")
                    .build();
        }
        return this.inventory;
    }
}
