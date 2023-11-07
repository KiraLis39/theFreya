package game.freya.entities.dto.interfaces;

import game.freya.items.interfaces.iEntity;
import game.freya.items.logic.Buff;

public interface iPlayer extends iEntity {
    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);

    void addBuff(Buff buff);

    void removeBuff(Buff buff);

    void setLevel(short level);
}
