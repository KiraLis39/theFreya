package game.freya.enums.player;

import lombok.Getter;

@Getter
public enum HeroCorpusType {
    COMPACT("Компактный"),
    ADVANCED("Продвинутый"),
    HARD("Тяжёлый");

    private final String description;

    HeroCorpusType(String description) {
        this.description = description;
    }
}
