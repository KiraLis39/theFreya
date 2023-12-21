package game.freya.interfaces;

import game.freya.gl.iCollider;

import java.awt.Rectangle;

public interface iEntity {

    float getJumpForce();

    float getAngle();

    float getSpeed();

    float getVelocityX();

    float getVelocityY();

    int getHealth();

    int getOil();

    void attack(iEntity entity);

    void heal(float healPoints);

    void hurt(float hurtPoints);

    boolean isDead();

    iCollider getCollider();

    void setCollider(iCollider collider);

    Rectangle getShape();

    void setShape(Rectangle shape);

    boolean isWalking();

    boolean isImmortal();

    boolean isLoaded();

    boolean isOnGround();

    long lifeTime(); // время жизни (для мобов)
}
