package game.freya.items;

import game.freya.config.Constants;
import game.freya.items.prototypes.Environment;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MockEnvironmentWithStorage extends Environment {
    private transient Random r = new Random();

    private transient BufferedImage image;

    public MockEnvironmentWithStorage(String name) {
        setName(name);
        setImageNameInCache("mock");

        this.image = (BufferedImage) Constants.CACHE.get(getImageNameInCache());
        setLocation(new Point2D.Double(r.nextDouble() * 1024, r.nextDouble() * 1024));
        setSize(new Dimension(128, 128));

        setHasCollision(true);
        setVisible(true);
    }

    @Override
    public void init() {
        this.image = (BufferedImage) Constants.CACHE.get(getImageNameInCache());
        this.r = new Random();
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (this.image == null) {
            init();
        }
        g2D.drawImage(this.image, (int) getLocation().x, (int) getLocation().y, getSize().width, getSize().height, null);
    }
}
