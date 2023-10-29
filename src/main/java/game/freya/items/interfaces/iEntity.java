package game.freya.items.interfaces;

import java.awt.image.BufferedImage;

public interface iEntity {
    short MAX_HEALTH = 100;

    BufferedImage getAvatar();

    boolean isDead();

    void hurt(float hurtPoints);

    void heal(float healPoints);

    void attack(iEntity entity);
}
