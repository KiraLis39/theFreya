package game.freya.gui.panes;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.services.GameControllerService;
import game.freya.states.substates.DebugInfoState;
import game.freya.states.substates.ExitHandlerState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public class JMEApp extends SimpleApplication {
    private volatile boolean isReady;
    private GameControllerService gameControllerService;

    public JMEApp(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;

        setSettings(settings);

        // игра может быть сетевой, так что сами контролируем когда пауза:
        setPauseOnLostFocus(false);

        // снижаем FPS при потере фокуса окном игры (вплоть до 20 fps):
        // (хотя всё это уже кастомизировано в this.loseFocus()\this.gainFocus()...)
        setLostFocusBehavior(LostFocusBehavior.ThrottleOnLostFocus);
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        // контроллер выхода из игры:
        stateManager.attach(new ExitHandlerState(gameControllerService));

        // удаляем дефолтный debug info state и ставим свой state:
        stateManager.detach(stateManager.getState(StatsAppState.class, false));
        stateManager.attach(new DebugInfoState());

        // запуск игрового окна:
        setReady(true);
    }

    // tpf большой на медленных ПК и маленький на быстрых ПК.
    @Override
    public void simpleUpdate(float tpf) {
        // todo: ...
    }

    @Override
    public void simpleRender(RenderManager rm) {
        // todo: add render code
    }

    @Override
    public void requestClose(boolean esc) {
        stateManager.getState(ExitHandlerState.class).onExit();
    }

    public void setScene(BaseAppState state) {
        log.info("Loading the scene '{}'...", state.getId());
        stateManager.attach(state);

        enqueue(() -> {
            // вывод в debug info имени текущего state:
            stateManager.getState(DebugInfoState.class).currentStateId(state.getId());
            stateManager.getState(DebugInfoState.class).rebuildFullText();
        });
    }

    public void setAltControlMode(boolean altMode) {
        if (altMode) {
//            mouseInput.setNativeCursor(new JmeCursor());
            stateManager.detach(getStateManager().getState(FlyCamAppState.class));
            mouseInput.setCursorVisible(true);
            flyCam.setDragToRotate(false);
        } else {
            stateManager.attach(new FlyCamAppState());
            mouseInput.setCursorVisible(false);
            flyCam.setDragToRotate(true);
        }
    }

    @Override
    public void gainFocus() {
        log.info("Focus gained...");
        context.setAutoFlushFrames(true);
        if (inputManager != null) {
            inputManager.reset();
        }

        if (paused && Constants.getUserConfig().isPauseOnHidden()) {
            paused = false;
            log.debug("Game resumed...");
        }
    }

    @Override
    public void loseFocus() {
        log.info("Focus lost...");
        if (!Constants.getUserConfig().isFullscreen()) {
            context.setAutoFlushFrames(false); // снижаем fps (if fullscreen broke tabs)
        }

        if (Controls.isGameActive() && gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            return;
        }

        if (!paused && Constants.getUserConfig().isPauseOnHidden()) {
            paused = true;
            log.debug("Game paused...");
        }
    }

    public void setPaused(boolean b) {
        paused = b;
    }

    public boolean isPaused() {
        return paused;
    }
}
