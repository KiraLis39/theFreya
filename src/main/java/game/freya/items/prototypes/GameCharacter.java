package game.freya.items.prototypes;

import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import lombok.Getter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public abstract class GameCharacter implements iGameObject, iHero {
    @Getter
    private Weapon weapon;

    private Rectangle collider;

    private Rectangle shape;

    @Override
    public void attack(iEntity entity) {
        entity.hurt(weapon.getAttackPower());
    }

    @Override
    public void setCollider(Rectangle collider) {
        this.collider = collider;
    }

    @Override
    public Rectangle getShape() {
        return this.shape;
    }

    @Override
    public Rectangle getCollider() {
        return this.collider;
    }

    @Override
    public void setShape(Rectangle shape) {
        this.shape = shape;
    }

    protected void resetCollider(Point2D position, Dimension size) {
        setShape(new Rectangle((int) position.getX() - size.width / 2, (int) position.getY() - size.height / 2, size.width, size.height));
        setCollider(new Rectangle(getShape().x - 6, getShape().y - 6, getShape().width + 12, getShape().height + 12));
    }
}
