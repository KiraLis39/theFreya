package game.freya.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.entities.logic.Buff;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
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
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HeroDTO extends PlayedCharacter {
    @NotNull
    @Builder.Default
    private transient List<Buff> buffs = new ArrayList<>(9);

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
    private HeroType heroType = HeroType.VOID;

    @Setter
    private UUID worldUid;

    @Getter
    @Setter
    @Builder.Default
    private LocalDateTime lastPlayDate = LocalDateTime.now();

    private transient Storage inventory;

    @Setter
    @Builder.Default
    private long inGameTime = 0;

    @Builder.Default
    private boolean isOnline = false;

    @Transient
    @JsonIgnore
    private transient Image heroViewImage;

    @Builder
    public HeroDTO(@NotNull UUID heroUid, @NotNull String heroName, short level, int curHealth, int curOil,
                   int maxHealth, int maxOil, float power, HurtLevel hurtLevel, byte speed, MovingVector vector,
                   long experience, UUID ownerUid, String imageNameInCache, LocalDateTime createDate, boolean isVisible
    ) {
        super(heroUid, heroName, level, curHealth, curOil, maxHealth, maxOil, power, hurtLevel, speed, vector,
                experience, ownerUid, imageNameInCache, createDate, isVisible);
        buffs = new ArrayList<>(9);
    }

    @Override
    public boolean hasCollision() {
        return true;
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
                new FilteredImageSource(((Image) Constants.CACHE.get("player")).getSource(), new RGBImageFilter() {
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

    public Storage getInventory() {
        if (this.inventory == null) {
            this.inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"),
                    getHeroUid(), getLocation(), getSize(), "hero_backpack");
        }
        return this.inventory;
    }
}
