package game.freya.gui.panes;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.system.lwjgl.LwjglWindow;
import com.jme3.util.BufferUtils;
import fox.components.FOptionPane;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.config.UserConfig;
import game.freya.dto.roots.WorldDto;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.states.DebugInfoState;
import game.freya.gui.states.MenuBackgState;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Setter
@Getter
public class GameWindowJME extends SimpleApplication implements ActionListener, AnalogListener {
    private final List<String> events = new ArrayList<>();
    private UserConfig.Hotkeys hotKeys;
    private volatile boolean isReady;
    private Node currentModeNode;
    private GameControllerService gameControllerService;

    public GameWindowJME(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;

        setSettings(settings);
        setShowSettings(true); // not works
        setDisplayFps(Constants.getGameConfig().isFpsInfoVisible());
        setDisplayStatView(Constants.getGameConfig().isStatsInfoVisible());
        setPauseOnLostFocus(false);

        reloadHotKeys();

        start(true);
        Constants.getSoundPlayer().play("landing");
    }

    private void reloadHotKeys() {
        this.hotKeys = Constants.getUserConfig().getHotkeys();
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        BufferUtils.setTrackDirectMemoryEnabled(true);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT); // дефолтное закрытие окна игры.
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS); // дефолтные гор. клавиши отображения инфо.

        setInAc();

        renderer.setLinearizeSrgbImages(Constants.getGameConfig().isLinearSrgbImagesEnable());

        float[] eax = new float[]{15, 38.0f, 0.300f, -1000, -3300, 0,
                1.49f, 0.54f, 1.00f, -2560, 0.162f, 0.00f, 0.00f,
                0.00f, -229, 0.088f, 0.00f, 0.00f, 0.00f, 0.125f, 1.000f,
                0.250f, 0.000f, -5.0f, 5000.0f, 250.0f, 0.00f, 0x3f};
        Environment env = new Environment(eax);
        audioRenderer.setEnvironment(env);

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
    }

    @Override
    public void gainFocus() {
        super.gainFocus();
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
    }

    @Override
    public void requestClose(boolean esc) {
        onExit();
    }

    public void setScene(Node nextNode) {
        this.currentModeNode = nextNode;

        // сразу подменяем дефолтный debug info state на свой state:
        if (!stateManager.hasState(stateManager.getState(DebugInfoState.class))) {
            stateManager.detach(stateManager.getState(StatsAppState.class));
            stateManager.attach(new DebugInfoState(this.currentModeNode, gameControllerService));
        }

        if (this.currentModeNode.getName().equals(NodeNames.MENU_SCENE.name())) {
            // если загружается меню - удаляем игру и камеру:
            rootNode.detachChildNamed(NodeNames.GAME_SCENE.name());
            stateManager.attach(new MenuBackgState(this.currentModeNode, gameControllerService));

            // перевод курсора в режим меню:
            setAltControlMode(true);
        } else {
            // если загружается игра:
            rootNode.detachChildNamed(NodeNames.MENU_SCENE.name());

            // перевод курсора в режим игры:
            setAltControlMode(false);
        }

        log.info("Loading the scene '{}'...", this.currentModeNode.getName());
        rootNode.attachChild(this.currentModeNode);
    }

    private void toggleFullscreen() {
        AppSettings settings = context.getSettings();
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
            }
        }

//        guiNode.detachChildNamed(NodeNames.UI_TEXT_NODE.name());
//        gameControllerService.getSceneController().setupText(currentModeNode);

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек
    }

    private void toggleLSI() {
        if (renderer.isLinearizeSrgbImages()) {
            log.info("LSI: off");
            Constants.getGameConfig().setLinearSrgbImagesEnable(false);
        } else {
            log.info("LSI: on");
            Constants.getGameConfig().setLinearSrgbImagesEnable(true);
        }
        restart();
    }

    private void onGameRestore() {
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            return;
        }

        if (Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Controls.setPaused(false);
            log.debug("Resume game...");
        }
    }

    private void onGameHide() {
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            return;
        }

        log.debug("Hide or minimized");
        if (!Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Controls.setPaused(true);
            log.debug("Paused...");
        }
    }

    private void onExit() {
        if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        ) {
            stop();
            gameControllerService.exitTheGame(null, 0);
        } else {
            // Не закрываем окно:
            GLFW.glfwSetWindowShouldClose(((LwjglWindow) context).getWindowHandle(), false);
        }
    }

    private void setAltControlMode(boolean altMode) {
        if (altMode) {
//            mouseInput.setNativeCursor(new JmeCursor());
            stateManager.detach(stateManager.getState(FlyCamAppState.class));
            mouseInput.setCursorVisible(true);
            flyCam.setDragToRotate(false);
        } else {
            stateManager.attach(new FlyCamAppState());
            mouseInput.setCursorVisible(false);
            flyCam.setDragToRotate(true);
        }
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

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        System.err.println("\nAction: " + name + "; pressed: " + isPressed + "; tpf: " + tpf);

        switch (name) {
            case "ExitAction", "TogglePause" -> {
                WorldDto w = gameControllerService.getWorldService().getCurrentWorld();
                if (w != null && w.isNetAvailable() && Controls.isGameActive()) {
                    Controls.setPaused(!Controls.isPaused());
                } else {
                    onExit();
                }
            }
            case "ToggleFullscreen" -> toggleFullscreen(); // зависает нажатое событие в слушателе!
            case "MouseL" -> log.info("ЛКМ нажата: {}", isPressed);
            case "ToggleGameInfo" -> stateManager.getState(DebugInfoState.class).toggleStats();
            case "ToggleSLI" -> toggleLSI();
            case "ToggleAmbientLight" ->
                    currentModeNode.getWorldLightList().get(0).setEnabled(!currentModeNode.getWorldLightList().get(0).isEnabled());
            case "Shoot" -> {
                AudioNode shot = ((AudioNode) currentModeNode.getChild("gun"));
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
}
