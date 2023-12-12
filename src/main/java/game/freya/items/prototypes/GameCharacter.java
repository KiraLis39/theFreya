package game.freya.items.prototypes;

import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import lombok.Getter;
import lombok.Setter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public abstract class GameCharacter implements iGameObject, iHero {
    @Getter
    @Setter
    private Weapon weapon;

    @Getter
    @Setter
    private Rectangle collider;

    @Getter
    @Setter
    private Rectangle shape;

    @Setter
    private Dimension size;

    private Point2D.Double location;

    @Override
    public void attack(iEntity entity) {
        entity.hurt(weapon.getAttackPower());
    }

    protected void resetCollider(Point2D position) {
        setShape(new Rectangle((int) position.getX() - getSize().width / 2,
                (int) position.getY() - getSize().height / 2, getSize().width, getSize().height));
        setCollider(new Rectangle(getShape().x - 6, getShape().y - 6, getShape().width + 12, getShape().height + 12));
    }

    @Override
    public void setLocation(double x, double y) {
        if (this.location == null) {
            this.location = new Point2D.Double(x, y);
        }
        this.location.setLocation(x, y);
    }

    @Override
    public Dimension getSize() {
        if (size == null) {
            size = new Dimension(64, 64);
        }
        return size;
    }

    @Override
    public Point2D.Double getLocation() {
        if (location == null) {
            location = new Point2D.Double(256, 256);
        }
        return location;
    }
}
