package game.freya.interfaces;

public interface iEntity extends iGameObject {
    int getHealth();

    int getOil();

    void attack(iEntity entity);

    void heal(float healPoints);

    void hurt(float hurtPoints);

    boolean isDead();
}
