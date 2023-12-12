package game.freya.items.prototypes;

import game.freya.interfaces.iEnvironment;
import lombok.Getter;
import lombok.Setter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.Serial;
import java.util.Random;
import java.util.UUID;

public abstract class Environment implements iEnvironment {
    @Serial
    private static final long serialVersionUID = 2;

    @Getter
    private static final Random r = new Random();

    @Setter
    private UUID uuid;

    @Setter
    private UUID author;

    @Setter
    private String name;

    private Dimension size;

    private Point2D.Double location;

    private Rectangle collider;

    @Setter
    private boolean isVisible;

    @Setter
    private boolean hasCollision;

    @Setter
    private String imageNameInCache;

    @Override
    public UUID getUid() {
        return this.uuid;
    }

    @Override
    public UUID getAuthor() {
        return this.author;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Dimension getSize() {
        return this.size;
    }

    @Override
    public Point2D.Double getLocation() {
        return this.location;
    }

    protected void setLocation(Point2D.Double location) {
        this.location = location;
        if (this.size != null) {
            resetCollider();
        }
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(getLocation().x + getSize().width / 2d, getLocation().y + getSize().height / 2d);
    }

    @Override
    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public boolean hasCollision() {
        return this.hasCollision;
    }

    @Override
    public String getImageNameInCache() {
        return this.imageNameInCache;
    }

    @Override
    public Rectangle getCollider() {
        if (collider == null && location != null && size != null) {
            resetCollider();
        }
        return collider;
    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return sector.contains(getCenterPoint());
    }

    protected void setSize(Dimension size) {
        this.size = size;
        if (this.location != null) {
            resetCollider();
        }
    }

    public abstract void init();

    private void resetCollider() {
        this.collider = new Rectangle((int) location.x + 16, (int) location.y + 16, size.width - 32, size.height - 32);
    }
}
