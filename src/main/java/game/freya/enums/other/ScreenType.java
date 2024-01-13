package game.freya.enums.other;

import game.freya.GameController;
import game.freya.gl.RenderScreen;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.Game;
import game.freya.gui.panes.LoadGame;
import game.freya.gui.panes.LoadMenu;
import game.freya.gui.panes.Menu;

public enum ScreenType {
    MENU_LOADING_SCREEN(0, "Экран загрузки приложения") {
        @Override
        public RenderScreen getScreen(WindowManager windowManager, GameController gameController) {
            return new LoadMenu(windowManager, gameController);
        }
    },
    MENU_SCREEN(1, "Экран меню") {
        @Override
        public RenderScreen getScreen(WindowManager windowManager, GameController gameController) {
            return new Menu(windowManager, gameController);
        }
    },
    GAME_LOADING_SCREEN(2, "Экран загрузки игры") {
        @Override
        public RenderScreen getScreen(WindowManager windowManager, GameController gameController) {
            return new LoadGame(windowManager, gameController);
        }
    },
    GAME_SCREEN(3, "Экран игрового геймплея") {
        @Override
        public RenderScreen getScreen(WindowManager windowManager, GameController gameController) {
            return new Game(windowManager, gameController);
        }
    };

    final long index;

    final String description;

    ScreenType(long index, String description) {
        this.index = index;
        this.description = description;
    }

    public abstract RenderScreen getScreen(WindowManager windowManager, GameController gameController);
}
