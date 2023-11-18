package game.freya.enums;

import lombok.Getter;

public enum HeroType {
    SNIPER("Стрелок"), // пулеметчик, снайпер, страйдер
    TOWER("Башня"), // защитник, танк, турель
    FIXER("Ремонтник"), // восстановление и ремонт, обработка ресурсов
    HUNTER("Добытчик"), // добыча и транспортировка, обработка ресурсов
    HACKER("Взломщик"), // взлом систем (врагов тоже)
    VOID("-"); // -

    @Getter
    private final String description;

    HeroType(String description) {
        this.description = description;
    }
}
