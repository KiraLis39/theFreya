package game.freya.enums.other;

public enum ScreenType {
    MENU_LOADING_SCREEN(0, "Экран загрузки приложения"),
    MENU_SCREEN(1, "Экран меню"),
    GAME_LOADING_SCREEN(2, "Экран загрузки игры"),
    GAME_SCREEN(3, "Экран игрового геймплея");

    final long index;

    final String description;

    ScreenType(long index, String description) {
        this.index = index;
        this.description = description;
    }
}
