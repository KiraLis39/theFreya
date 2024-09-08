package game.freya.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractEntity {
    @Column(name = "owner_uid")
    private UUID ownerUid;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @NotNull
    @Column(name = "world_uid", nullable = false)
    private UUID worldUid;

    @NotNull
    @Column(name = "name", length = 16, nullable = false)
    private String name;

    @Column(name = "size", nullable = false)
    private Dimension size;

    @Column(name = "collider")
    private Rectangle collider;

    @Column(name = "shape")
    private Rectangle shape;

    @Column(name = "location", nullable = false)
    private Point2D.Double location;

    @Column(name = "visible", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isVisible;

    @Column(name = "collision", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean hasCollision;

    @Column(name = "cache_key", length = 32, columnDefinition = "VARCHAR(32) DEFAULT no_image")
    private String cacheKey;

    @CreatedDate
    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modify_date", nullable = false)
    private LocalDateTime modifyDate;
}
