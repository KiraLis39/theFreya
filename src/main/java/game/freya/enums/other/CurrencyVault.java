package game.freya.enums.other;

import lombok.Getter;

@Getter
public enum CurrencyVault {
    CHIPS("микрочипов"),
    GOLD("золотых монет"),
    REAL("реалов");

    private final String description;

    CurrencyVault(String description) {
        this.description = description;
    }
}
