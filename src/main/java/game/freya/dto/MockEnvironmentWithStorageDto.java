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

@Slf4j
@Getter
@Setter
@SuperBuilder
public class MockEnvironmentWithStorageDto extends EnvironmentDto {
    private final BufferedImage[] spriteList;

    @Setter
    @Builder.Default
    private short spriteIndex = 0;

    public MockEnvironmentWithStorageDto(String name, double locationW, double locationY) {
        setName(name);
        setCacheKey("mock_0" + Math.round(1 + getRandom().nextDouble() * 2));
        this.spriteList = Constants.SPRITES_COMBINER.getSprites(getCacheKey(),
                Constants.CACHE.getBufferedImage(getCacheKey()), 1, 1);

        setLocation(new Point2D.Double(getRandom().nextDouble() * locationW, getRandom().nextDouble() * locationY));
        setSize(new Dimension(128, 128));

        setHasCollision(true);
        setVisible(true);
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.drawImage(spriteList[spriteIndex],
                (int) getLocation().x, (int) getLocation().y, getSize().width, getSize().height, null);
    }
}
