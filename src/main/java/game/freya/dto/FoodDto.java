package game.freya.dto;

import game.freya.dto.roots.ItemDto;
import game.freya.interfaces.iEdible;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public class FoodDto extends ItemDto implements iEdible {
    @Builder.Default
    @Schema(description = "Отравлена ли еда?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isPoisoned = false;

    @Builder.Default
    @Schema(description = "Количество восстанавливаемого ХП", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int healthCompensation = 1;

    @Builder.Default
    @Schema(description = "Количество восстанавливаемого масла", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int oilCompensation = 1;

    @Override
    public void use() {
        log.info("Здесь будет использование еды '{}' ({})...", getName(), getUid());
    }

    @Override
    public void onRotting() {
        log.info("Еда '{}' ({}) протухла!", getName(), getUid());
    }
}
