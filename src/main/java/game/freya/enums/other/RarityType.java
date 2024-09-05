package game.freya.enums.other;

import lombok.Getter;

import java.awt.*;

@Getter
public enum RarityType {
    DECREPIT(Color.GRAY, "Ветх"),
    SIMPLE(Color.WHITE, "Прост"),
    GOOD(Color.YELLOW, "Хорош"),
    DURABLE(Color.GREEN, "Прочн"),
    GREAT(Color.BLUE, "Отличн"),
    SUPER(Color.MAGENTA, "Великолепн"),
    RARE(Color.ORANGE, "Редк"),
    LEGENDARY(Color.RED, "Легендарн");

    private final Color rarityColor;

    private final String description;

    RarityType(Color rarityColor, String description) {
        this.rarityColor = rarityColor;
        this.description = description;
    }

    public String getDescription(GenderType gt) {
        return switch (gt) {
            case IT -> description + "ое";
            case HE -> description + "ый";
            case SHE -> description + "ая";
            case THIS -> description + "ие";
        };
    }
}
