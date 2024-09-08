package game.freya.interfaces;

import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.ItemDto;
import game.freya.enums.other.CurrencyVault;

import java.util.UUID;

public interface iTradeable extends Comparable<iTradeable> {
    UUID getUid();

    String getName();

    CurrencyVault getCurrencyType();

    int getDefaultByeCost(); // стоимость по-умолчанию покупки у NPC

    int getCurrentByeCost(); // стоимость покупки у игроков

    int getDefaultSellCost(); // стоимость по-умолчанию продажи у NPC

    int getCurrentSellCost(); // стоимость продажи у игроков

    void setCurrentSellCost(int cost);

    default boolean trade(CharacterDto seller, ItemDto item, CharacterDto buyer, CurrencyVault vaultType, int paySum) {
        ItemDto itemToSell = seller.getInventory().removeItem(item);
        if (itemToSell == null) {
            return false;
        }

        if (buyer.getInventory().tryDecreaseBalance(paySum, vaultType)) {
            // успешная торговля:
            seller.getInventory().increaseBalance(paySum, vaultType);
            buyer.getInventory().addItem(itemToSell);
            return true;
        } else {
            // провал торговли:
            seller.getInventory().addItem(itemToSell);
            return false;
        }
    }
}
