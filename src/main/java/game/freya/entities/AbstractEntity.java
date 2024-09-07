package game.freya.entities;

import game.freya.entities.roots.Environment;
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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid", nullable = false)
    private UUID uid;

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

    @Column(name = "collider", nullable = false)
    private Rectangle collider;

    @Column(name = "shape", nullable = false)
    private Rectangle shape;

    @Column(name = "location", nullable = false)
    private Point2D.Double location;

    @Column(name = "visible", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isVisible;

    @Column(name = "collision", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean hasCollision;

    @Column(name = "cache_key", length = 32)
    private String cacheKey;

    @CreatedDate
    @CreationTimestamp
    @Column(name = "created_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modify_date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp", nullable = false)
    private LocalDateTime modifyDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Environment that)) {
            return false;
        }
        return Objects.equals(uid, that.getUid())
                && Objects.equals(name, that.getName())
                && Objects.equals(cacheKey, that.getCacheKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, cacheKey);
    }

    @Override
    public String toString() {
        return "AbstractEntity{"
                + "uuid=" + uid
                + ", createdBy=" + createdBy
                + ", name='" + name + '\''
                + ", isVisible=" + isVisible
                + ", hasCollision=" + hasCollision
                + ", cacheKey='" + cacheKey + '\''
                + '}';
    }
}
