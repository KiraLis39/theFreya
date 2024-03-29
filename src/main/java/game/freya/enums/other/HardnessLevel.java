package game.freya.enums.other;

import lombok.Getter;

public enum HardnessLevel {
    PEACEFUL("Мирный"),
    EASY("Лёгкий"),
    HARD("Сложный"),
    HELL("Ад");

    @Getter
    private final String description;

    HardnessLevel(String description) {
        this.description = description;
    }
}
