package game.freya.items.prototypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "hero_storages")
public abstract class Storage implements iGameObject, iStorable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @NotNull
    @Column(name = "owner_uid", nullable = false)
    private UUID ownerUid;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "location")
    private Point2D.Double location;

    @Column(name = "size")
    private Dimension size;

    @Column(name = "cache_key")
    private String cacheKey;

    @Builder.Default
    @Column(name = "is_visible")
    private boolean isVisible = true;

    @JsonIgnore
    private transient Rectangle collider;

    public Storage(String name, UUID createdBy, Point2D.Double location, Dimension size, String cacheKey) {
        this.name = name;
        this.createdBy = createdBy;
        this.location = location;
        this.size = size;
        this.cacheKey = cacheKey;
    }

    @Override
    public UUID getAuthor() {
        return createdBy;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
        if (this.location != null) {
            this.collider = new Rectangle((int) (this.location.x + this.size.width / 2d), (int) (this.location.y + this.size.height / 2d));
        }
    }

    @Override
    public Point2D.Double getLocation() {
        return location;
    }

    public void setLocation(Point2D.Double location) {
        this.location = location;
        if (this.size != null) {
            this.collider = new Rectangle((int) (this.location.x + this.size.width / 2d), (int) (this.location.y + this.size.height / 2d));
        }
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public Rectangle getCollider() {
        if (collider == null) {
            createCollider();
        }
        return collider;
    }

    private void createCollider() {
        this.collider = new Rectangle((int) location.x, (int) location.y, size.width, size.height);
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(location.x + size.width / 2d, location.y + size.height / 2d);
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
}
