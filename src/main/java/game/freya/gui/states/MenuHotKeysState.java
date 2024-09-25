package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.RootNodeAppState;
import com.jme3.audio.AudioNode;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.system.AppSettings;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.config.UserConfig;
import game.freya.dto.roots.WorldDto;
import game.freya.gui.panes.GameWindowJME;
import game.freya.services.GameControllerService;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class MenuHotKeysState extends BaseAppState implements ActionListener, AnalogListener {
    private final GameControllerService gameControllerService;
    private final List<String> events = new ArrayList<>();
    private UserConfig.Hotkeys hotKeys;
    private InputManager inputManager;
    private GameWindowJME appRef;

    public MenuHotKeysState(GameControllerService gameControllerService) {
        super("MenuHotKeysState");
        this.gameControllerService = gameControllerService;
    }

    @Override
    public void initialize(Application app) {
        this.appRef = (GameWindowJME) app;
        this.inputManager = getApplication().getInputManager();
    }

    @Override
    protected void onEnable() {
        reloadHotKeys();
        setInAc();
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(this);
    }

    private void reloadHotKeys() {
        this.hotKeys = Constants.getUserConfig().getHotkeys();
    }

    private void setInAc() {
        inputManager.addMapping("ExitAction", new KeyTrigger(hotKeys.getKeyPause().getJmeKey()));
        events.add("ExitAction");

        inputManager.addMapping("ToggleFullscreen", new KeyTrigger(hotKeys.getKeyFullscreen().getJmeKey()));
        events.add("ToggleFullscreen");

        inputManager.addMapping("ToggleGameInfo", new KeyTrigger(hotKeys.getKeyDebugInfo().getJmeKey()));
        events.add("ToggleGameInfo");

        inputManager.addMapping("ToggleSLI", new KeyTrigger(KeyInput.KEY_F9));
        events.add("ToggleSLI");

        inputManager.addMapping("ToggleAmbientLight", new KeyTrigger(KeyInput.KEY_F10));
        events.add("ToggleAmbientLight");

        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        events.add("Shoot");

        inputManager.addListener(this, events.toArray(new String[0]));
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
                case null, default -> log.error("Некорректное указание режима окна '{}'", Constants.getUserConfig().getFullscreenType());
            }
        }

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        getApplication().restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек

        // сброс расположения debug full info:
        getApplication().enqueue(() -> getStateManager().getState(DebugInfoState.class).rebuildFullText());
    }

    private void toggleLSI() {
        if (getApplication().getRenderer().isLinearizeSrgbImages()) {
            log.info("LSI: off");
            Constants.getGameConfig().setLinearSrgbImagesEnable(false);
        } else {
            log.info("LSI: on");
            Constants.getGameConfig().setLinearSrgbImagesEnable(true);
        }
        getApplication().restart();
    }

    public void setAltControlMode(boolean altMode) {
        if (altMode) {
//            mouseInput.setNativeCursor(new JmeCursor());
            appRef.getStateManager().detach(getStateManager().getState(FlyCamAppState.class));
            appRef.getMouseInput().setCursorVisible(true);
            appRef.getFlyByCamera().setDragToRotate(false);
        } else {
            appRef.getStateManager().attach(new FlyCamAppState());
            appRef.getMouseInput().setCursorVisible(false);
            appRef.getFlyByCamera().setDragToRotate(true);
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        System.err.println("\nAction: " + name + "; pressed: " + isPressed + "; tpf: " + tpf);

        switch (name) {
            case "ExitAction", "TogglePause" -> {
                WorldDto w = gameControllerService.getWorldService().getCurrentWorld();
                if (w != null && !w.isNetAvailable() && Controls.isGameActive()) {
                    appRef.setPaused(!appRef.isPaused());
                    log.info("Game paused: {}", appRef.isPaused());
                } else {
                    getStateManager().getState(ExitHandlerState.class).onExit();
                }
            }
            case "ToggleFullscreen" -> toggleFullscreen(); // зависает нажатое событие в слушателе!
            case "ToggleGameInfo" -> getStateManager().getState(DebugInfoState.class).toggleStats();
            case "ToggleSLI" -> toggleLSI();
            case "ToggleAmbientLight" -> getStateManager().getState(RootNodeAppState.class).getRootNode()
                    .getWorldLightList().get(0).setEnabled(!getStateManager().getState(RootNodeAppState.class).getRootNode()
                            .getWorldLightList().get(0).isEnabled());
            case "Shoot" -> {
                AudioNode shot = (AudioNode) appRef.getRootNode().getChild("gun");
                shot.setTimeOffset(shot.getAudioData().getDuration() / 2);
                float shotMinPitch = 0.75f, shotMaxPitch = 1.25f;
                shot.setPitch(shotMinPitch + new Random().nextFloat(shotMaxPitch));
                shot.playInstance();
            }
            default -> log.warn("Не релизовано действие {}", name);
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
//        System.out.println("Process: " + name + "; value: " + value + "; tpf: " + tpf);

//        switch (name) {
//            default -> log.warn("Не релизовано действие {}", name);
//        }

//        if (name.equals("Rotate")) {         // test?
//            player.rotate(0, value*speed, 0);  // action!
//        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    public void startingAwait() {
        while (!isInitialized()) {
            Thread.yield();
        }
    }
}
