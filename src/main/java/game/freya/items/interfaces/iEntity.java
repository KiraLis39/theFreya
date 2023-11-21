package game.freya.items.interfaces;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public interface iEntity {
    BufferedImage getAvatar();

    boolean isDead();

    void hurt(float hurtPoints);

    void heal(float healPoints);

    void attack(iEntity entity);

    void draw(Graphics2D g2D);

    Point2D.Double getPosition();
}
