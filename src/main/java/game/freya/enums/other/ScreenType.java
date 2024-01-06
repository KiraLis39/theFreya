package game.freya.enums.other;

public enum ScreenType {
    MENU_SCREEN(0),
    GAME_SCREEN(1);

    final long index;

    ScreenType(long index) {
        this.index = index;
    }
}
