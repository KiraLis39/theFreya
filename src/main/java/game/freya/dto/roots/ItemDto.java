package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorable;
import game.freya.interfaces.iStorage;
import game.freya.interfaces.iTradeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@RequiredArgsConstructor
public class ItemDto implements iGameObject, iStorable {
    @Schema(description = "UUID of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @NotNull
    @Schema(description = "Owner`s uid of this item", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID ownerUid;

    @NotNull
    @Schema(description = "Creator`s uid of this item", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID createdBy;

    @NotNull
    @Schema(description = "World`s uid of this item", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID worldUid;

    @NotNull
    @Schema(description = "Name of this item", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Builder.Default
    @Schema(description = "Visual size of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Dimension size = new Dimension(32, 32);

    @Schema(description = "Collider of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle collider;

    @Schema(description = "Rigid body of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Rectangle shape;

    @Builder.Default
    @Schema(description = "World location of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Point2D.Double location = new Point2D.Double(0, 0);

    @Builder.Default
    @JsonProperty("isVisible")
    @Schema(description = "Is item is visible?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isVisible = true;

    @Builder.Default
    @Schema(description = "Is item has collision with other objects?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean hasCollision = true;

    @NotNull
    @Builder.Default
    @Schema(description = "Cached image name of this item", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cacheKey = "no_image";

    @Builder.Default
    @Schema(description = "Created date of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    @Schema(description = "Modification date of this item", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime modifyDate = LocalDateTime.now();

    @Override
    @JsonIgnore
    public Point2D.Double getCenterPoint() {
        return null;
    }

    @Override
    @JsonIgnore
    public boolean hasCollision() {
        return false;
    }

    @Override
    @JsonIgnore
    public void draw(Graphics2D g2D) {

    }

    @Override
    @JsonIgnore
    public boolean isInSector(Rectangle sector) {
        return false;
    }

    @Override
    @JsonIgnore
    public void drop() {

    }

    @Override
    @JsonIgnore
    public void store(iStorage inStorage) {

    }

    @Override
    @JsonIgnore
    public CurrencyVault getCurrencyType() {
        return null;
    }

    @Override
    @JsonIgnore
    public int getDefaultByeCost() {
        return 0;
    }

    @Override
    @JsonIgnore
    public int getCurrentByeCost() {
        return 0;
    }

    @Override
    @JsonIgnore
    public int getDefaultSellCost() {
        return 0;
    }

    @Override
    @JsonIgnore
    public int getCurrentSellCost() {
        return 0;
    }

    @Override
    @JsonIgnore
    public void setCurrentSellCost(int cost) {

    }

    @Override
    @JsonIgnore
    public int compareTo(@NotNull iTradeable o) {
        return 0;
    }
}
