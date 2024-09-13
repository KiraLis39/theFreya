package game.freya.enums.other;

import lombok.Getter;

@Getter
public enum HardnessLevel {
    PEACEFUL("Мирный"),
    EASY("Лёгкий"),
    HARD("Сложный"),
    HELL("Ад");

    private final String description;

    HardnessLevel(String description) {
        this.description = description;
    }
}
