package game.freya.items.prototypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorable;
import lombok.Getter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
public abstract class Storage implements iGameObject, iStorable {
    @Getter
    private final UUID uid = UUID.randomUUID();

    @Getter
    private final String name;

    private final UUID author;

    private final Point2D.Double location;

    private final Dimension size;

    private final String imageNameInCache;

    @JsonIgnore
    private transient Rectangle collider;

    protected Storage(String name, UUID author, Point2D.Double location, Dimension size, String imageNameInCache) {
        this.name = name;
        this.author = author;
        this.location = location;
        this.size = size;
        this.imageNameInCache = imageNameInCache;

        this.collider = new Rectangle((int) (this.location.x + this.size.width / 2d), (int) (this.location.y + this.size.height / 2d));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Storage storage = (Storage) o;
        return Objects.equals(getName(), storage.getName()) && Objects.equals(getUid(), storage.getUid());
    }

    @Override
    public String toString() {
        return "Storage{"
                + "uid=" + uid
                + ", name='" + name + '\''
                + ", author=" + author
                + ", location=" + location
                + ", size=" + size
                + ", imageNameInCache='" + imageNameInCache + '\''
                + ", collider=" + collider
                + '}';
    }

    @Override
    public UUID getCreator() {
        return author;
    }

    @Override
    public Point2D.Double getLocation() {
        return location;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public String getImageNameInCache() {
        return imageNameInCache;
    }

    @Override
    public Rectangle getCollider() {
        if (collider == null) {
            resetCollider();
        }
        return collider;
    }

    private void resetCollider() {
        this.collider = new Rectangle((int) location.x, (int) location.y, size.width, size.height);
    }
}
