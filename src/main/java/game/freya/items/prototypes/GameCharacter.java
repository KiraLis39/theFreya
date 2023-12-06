package game.freya.items.prototypes;

import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;

public abstract class GameCharacter implements iGameObject, iHero {
    private Weapon weapon;

    @Override
    public void attack(iEntity entity) {
        entity.hurt(weapon.getAttackPower());
    }
}
