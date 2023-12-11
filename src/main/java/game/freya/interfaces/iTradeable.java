package game.freya.interfaces;

import game.freya.enums.other.CurrencyVault;
import game.freya.items.prototypes.GameCharacter;

public interface iTradeable {
    CurrencyVault getCurrencyType();

    int getDefaultByeCost(); // стоимость по-умолчанию покупки у NPC

    int getCurrentByeCost(); // стоимость покупки у игроков

    int getDefaultSellCost(); // стоимость по-умолчанию продажи у NPC

    int getCurrentSellCost(); // стоимость продажи у игроков

    void setCurrentSellCost(int cost);

    void trade(GameCharacter owner, GameCharacter buyer, CurrencyVault vault, int value);
}
