package game.freya.enums;

import lombok.Getter;

public enum HeroPeriferiaType {
    COMPACT("Компактный"),
    ADVANCED("Продвинутый"),
    HARD("Тяжёлый");

    @Getter
    private final String description;

    HeroPeriferiaType(String description) {
        this.description = description;
    }
}
