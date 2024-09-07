package game.freya.interfaces;

import game.freya.enums.other.CurrencyVault;

public interface iBackpack {
    void increaseBalance(int paySum, CurrencyVault vaultType);

    boolean tryDecreaseBalance(int paySum, CurrencyVault vaultType);
}
