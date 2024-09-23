package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import game.freya.services.GameControllerService;

public class ExitHandlerState extends AbstractAppState {
    private SimpleApplication app;
    private GameControllerService gameControllerService;

    public ExitHandlerState(String id, GameControllerService gameControllerService) {
        super(id);
        this.gameControllerService = gameControllerService;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app;
        super.initialize(stateManager, app);
    }
}
