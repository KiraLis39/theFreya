package game.freya.interfaces;

import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.subroot.iStorable;

public interface iTradeable extends iStorable {
    CurrencyVault getCurrencyType();

    int getDefaultByeCost(); // стоимость по-умолчанию покупки у NPC

    int getCurrentByeCost(); // стоимость покупки у игроков

    int getDefaultSellCost(); // стоимость по-умолчанию продажи у NPC

    int getCurrentSellCost(); // стоимость продажи у игроков
}
