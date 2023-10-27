package game.freya.players.interfaces;

import java.awt.image.BufferedImage;

public interface iPlayer extends iEntity {
    BufferedImage getAvatar();

    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);
}
