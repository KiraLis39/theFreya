package game.freya.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.enums.other.MovingVector;
import game.freya.items.prototypes.GameCharacter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.time.LocalDateTime;
import java.util.UUID;

import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@AllArgsConstructor
@RequiredArgsConstructor
public class PlayedCharacter extends GameCharacter {
    @JsonIgnore
    private transient Image heroViewImage;

    @Setter
    @Getter
    private Color baseColor;

    @Setter
    @Getter
    private Color secondColor;

    @Getter
    private boolean isOnline;

    protected PlayedCharacter(UUID ownerUid, UUID heroUid, String heroName) {
        this(ownerUid, heroUid, heroName, 1, 100, 100, 100, 100, 1f, 6, MovingVector.UP,
                0, null, LocalDateTime.now(), true);
    }

    protected PlayedCharacter(
            UUID ownerUid,
            UUID heroUid,
            String heroName,
            int level,
            int curHealth,
            int curOil,
            int maxHealth,
            int maxOil,
            float power,
            int speed,
            MovingVector vector,
            long experience,
            String imageNameInCache,
            LocalDateTime createDate,
            boolean isVisible
    ) {
        setOwnerUid(ownerUid);
        setCharacterUid(heroUid);
        setCharacterName(heroName);
        setLevel(level);
        setHealth(curHealth);
        setOil(curOil);
        setMaxHealth(maxHealth);
        setMaxOil(maxOil);
        setPower(power);
        setSpeed(speed);
        setVector(vector);
        setExperience(experience);
        setImageNameInCache(imageNameInCache);
        setCreateDate(createDate);
        setVisible(isVisible);
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (!isOnline) {
            return;
        }

        if (heroViewImage == null) {
            recolorHeroView();
        }

        if (getCollider() == null || getShape() == null) {
            resetCollider(getLocation());
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
            g2D.draw(getCollider().getShape());

            g2D.setColor(Color.YELLOW);
            g2D.fillOval((int) (getCenterPoint().x - 3), (int) (getCenterPoint().y - 3), 6, 6);
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

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Icon getIcon() {
        log.warn("Иконки типов героев ещё не заведены!");
        return null;
    }
}
