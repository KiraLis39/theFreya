package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import game.freya.config.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
//@Accessors(chain = true, fluent = true, prefix = {"+get"})
@SuperBuilder
@RequiredArgsConstructor
public abstract class AbstractEntityDto {
    @Schema(description = "UUID объекта", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @NotNull
    @Schema(description = "Имя/Название объекта", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @Schema(description = "Создатель объекта", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID createdBy;

    @Schema(description = "Владелец объекта", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID ownerUid;

    @Schema(description = "Мир объекта", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID worldUid;

    @Builder.Default
    @Schema(description = "The object`s collider", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle2D.Double collider = new Rectangle2D.Double();

    @Builder.Default
    @Schema(description = "The object`s visual size", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Dimension size = new Dimension(1, 1);

    @Builder.Default
    @Schema(description = "Позиция объекта на карте", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Point2D.Double location = new Point2D.Double(0, 0);

    @Schema(description = "The object`s real box shape", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle2D.Double shape;

    @Builder.Default
    @JsonProperty("isVisible")
    @Schema(description = "Is Object visible?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isVisible = true;

    @Builder.Default
    @Schema(description = "Image name into cache", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cacheKey = Constants.NO_CACHED_IMAGE_MOCK_KEY;

    @Builder.Default
    @Schema(description = "Дата создания объекта", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    @Schema(description = "Дата изменения объекта", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime modifyDate = LocalDateTime.now();

    public boolean hasCollision() {
        return collider != null && collider.getWidth() > 0 && collider.getHeight() > 0;
    }

    @Transient
    public void setCollider() {
        if (size.width > 2 && size.height > 2) {
            this.collider = new Rectangle2D.Double(location.x + 1, location.y + 1, size.width - 2, size.height - 2);
        } else {
            this.collider = new Rectangle2D.Double();
        }
    }

    public Rectangle2D.Double getCollider() {
        if (collider == null && location != null && size != null) {
            setCollider();
        }
        return collider;
    }

    @Transient
    public void setCollider(Point2D location) {
        setShape(location);
        this.collider = new Rectangle2D.Double(this.shape.x + 3, this.shape.y + 3, this.shape.width - 6, this.shape.height - 6);
    }

    @Transient
    public void setShape(Point2D location) {
        if (size.width > 2 && size.height > 2) {
            this.shape = new Rectangle2D.Double(
                    (int) (location.getX() + 1),
                    (int) (location.getY() + 1),
                    getSize().width - 2,
                    getSize().height - 2);
        } else {
            this.shape = new Rectangle2D.Double();
        }
    }

    @Transient
    public void setSize(Dimension size) {
        this.size = size;
        if (this.location != null) {
            setCollider();
        }
    }

    @Transient
    public void setLocation(double x, double y) {
        this.location = new Point2D.Double(x, y);
        setShape(this.location);
    }

    @Transient
    public void setLocation(Point2D.Double location) {
        this.location = location;
        setShape(this.location);
        if (this.size != null) {
            setCollider();
        }
    }

    @Transient
    public boolean isInSector(Rectangle2D.Double sector) {
        return sector.contains(getLocation());
    }
}
