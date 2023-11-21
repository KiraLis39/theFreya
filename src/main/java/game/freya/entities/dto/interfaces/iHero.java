package game.freya.entities.dto.interfaces;

import game.freya.items.interfaces.iEntity;
import game.freya.items.logic.Buff;

import java.util.UUID;

public interface iHero extends iEntity {
    UUID getUid();

    void increaseExp(float increaseValue);

    void decreaseExp(float decreaseValue);

    void addBuff(Buff buff);

    void removeBuff(Buff buff);

    void setLevel(short level);
}
