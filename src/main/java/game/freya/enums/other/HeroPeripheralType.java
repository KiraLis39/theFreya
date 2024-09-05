package game.freya.enums.other;

import lombok.Getter;

@Getter
public enum HeroPeripheralType {
    COMPACT("Компактный"),
    ADVANCED("Продвинутый"),
    HARD("Тяжёлый");

    private final String description;

    HeroPeripheralType(String description) {
        this.description = description;
    }
}
