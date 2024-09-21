package game.freya.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.dto.roots.EnvironmentDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MockEnvironmentWithStorageDto extends EnvironmentDto {
    @JsonIgnore
    private BufferedImage[] spriteList;

    @Setter
    @JsonIgnore
    @Builder.Default
    private short spriteIndex = 0;

    public MockEnvironmentWithStorageDto(String name, double locationW, double locationY) {
        setName(name);
        setCacheKey("mock_0" + Math.round(1 + Constants.RANDOM.nextDouble() * 2));
        this.spriteList = Constants.SPRITES_COMBINER.getSprites(getCacheKey(),
                Constants.CACHE.getBufferedImage(getCacheKey()), 1, 1);

        setLocation(new Point2D.Double(Constants.RANDOM.nextDouble() * locationW, Constants.RANDOM.nextDouble() * locationY));
        setSize(new Dimension(128, 128));

        setVisible(true);
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (this.spriteList == null && getCacheKey() != null) {
            this.spriteList = Constants.SPRITES_COMBINER.getSprites(getCacheKey(),
                    Constants.CACHE.getBufferedImage(getCacheKey()), 1, 1);
        }

        g2D.drawImage(spriteList[spriteIndex],
                (int) getLocation().x, (int) getLocation().y, getSize().width, getSize().height, null);
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public void onDestroy() {

    }
}
