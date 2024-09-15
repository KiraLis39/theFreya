package game.freya.interfaces;

import game.freya.dto.roots.BuffDto;
import game.freya.enums.player.HeroType;
import game.freya.enums.player.MovingVector;
import game.freya.interfaces.subroot.iEntity;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;

public interface iHero extends iEntity {
    HeroType getType();

    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);

    void addBuff(BuffDto buff);

    void removeBuff(BuffDto buff);

    short getLevel();

    LocalDateTime getCreatedDate();

    MovingVector getVector();

    Point2D.Double getLocation();
}
