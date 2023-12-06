package game.freya.enums;

import lombok.Getter;

public enum CurrencyVault {
    CHIPS("микрочипов"),
    GOLD("золотых монет"),
    REAL("реалов");

    @Getter
    private final String description;

    CurrencyVault(String description) {
        this.description = description;
    }
}
