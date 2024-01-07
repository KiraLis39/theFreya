package game.freya.enums.other;

import lombok.Getter;

public enum CurrencyVault {
    CHIPS("микрочипов") {
        @Override
        boolean isVaultActive(CurrencyVault vault) {
            return isChipsActive;
        }

        @Override
        void setVaultActive(CurrencyVault vault, boolean isActive) {
            isChipsActive = isActive;
        }
    },
    GOLD("золотых монет") {
        @Override
        boolean isVaultActive(CurrencyVault vault) {
            return isGoldActive;
        }

        @Override
        void setVaultActive(CurrencyVault vault, boolean isActive) {
            isGoldActive = isActive;
        }
    },
    REAL("реалов") {
        @Override
        boolean isVaultActive(CurrencyVault vault) {
            return isRealActive;
        }

        @Override
        void setVaultActive(CurrencyVault vault, boolean isActive) {
            isRealActive = isActive;
        }
    };

    private static boolean isChipsActive;

    private static boolean isGoldActive;

    private static boolean isRealActive;

    @Getter
    private final String description;

    CurrencyVault(String description) {
        this.description = description;
    }

    abstract boolean isVaultActive(CurrencyVault vault);

    abstract void setVaultActive(CurrencyVault vault, boolean isActive);
}
