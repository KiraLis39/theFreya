package game.freya.dto;

import game.freya.dto.roots.ItemDto;
import game.freya.interfaces.iEdible;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@SuperBuilder
public class FoodDto extends ItemDto implements iEdible {
    public boolean isPoisoned;

    public int healthCompensation;

    public int oilCompensation;

    @Override
    public void use() {
        log.info("Здесь будет использование еды '{}' ({})...", getName(), getUid());
    }

    @Override
    public void onRotting() {
        log.info("Еда '{}' ({}) протухла!", getName(), getUid());
    }
}
