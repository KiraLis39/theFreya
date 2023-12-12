package game.freya.interfaces;

import game.freya.entities.logic.Buff;
import game.freya.enums.other.MovingVector;

import java.time.LocalDateTime;

public interface iHero extends iEntity {
    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);

    void addBuff(Buff buff);

    void removeBuff(Buff buff);

    short getLevel();

    LocalDateTime getCreateDate();

    void setVector(MovingVector movingVector);

    void setLocation(double x, double y);
}
