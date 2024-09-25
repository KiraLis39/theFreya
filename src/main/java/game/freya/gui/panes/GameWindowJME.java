package game.freya.gui.panes;

import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.MouseInput;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.states.DebugInfoState;
import game.freya.gui.states.ExitHandlerState;
import game.freya.gui.states.MenuBackgState;
import game.freya.gui.states.MenuHotKeysState;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Setter
@Getter
public class GameWindowJME extends SimpleApplication {
    private volatile boolean isReady;
    private Node currentModeNode;
    private GameControllerService gameControllerService;
    private ExecutorService executor;

    public GameWindowJME(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;

        setSettings(settings);
        setShowSettings(true); // not works
//        setDisplayFps(Constants.getGameConfig().isFpsInfoVisible());
//        setDisplayStatView(Constants.getGameConfig().isStatsInfoVisible());

        // игра может быть сетевой, так что сами контролируем когда пауза:
        setPauseOnLostFocus(false);

        // снижаем FPS при потере фокуса окном игры (вплоть до 20 fps):
        // (хотя всё это уже кастомизировано в this.loseFocus()\this.gainFocus()...)
        setLostFocusBehavior(LostFocusBehavior.ThrottleOnLostFocus);

        start(true);
        Constants.getSoundPlayer().play("landing");
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
//        executor.execute(this::setInAc);
//        executor.shutdown();

//        renderer.setLinearizeSrgbImages(Constants.getGameConfig().isLinearSrgbImagesEnable());

        // какая-то настройка аудио?
//        float[] eax = new float[]{15, 38.0f, 0.300f, -1000, -3300, 0,
//                1.49f, 0.54f, 1.00f, -2560, 0.162f, 0.00f, 0.00f,
//                0.00f, -229, 0.088f, 0.00f, 0.00f, 0.00f, 0.125f, 1.000f,
//                0.250f, 0.000f, -5.0f, 5000.0f, 250.0f, 0.00f, 0x3f};
//        Environment env = new Environment(eax);
//        audioRenderer.setEnvironment(env);

        // контроллер выхода из игры:
        stateManager.attach(new ExitHandlerState(gameControllerService));

        // запуск игрового окна:
        setReady(true);
    }

    /* Interact with game events in the main loop */
    // tpf большой на медленных ПК и маленький на быстрых ПК.
    @Override
    public void simpleUpdate(float tpf) {
        if (currentModeNode != null && currentModeNode.getName().equals(NodeNames.GAME_SCENE.name())) {
            currentModeNode.getChild("Box").rotate(0.003f * tpf, 0.003f * tpf, 0.003f * tpf);
        }

        // first-person: keep the audio listener moving with the camera
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

    /* (optional) Make advanced modifications to frameBuffer and scene graph. */
    @Override
    public void simpleRender(RenderManager rm) {
        // todo: add render code
    }

    @Override
    public void requestClose(boolean esc) {
        stateManager.getState(ExitHandlerState.class).onExit();
    }

    public void setScene(Node nextNode) {
        this.currentModeNode = nextNode;

        // удаляем дефолтный debug info state и ставим свой state:
        if (!stateManager.hasState(stateManager.getState(DebugInfoState.class))) {
            stateManager.detach(stateManager.getState(StatsAppState.class));
            stateManager.attach(new DebugInfoState(this.currentModeNode));
        }

        if (this.currentModeNode.getName().equals(NodeNames.MENU_SCENE.name())) {
            // если загружается меню - удаляем игру и камеру:
            rootNode.detachChildNamed(NodeNames.GAME_SCENE.name());
            // включаем фоновую музыку меню:
            stateManager.attach(new MenuBackgState(this.currentModeNode));
            // подключаем модуль горячих клавиш:
            if (stateManager.attach(new MenuHotKeysState(gameControllerService))) {
                executor.execute(() -> {
                    stateManager.getState(MenuHotKeysState.class).startingAwait();
                    // перевод курсора в режим меню:
                    stateManager.getState(MenuHotKeysState.class).setAltControlMode(true);
                });
                log.info("Запуск главного меню произведён!");
            }
        } else {
            // если загружается игра:
            rootNode.detachChildNamed(NodeNames.MENU_SCENE.name());
            // перевод курсора в режим игры:
            stateManager.getState(MenuHotKeysState.class).setAltControlMode(false);
        }

        log.info("Loading the scene '{}'...", this.currentModeNode.getName());
        rootNode.attachChild(this.currentModeNode);
    }

    @Override
    public void gainFocus() {
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
        context.setAutoFlushFrames(false); // снижаем fps

        if (Controls.isGameActive() && gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            return;
        }

        log.debug("Hide or minimized");
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

    public MouseInput getMouseInput() {
        return mouseInput;
    }
}
