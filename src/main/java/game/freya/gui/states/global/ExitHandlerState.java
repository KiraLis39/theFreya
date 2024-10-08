package game.freya.gui.states.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.system.lwjgl.LwjglWindow;
import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.services.GameControllerService;
import org.lwjgl.glfw.GLFW;

public class ExitHandlerState extends BaseAppState {
    private final GameControllerService gameControllerService;

    public ExitHandlerState(GameControllerService gameControllerService) {
        super(ExitHandlerState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
    }

    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void onEnable() {
        getApplication().getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT); // дефолтное закрытие окна игры.
    }

    @Override
    protected void onDisable() {
    }

    public void onExit() {
        // todo: когда в EXCLUSIVE - надо вместо модального диалога отображать другую панель внутри окна игры.
        int answer = (int) new FOptionPane(Constants.getGameFrame()).buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get();
        if (answer == 0) {
            gameControllerService.exitTheGame(null, 0);
        } else {
            try {
                GLFW.glfwSetWindowShouldClose(((LwjglWindow) getApplication().getContext()).getWindowHandle(), false);
            } catch (Exception _) {
            }
        }
    }

    @Override
    protected void cleanup(Application app) {
    }
}
