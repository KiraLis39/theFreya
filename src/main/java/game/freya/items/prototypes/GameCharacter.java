package game.freya.items.prototypes;

import game.freya.interfaces.iEntity;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iHero;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "characters")
public abstract class GameCharacter implements iGameObject, iHero {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;

    @Column(name = "collider", nullable = false)
    private Rectangle collider;

    @Column(name = "shape", nullable = false)
    private Rectangle shape;

    @Column(name = "size", nullable = false)
    private Dimension size;

    @Column(name = "location", nullable = false)
    private Point2D.Double location;

    // созданное игроком оружие в любом случае не удаляется, остаётся для лута или анализа
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.EAGER)
    private Weapon currentWeapon;

    @Override
    public void attack(iEntity entity) {
        entity.hurt(currentWeapon.getAttackPower());
    }

    protected void resetCollider(Point2D position) {
        setShape(new Rectangle((int) position.getX() - getSize().width / 2,
                (int) position.getY() - getSize().height / 2, getSize().width, getSize().height));
        setCollider(new Rectangle(getShape().x + 3, getShape().y + 3, getShape().width - 6, getShape().height - 6));
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
