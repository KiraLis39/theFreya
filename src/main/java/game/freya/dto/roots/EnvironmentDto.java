package game.freya.dto.roots;

import game.freya.interfaces.iEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public class EnvironmentDto implements iEnvironment {
    @Getter
    private static final Random random = new Random();

    private UUID uid;
    private UUID createdBy;
    private String name;
    private Dimension size;
    private Point2D.Double location;
    private Rectangle collider;
    private boolean isVisible;
    private boolean hasCollision;
    private String cacheKey;

    protected void setSize(Dimension size) {
        this.size = size;
        if (this.location != null) {
            setCollider();
        }
    }

    protected void setLocation(Point2D.Double location) {
        this.location = location;
        if (this.size != null) {
            setCollider();
        }
    }

    private void setCollider() {
        this.collider = new Rectangle((int) location.x + 16, (int) location.y + 16, size.width - 32, size.height - 32);
    }

    @Override
    public Rectangle getCollider() {
        if (collider == null && location != null && size != null) {
            setCollider();
        }
        return collider;
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(getLocation().x + getSize().width / 2d, getLocation().y + getSize().height / 2d);
    }

    @Override
    public boolean hasCollision() {
        return hasCollision;
    }

    @Override
    public void draw(Graphics2D g2D) {

    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return sector.contains(getCenterPoint());
    }
}
