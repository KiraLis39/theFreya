package game.freya.interfaces;

import game.freya.entities.logic.Buff;

public interface iHero extends iEntity {
    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);

    void addBuff(Buff buff);

    void removeBuff(Buff buff);

    short getLevel();
}
