package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public class StorageDto implements iGameObject, iStorage {

    @Schema(description = "uid of container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @Schema(description = "Storage owner`s uid", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID ownerUid;

    @NotNull
    @Schema(description = "Uuid of container creator", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID createdBy;

    @NotNull
    @Schema(description = "Uuid of owned World", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID worldUid;

    @NotNull
    @Schema(description = "Name of container", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "The capacity of container", requiredMode = Schema.RequiredMode.REQUIRED)
    private short capacity;

    @Builder.Default
    @Schema(description = "The visible size of container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Dimension size = new Dimension(128, 64);

    @Schema(description = "Collider of this container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle collider;

    @Schema(description = "Rigid body of this container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle shape;

    @Builder.Default
    @Schema(description = "World location of this container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Point2D.Double location = new Point2D.Double(0, 0);

    @Builder.Default
    @JsonProperty("isVisible")
    @Schema(description = "Is container is visible?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isVisible = true;

    @Builder.Default
    @Schema(description = "Is container has collision with other objects?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean hasCollision = true;

    @Builder.Default
    @Schema(description = "Cached image name of container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cacheKey = "no_image";

    @Builder.Default
    @Schema(description = "Container`s content items array", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ItemDto> items = Collections.synchronizedList(new ArrayList<>());

    @Builder.Default
    @Schema(description = "Creation date of this container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    @Schema(description = "Modification date of this container", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime modifyDate = LocalDateTime.now();

    @JsonIgnore
    public void setSize(Dimension size) {
        this.size = size;
        if (this.location != null) {
            this.collider = new Rectangle((int) (this.location.x + this.size.width / 2d), (int) (this.location.y + this.size.height / 2d));
        }
    }

    @JsonIgnore
    public void setLocation(Point2D.Double location) {
        this.location = location;
        if (this.size != null) {
            this.collider = new Rectangle((int) (this.location.x + this.size.width / 2d), (int) (this.location.y + this.size.height / 2d));
        }
    }

    @Override
    @JsonIgnore
    public void draw(Graphics2D g2D) {

    }

    @Override
    @JsonIgnore
    public Rectangle getCollider() {
        if (collider == null) {
            createCollider();
        }
        return collider;
    }

    @Override
    @JsonIgnore
    public boolean isInSector(Rectangle sector) {
        return sector.contains(this.location);
    }

    @JsonIgnore
    private void createCollider() {
        this.collider = new Rectangle((int) location.x, (int) location.y, size.width, size.height);
    }

    @Override
    @JsonIgnore
    public Point2D.Double getCenterPoint() {
        return new Point2D.Double(location.x + size.width / 2d, location.y + size.height / 2d);
    }

    @Override
    @JsonIgnore
    public boolean hasCollision() {
        return hasCollision;
    }

    @JsonIgnore
    public ItemDto removeItem(ItemDto item) {
        log.info("Извлечён из инвентаря предмет '{} ({})'", item.getName(), item.getUid());
        return items.remove(items.indexOf(item));
    }

    @JsonIgnore
    public void addItem(ItemDto item) {
        log.info("Добавлен в инвентарь предмет '{} ({})'", item.getName(), item.getUid());
        items.add(item);
    }

    @Override
    @JsonIgnore
    public void put(ItemDto storable) {
        if (items.size() < capacity) {
            items.add(storable);
        }
    }

    @Override
    @JsonIgnore
    public ItemDto remove(UUID storableUid) {
        Optional<ItemDto> uid = items.stream().filter(item -> item.getUid().equals(storableUid)).findFirst();
        return uid.map(iStorable -> items.remove(items.indexOf(iStorable))).orElse(null);
    }

    @Override
    @JsonIgnore
    public void translate(StorageDto dst, UUID storableUid) {
        Optional<ItemDto> uid = items.stream().filter(item -> item.getUid().equals(storableUid)).findFirst();
        uid.ifPresent(iStorable -> dst.put(items.remove(items.indexOf(iStorable))));
    }

    @Override
    @JsonIgnore
    public boolean has(UUID storableUid) {
        return items.stream().anyMatch(iStorable -> iStorable.getUid().equals(storableUid));
    }

    @Override
    @JsonIgnore
    public boolean has(String storableName) {
        return items.stream().anyMatch(iStorable -> iStorable.getName().equals(storableName));
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    @JsonIgnore
    public boolean isFull() {
        return items.size() >= capacity;
    }

    @Override
    @JsonIgnore
    public void removeAll() {
        items.clear();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return Objects.hash(getName(), getUid());
    }

    @Override
    @JsonIgnore
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
