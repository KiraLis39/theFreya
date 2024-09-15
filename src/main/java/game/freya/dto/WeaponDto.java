package game.freya.dto;

import game.freya.dto.roots.ItemDto;
import game.freya.enums.player.HeroType;
import game.freya.interfaces.iHero;
import game.freya.interfaces.iWeapon;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@SuperBuilder
public class WeaponDto extends ItemDto implements iWeapon {
    public final Set<HeroType> allowedHeroTypes = new HashSet<>();

    public short requiredLevel;

    public int attackPower;

    @Override
    public boolean isAllowed(iHero hero) {
        return allowedHeroTypes.contains(hero.getType());
    }
}
