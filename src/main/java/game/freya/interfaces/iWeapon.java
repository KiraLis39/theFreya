package game.freya.interfaces;

import game.freya.enums.HeroType;

import java.util.Set;

public interface iWeapon extends iTools {
    String setName(); // сборка имени оружия производится с учетом его редкости или владельца
    Set<HeroType> getAllowedHeroTypes();
    boolean isAllowed(iHero hero); // разрешено ли носить указанному персонажу?
    int getAttackPower();

    /*
    float getWeight(); // вес

    int getStrength(); // прочность

    short getRequiredLevel(); // необходимый уровень

    RarityType getRarity(); // редкость предмета

    int getDefaultByeCost(); // стоимость по-умолчанию покупки у NPC

    int getDefaultSellCost(); // стоимость по-умолчанию продажи у NPC

    Set<Buff> seeBuffs(); // зачарования, бафы, доп.свойства
     */
}
