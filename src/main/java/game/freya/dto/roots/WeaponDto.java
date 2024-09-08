package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonFormat;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iWeapon;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public abstract class WeaponDto implements iGameObject, iWeapon {
    private UUID uid;
    private UUID ownerUid;
    private UUID createdBy;
    private UUID worldUid;
    private String name;
    private Dimension size;
    private Rectangle collider;
    private Rectangle shape;
    private Point2D.Double location;
    private boolean isVisible;
    private boolean hasCollision;
    private String cacheKey;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime modifyDate;
}
