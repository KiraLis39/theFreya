package game.freya.interfaces;

import game.freya.enums.player.HeroType;

import java.util.Set;

public interface iWeapon extends iItem {
    String getName(); // сборка имени оружия производится с учетом его редкости или владельца

    Set<HeroType> getAllowedHeroTypes();

    short getRequiredLevel(); // необходимый уровень

    boolean isAllowed(iHero hero); // разрешено ли носить указанному персонажу?

    int getAttackPower();
}
