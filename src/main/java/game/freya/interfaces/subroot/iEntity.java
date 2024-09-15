package game.freya.interfaces.subroot;

import game.freya.interfaces.root.iDestroyable;

public interface iEntity extends iDestroyable {
    int getHealth();

    int getOil();

    void attack(iEntity entity);

    void heal(float healPoints);

    void hurt(float hurtPoints);

    boolean isDead();
}
