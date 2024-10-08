package game.freya.enums.player;

import lombok.Getter;

import java.awt.*;

@Getter
public enum HeroType {
    SNIPER("Стрелок", new Color(240, 236, 34)), // пулеметчик, снайпер, страйдер
    TOWER("Башня", new Color(15, 41, 77)), // защитник, танк, турель
    FIXER("Ремонтник", new Color(188, 217, 24)), // восстановление и ремонт, обработка ресурсов
    HUNTER("Добытчик", new Color(217, 88, 24)), // добыча и транспортировка, обработка ресурсов
    HACKER("Взломщик", new Color(34, 61, 240)), // взлом систем (врагов тоже)
    VOID("Без класса", new Color(84, 84, 84)); // -

    private final String description;

    private final Color color;

    HeroType(String description, Color color) {
        this.description = description;
        this.color = color;
    }
}
