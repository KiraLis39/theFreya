package game.freya.enums;

import lombok.Getter;

public enum HeroCorpusType {
    COMPACT("Компактный"),
    ADVANCED("Продвинутый"),
    HARD("Тяжёлый");

    @Getter
    private final String description;

    HeroCorpusType(String description) {
        this.description = description;
    }
}
