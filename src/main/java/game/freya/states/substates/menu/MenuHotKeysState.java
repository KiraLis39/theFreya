package game.freya.states.substates.menu;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.RootNodeAppState;
import com.jme3.audio.AudioNode;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.system.AppSettings;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.GameWindowJME;
import game.freya.states.substates.DebugInfoState;
import game.freya.states.substates.ExitHandlerState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class MenuHotKeysState extends BaseAppState {
    private final List<String> actions = new ArrayList<>();
    private final List<String> processes = new ArrayList<>();
    private UserConfig.Hotkeys hotKeys;
    private InputManager inputManager;
    private GameWindowJME appRef;
    private ActionListener actList;
    private AnalogListener anlList;

    public MenuHotKeysState() {
        super(MenuHotKeysState.class.getSimpleName());
    }

    @Override
    public void initialize(Application app) {
        this.appRef = (GameWindowJME) app;
        this.inputManager = getApplication().getInputManager();
        actList = new MenuActionListener();
        anlList = new MenuAnalogListener();
    }

    @Override
    protected void onEnable() {
        // перезагружаем хоткеи из конфига игры:
        reloadHotKeys();
        // загружаем обновленные маппинги:
        setInAc();
    }

    @Override
    protected void onDisable() {
        actions.clear();
        processes.clear();
        inputManager.removeListener(actList);
        inputManager.removeListener(anlList);
    }

    private void reloadHotKeys() {
        this.hotKeys = Constants.getUserConfig().getHotkeys();
    }

    private void setInAc() {
        // base game window hotkeys:
        inputManager.addMapping("ExitAction", new KeyTrigger(hotKeys.getKeyPause().getJmeKey()));
        actions.add("ExitAction");

        inputManager.addMapping("ToggleFullscreen", new KeyTrigger(hotKeys.getKeyFullscreen().getJmeKey()));
        actions.add("ToggleFullscreen");

        // debug:
        inputManager.addMapping("ToggleGameInfo", new KeyTrigger(hotKeys.getKeyDebugInfo().getJmeKey()));
        actions.add("ToggleGameInfo");

        // mouse actions:
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        actions.add("Click");

        // mouse test:
        inputManager.addMapping("MATTest", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        processes.add("MATTest");

        inputManager.addMapping("MWTTest", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        actions.add("MWTTest");

        // final:
        inputManager.addListener(actList, actions.toArray(new String[0]));
        inputManager.addListener(anlList, processes.toArray(new String[0]));
    }

    private void toggleFullscreen() {
        AppSettings settings = getApplication().getContext().getSettings();
        DisplayMode vMode = FoxVideoMonitorUtil.getDisplayMode();
//        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (Constants.getUserConfig().isUseVSync()) {
            settings.setFrequency(vMode.getRefreshRate()); // use VSync
        } else if (Constants.getUserConfig().getFpsLimit() > 0) {
            settings.setFrequency(Constants.getUserConfig().getFpsLimit()); // use fps limit
        } else {
            settings.setFrequency(-1); // unlimited
        }

        if (Constants.getUserConfig().isFullscreen()) {
            settings.setWindowSize(settings.getMinWidth(), settings.getMinHeight());
            settings.setFullscreen(false);
            settings.setCenterWindow(true);
        } else if (FoxVideoMonitorUtil.isFullScreenSupported()) {
            switch (Constants.getUserConfig().getFullscreenType()) {
                case EXCLUSIVE -> {
                    settings.setWindowSize(vMode.getWidth(), vMode.getHeight());
                    settings.setBitsPerPixel(vMode.getBitDepth());
                    settings.setFullscreen(true);
                }
                case MAXIMIZE_WINDOW -> {
                    // todo
//                    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
//                    glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
                    settings.setWindowSize(vMode.getWidth(), vMode.getHeight());
//                    settings.setWindowXPosition(3);
                    settings.setWindowYPosition(15);
//                    GLFWVidMode glMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//                    glfwSetWindowMonitor(((LwjglWindow) context).getWindowHandle(), glfwGetPrimaryMonitor(), 0, 0, glMode.width(), glMode.height(), 60);
                    settings.setCenterWindow(false);
                }
                case null, default ->
                        log.error("Некорректное указание режима окна '{}'", Constants.getUserConfig().getFullscreenType());
            }
        }

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        getApplication().restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек

        // сброс расположения debug full info:
        getApplication().enqueue(() -> getStateManager().getState(DebugInfoState.class).rebuildFullText());
    }

    @Override
    protected void cleanup(Application app) {
        inputManager.removeListener(actList);
        inputManager.removeListener(anlList);
    }

    public void startingAwait() {
        while (!isInitialized()) {
            Thread.yield();
        }
    }

    class MenuActionListener implements ActionListener {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed || !isEnabled()) {
                return;
            }

            switch (name) {
                case "ExitAction" -> getStateManager().getState(ExitHandlerState.class).onExit();
                case "ToggleFullscreen" -> toggleFullscreen(); // зависает нажатое событие в слушателе!
                case "ToggleGameInfo" -> getStateManager().getState(DebugInfoState.class).toggleStats();
                case "ToggleAmbientLight" -> getStateManager().getState(RootNodeAppState.class).getRootNode()
                        .getWorldLightList().get(0).setEnabled(!getStateManager().getState(RootNodeAppState.class).getRootNode()
                                .getWorldLightList().get(0).isEnabled());
                case "Click" -> {
                    AudioNode shot = (AudioNode) appRef.getRootNode().getChild("gun");
                    shot.setTimeOffset(shot.getAudioData().getDuration() / 2);
                    float shotMinPitch = 0.75f, shotMaxPitch = 1.25f;
                    shot.setPitch(shotMinPitch + new Random().nextFloat(shotMaxPitch));
                    shot.playInstance();
                }
                default -> log.warn("Не релизовано действие [{}]", name);
            }
        }
    }

    class MenuAnalogListener implements AnalogListener {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (!isEnabled()) {
                return;
            }

            switch (name) {
                case "MATTest" -> log.info("Process [MouseMoveLeftTest]: %.3f".formatted(value));
                case "MWTTest" -> log.info("Process [MouseWheelDownTest]: %.3f".formatted(value));
                case "MoveForward" -> log.info("Process [MoveForwardTest]: %.3f".formatted(value));
                case "MoveLeft" -> log.info("Process [MoveLeftTest]: %.3f".formatted(value));
                case "MoveBack" -> log.info("Process [MoveBackTest]: %.3f".formatted(value));
                case "MoveRight" -> log.info("Process [MoveRightTest]: %.3f".formatted(value));
                case "Click" -> log.info("Process [LMBTest]: %.3f".formatted(value));
                case null, default -> log.warn("Не релизован процесс [{}] ({})", name, value * tpf);
            }
        }
    }
}
