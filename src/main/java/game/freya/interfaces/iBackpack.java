package game.freya.interfaces;

import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.ItemDto;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.subroot.iStorage;

/**
 * Интерфейс для личного инвентаря героя.
 * Только он может иметь баланс личных средств и участвовать в торговле.
 */
public interface iBackpack extends iStorage {
    int getBalanceOf(CurrencyVault vaultType);

    int increaseBalanceOf(int paySum, CurrencyVault vaultType);

    boolean tryDecreaseBalanceOf(int paySum, CurrencyVault vaultType);

    boolean trade(CharacterDto seller, ItemDto item, CharacterDto buyer, CurrencyVault vaultType, int paySum);
}
