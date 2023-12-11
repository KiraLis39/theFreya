package game.freya.interfaces;

import java.awt.Rectangle;

public interface iEntity {
    int getHealth();

    int getOil();

    void attack(iEntity entity);

    void heal(float healPoints);

    void hurt(float hurtPoints);

    boolean isDead();

    Rectangle getCollider();

    void setCollider(Rectangle collider);

    Rectangle getShape();

    void setShape(Rectangle shape);
}
