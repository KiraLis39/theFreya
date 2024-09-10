package game.freya.entities.roots;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @Column(name = "owner_uid")
    private UUID ownerUid;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "world_uid")
    private UUID worldUid;

    @NotNull
    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @NotNull
    @Column(name = "size", nullable = false)
    private Dimension size;

    @Column(name = "collider")
    private Rectangle2D.Double collider;

    @Column(name = "shape")
    private Rectangle2D.Double shape;

    @NotNull
    @Column(name = "location", nullable = false)
    private Point2D.Double location;

    @Column(name = "visible", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isVisible;

//    @Column(name = "collision", columnDefinition = "BOOLEAN DEFAULT TRUE")
//    private boolean hasCollision;

    @Column(name = "cache_key", length = 32)
    private String cacheKey;

    @CreatedDate
    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;
}
