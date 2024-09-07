package game.freya.dto;

import game.freya.config.Constants;
import game.freya.dto.roots.EnvironmentDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.UUID;

@Slf4j
@Getter
@SuperBuilder
public class MockEnvironmentWithStorageDto extends EnvironmentDto {
    @Builder.Default
    private BufferedImage[] spriteList = Constants.SPRITES_COMBINER.getSprites(getCacheKey(),
            Constants.CACHE.getBufferedImage(getCacheKey()), 1, 1);

    @Setter
    @Builder.Default
    private short spriteIndex = 0;

    public MockEnvironmentWithStorageDto(String name, double locationW, double locationY) {
        setName(name);
        setCacheKey("mock_0" + Math.round(1 + getRandom().nextDouble() * 2));

        setLocation(new Point2D.Double(getRandom().nextDouble() * locationW, getRandom().nextDouble() * locationY));
        setSize(new Dimension(128, 128));

        setHasCollision(true);
        setVisible(true);
    }

    @Override
    public UUID getUid() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Dimension getSize() {
        return null;
    }

    @Override
    public Point2D.Double getLocation() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public String getCacheKey() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.drawImage(spriteList[spriteIndex],
                (int) getLocation().x, (int) getLocation().y, getSize().width, getSize().height, null);
    }
}
