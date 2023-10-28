package game.freya.players.interfaces;

import java.awt.image.BufferedImage;

public interface iPlayer extends iEntity {
    short MAX_PLAYER_HEALTH = 100;

    BufferedImage getAvatar();

    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);
}
