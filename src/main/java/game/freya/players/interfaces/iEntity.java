package game.freya.players.interfaces;

public interface iEntity {
    boolean isDead();

    void hurt(float hurtPoints);

    void heal(float healPoints);

    void attack(iEntity entity);
}
