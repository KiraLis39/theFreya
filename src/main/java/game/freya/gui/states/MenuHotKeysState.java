package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import game.freya.services.GameControllerService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class MenuHotKeysState extends AbstractAppState {
    private SimpleApplication app;
    private InputManager inputManager;

    protected Node guiNode;
    protected BitmapFont guiFont;

    public MenuHotKeysState(GameControllerService gameControllerService) {
        super("TestState");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app;
        this.inputManager = app.getInputManager();

        super.initialize(stateManager, app);
    }

    @Override
    public void cleanup() {
//        if (inputManager.hasMapping(INPUT_MAPPING_CAMERA_POS))
//            inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS);
//        if (inputManager.hasMapping(INPUT_MAPPING_MEMORY))
//            inputManager.deleteMapping(INPUT_MAPPING_MEMORY);

//        inputManager.removeListener(keyListener);

//        guiNode.detachChild(statsView);
//        guiNode.detachChild(fpsText);
//        guiNode.detachChild(darkenFps);
//        guiNode.detachChild(darkenStats);

        super.cleanup();
    }
}
