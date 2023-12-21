package game.freya.items.prototypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.gl.Collider3D;
import game.freya.gl.iCollider;
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
    private transient Collider3D collider;

    protected Storage(String name, UUID author, Point2D.Double location, Dimension size, String imageNameInCache) {
        this.name = name;
        this.author = author;
        this.location = location;
        this.size = size;
        this.imageNameInCache = imageNameInCache;

        this.collider = Collider3D.builder()
                .x(0)
                .y(0)
                .z(0)
                .xw(this.location.x + this.size.width / 2d)
                .yw(this.location.y + this.size.height / 2d)
                .h(1)
                .build();
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
    public UUID getAuthor() {
        return author;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public Point2D.Double getLocation() {
        return location;
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return null;
    }

    @Override
    public String getImageNameInCache() {
        return imageNameInCache;
    }

    @Override
    public iCollider getCollider() {
        if (collider == null) {
            resetCollider();
        }
        return collider;
    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return false;
    }

    private void resetCollider() {
        this.collider = Collider3D.builder()
                .x(location.x)
                .y(location.y)
                .z(0)
                .xw(size.width)
                .yw(size.height)
                .h(1)
                .build();
    }
}
