package game.freya.enums;

import lombok.Getter;

import java.awt.Color;

public enum HeroType {
    SNIPER("Стрелок", new Color(240, 236, 34)), // пулеметчик, снайпер, страйдер
    TOWER("Башня", new Color(15, 41, 77)), // защитник, танк, турель
    FIXER("Ремонтник", new Color(188, 217, 24)), // восстановление и ремонт, обработка ресурсов
    HUNTER("Добытчик", new Color(217, 88, 24)), // добыча и транспортировка, обработка ресурсов
    HACKER("Взломщик", new Color(34, 61, 240)), // взлом систем (врагов тоже)
    VOID("Без класса", new Color(84, 84, 84)); // -

    @Getter
    private final String description;

    @Getter
    private final Color color;

    HeroType(String description, Color color) {
        this.description = description;
        this.color = color;
    }
}
