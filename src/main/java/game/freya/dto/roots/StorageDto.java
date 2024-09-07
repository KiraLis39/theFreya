package game.freya.dto.roots;

import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iTradeable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@SuperBuilder
public class StorageDto implements iGameObject {
    private final List<iTradeable> content = Collections.synchronizedList(new ArrayList<>());

    private UUID uid;

    private UUID ownerUid;

    @NotNull
    private UUID createdBy;

    @NotNull
    private UUID worldUid;

    @NotNull
    private String name;

    private Dimension size;

    private Rectangle collider;

    private Rectangle shape;

    private Point2D.Double location;

    private boolean isVisible;

    private boolean hasCollision;

    private String cacheKey;

    private LocalDateTime createdDate;

    private LocalDateTime modifyDate;

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
    public void draw(Graphics2D g2D) {

    }

    @Override
    public Rectangle getCollider() {
        if (collider == null) {
            createCollider();
        }
        return collider;
    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return false;
    }

    private void createCollider() {
        this.collider = new Rectangle((int) location.x, (int) location.y, size.width, size.height);
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(location.x + size.width / 2d, location.y + size.height / 2d);
    }

    @Override
    public boolean hasCollision() {
        return hasCollision;
    }

    public iTradeable removeItem(iTradeable item) {
        log.info("Извлечён из инвентаря предмет '{} ({})'", item.getName(), item.getUid());
        return content.remove(content.indexOf(item));
    }

    public void addItem(iTradeable item) {
        log.info("Добавлен в инвентарь предмет '{} ({})'", item.getName(), item.getUid());
        content.add(item);
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
        StorageDto storage = (StorageDto) o;
        return Objects.equals(getName(), storage.getName()) && Objects.equals(getUid(), storage.getUid());
    }
}
