package game.freya.enums;

public enum CurrencyVault {
    CHIPS("микрочипов"),
    GOLD("золотых монет"),
    REAL("реалов");

    final String description;
    CurrencyVault(String description) {
        this.description = description;
    }
}
