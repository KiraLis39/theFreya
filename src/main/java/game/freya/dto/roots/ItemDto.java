package game.freya.dto.roots;

import game.freya.enums.amunitions.RarityType;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iItem;
import game.freya.interfaces.subroot.iStorable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class ItemDto extends AbstractEntityDto implements iItem {
    @Builder.Default
    @Schema(description = "Прочность предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int durability = 3;

    @Builder.Default
    @Schema(description = "Хранилища этого предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final Set<StorageDto> storages = new HashSet<>(1);

    @Builder.Default
    @Schema(description = "Список бафов данного предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final Set<BuffDto> buffs = new HashSet<>(3);

    @Builder.Default
    @Min(1)
    @Schema(description = "По сколько может стаковаться в ячейке", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int stackCount = 1;

    @Override
    public void draw(Graphics2D g2D) {

    }

    @Override
    public float getWeight() {
        return 0;
    }

    @Override
    public void drop(Point2D.Double location) {
        log.info("Оружие '{}' ({}) выбрасывается в точке '{}'...", getName(), getUid(), location);
        // setCacheKey("dropped_item_type_image");
        setOwnerUid(null);
        setLocation(location);
    }

    @Override
    public void onStoreTo(StorageDto storageDto) {
        log.info("Оружие '{}' ({}) помещается в хранилище '{}' ({})...", getName(), getUid(), storageDto.getName(), storageDto.getUid());
    }

    @Override
    public RarityType getRarity() {
        return null;
    }

    @Override
    public Set<iStorable> getSpareParts() {
        return null;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CurrencyVault getCurrencyType() {
        return null;
    }

    @Override
    public int getDefaultByeCost() {
        return 0;
    }

    @Override
    public int getCurrentByeCost() {
        return 0;
    }

    @Override
    public int getDefaultSellCost() {
        return 0;
    }

    @Override
    public int getCurrentSellCost() {
        return 0;
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public boolean isDestroyed() {
        return durability <= 0;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean isBroken() {
        return durability <= 1;
    }

    @Override
    public void onBreak() {
        this.durability = 1;
        log.info("Оружие '{}' ({}) сломано.", getName(), getUid());
    }

    @Override
    public void repair() {
        this.durability = 100;
    }

    @Override
    public void onRepair() {
        log.info("Ремонт оружия '{}' ({})...", getName(), getUid());
    }

    @Override
    public int compareTo(iItem o) {
        return (o.getName().compareTo(getName()) + Integer.compare(o.getDurability(), getDurability())) / 2;
    }
}
