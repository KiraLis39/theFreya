package game.freya.items.prototypes;

import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import lombok.Getter;

import java.awt.Rectangle;

public abstract class GameCharacter implements iGameObject, iHero {
    @Getter
    private Weapon weapon;

    @Getter
    private Rectangle collider;

    @Override
    public void attack(iEntity entity) {
        entity.hurt(weapon.getAttackPower());
    }
}
