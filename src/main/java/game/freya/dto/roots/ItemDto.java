package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
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
public non-sealed class ItemDto extends TradeableImpl implements iGameObject, iStorable {

//    @Transient
//    @JsonIgnore
//    private static TradeableImpl tradeable;

    @Builder.Default
    @Min(1)
    @Schema(description = "По сколько может стаковаться в ячейке", requiredMode = Schema.RequiredMode.REQUIRED)
    private int stackCount = 1;

    @Builder.Default
    @Schema(description = "Хранилища этого предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Set<StorageDto> storages = new HashSet<>(1);

    @Override
    @JsonIgnore
    public void draw(Graphics2D g2D) {

    }

    @Override
    @JsonIgnore
    public void drop(Point2D.Double location) {
        // setCacheKey("dropped_item_type_image");
        setOwnerUid(null);
        setLocation(location);
    }

    @Override
    public void onStoreTo(StorageDto storageDto) {
        log.info("Item {} was stored into {}", getName(), storageDto.getName());
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
    public void setCurrentSellCost(int cost) {

    }
}
