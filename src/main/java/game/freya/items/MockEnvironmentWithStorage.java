package game.freya.items;

import game.freya.config.Constants;
import game.freya.items.prototypes.Environment;
import lombok.Setter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class MockEnvironmentWithStorage extends Environment {
    private transient BufferedImage[] spriteList;

    @Setter
    private short spriteIndex = 0;

    public MockEnvironmentWithStorage(String name) {
        setName(name);
        setImageNameInCache("mock_0" + Math.round(1 + 2 * getR().nextDouble()));

        setLocation(new Point2D.Double(getR().nextDouble() * 1024, getR().nextDouble() * 1024));
        setSize(new Dimension(128, 128));

        setHasCollision(true);
        setVisible(true);
    }

    @Override
    public void init() {
        spriteList = Constants.SPRITES_COMBINER
                .getSprites(getImageNameInCache(), (BufferedImage) Constants.CACHE.get(getImageNameInCache()), 1, 1);
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (spriteList == null) {
            init();
        }
        g2D.drawImage(spriteList[spriteIndex],
                (int) getLocation().x, (int) getLocation().y, getSize().width, getSize().height, null);
    }
}
