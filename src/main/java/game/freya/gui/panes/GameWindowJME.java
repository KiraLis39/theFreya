package game.freya.gui.panes;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.font.BitmapText;
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
import game.freya.enums.NodeNames;
import game.freya.gui.states.MenuHotKeysState;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

@Slf4j
@Setter
@Getter
public class GameWindowJME extends SimpleApplication implements ActionListener, AnalogListener {
    private final List<String> events = new ArrayList<>();
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
        start(true);
        Constants.getSoundPlayer().play("landing");
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        BufferUtils.setTrackDirectMemoryEnabled(true);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);

        setInAc();

//        mouseInput.setInputListener(new MyMouseListenerAdapter() {
//            @Override
//            public void onMouseMotionEvent(MouseMotionEvent evt) {
//                // Mouse motion data: MouseMotion(X=629, Y=516, DX=1, DY=0, Wheel=0, dWheel=0)
//
//            }
//
//            @Override
//            public void onMouseButtonEvent(MouseButtonEvent evt) {
//                // Mouse button data: MouseButton(BTN=1, RELEASED) | MouseButton(BTN=2, PRESSED)
//
//            }
//        });

        getStateManager().attach(new MenuHotKeysState(gameControllerService));

        setReady(true);
    }

    /* Interact with game events in the main loop */
    // tpf большой на медленных ПК и маленький на быстрых ПК.
    @Override
    public void simpleUpdate(float tpf) {
        if (currentModeNode != null && currentModeNode.getName().equals(NodeNames.GAME_SCENE.name())) {
            currentModeNode.getChild("Box").rotate(0.003f * tpf, 0.003f * tpf, 0.003f * tpf);
        }

        // debug info:
        if (currentModeNode != null && Constants.getGameConfig().isDebugInfoVisible()) {
            showDebugInfo();
        }
    }

    private void showDebugInfo() {
        ((BitmapText) guiNode.getChild("samplesText")).setText("MultiSamplingLevel: %s".formatted(getContext().getSettings().getSamples()));
        ((BitmapText) guiNode.getChild("vsyncText")).setText("VSync: %s".formatted(getContext().getSettings().isVSync()));
        ((BitmapText) guiNode.getChild("fpsLimitText")).setText("FPS limit: %s".formatted(getContext().getSettings().getFrameRate()));
        ((BitmapText) guiNode.getChild("sndOnText")).setText("Sounds: %s".formatted(Constants.getUserConfig().isSoundEnabled()));
        ((BitmapText) guiNode.getChild("mscOnText")).setText("Music: %s".formatted(Constants.getUserConfig().isMusicEnabled()));
        ((BitmapText) guiNode.getChild("fcamMoveSpeed")).setText("FlyCam move speed: %s".formatted(flyCam.getMoveSpeed()));
        ((BitmapText) guiNode.getChild("fcamRotSpeed")).setText("FlyCam rotation speed: %s".formatted(flyCam.getRotationSpeed()));
        ((BitmapText) guiNode.getChild("fcamZoomSpeed")).setText("FlyCam zoom speed: %s".formatted(flyCam.getZoomSpeed()));
        ((BitmapText) guiNode.getChild("camRotText")).setText("Cam rotation: %s".formatted(cam.getRotation()));
        ((BitmapText) guiNode.getChild("camDirText")).setText("Cam direction: %s".formatted(cam.getDirection()));
        ((BitmapText) guiNode.getChild("camPosText")).setText("Cam position: %s".formatted(cam.getLocation()));
        ((BitmapText) guiNode.getChild("flscrnText")).setText("Fullscreen: %s"
                .formatted(getContext().getSettings().isFullscreen() && Constants.getUserConfig().isFullscreen() ? "true"
                        : !getContext().getSettings().isFullscreen() && Constants.getUserConfig().isFullscreen() ? "pseudo" : "false"));
        ((BitmapText) guiNode.getChild("winWidth")).setText("Width: %s".formatted(settings.getWidth()));
        ((BitmapText) guiNode.getChild("winHeight")).setText("Height: %s".formatted(settings.getHeight()));
        ((BitmapText) guiNode.getChild("wwinWidth")).setText("wWidth: %s".formatted(settings.getWindowWidth()));
        ((BitmapText) guiNode.getChild("wwinHeight")).setText("wHeight: %s".formatted(settings.getWindowHeight()));
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

    public void setScene(Node scene) {
        this.currentModeNode = scene;

        if (scene.getName().equals(NodeNames.MENU_SCENE.name())) {
            // если загружается меню - удаляем игру и камеру:
            rootNode.detachChildNamed(NodeNames.GAME_SCENE.name());

            // перевод курсора в режим меню:
            setAltControlMode(true);
        } else {
            // если загружается игра:
            rootNode.detachChildNamed(NodeNames.MENU_SCENE.name());

            // перевод курсора в режим игры:
            setAltControlMode(false);
        }

        log.info("Loading the scene '{}'...", scene.getName());
        rootNode.attachChild(scene);
    }

    private void setInAc() {
        inputManager.addMapping("ExitAction", new KeyTrigger(KeyInput.KEY_ESCAPE));
        events.add("ExitAction");

        inputManager.addMapping("ToggleStats", new KeyTrigger(KeyInput.KEY_F4));
        events.add("ToggleStats");

        inputManager.addMapping("ToggleFullscreen", new KeyTrigger(KeyInput.KEY_F11));
        events.add("ToggleFullscreen");

        inputManager.addMapping("MouseL", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        events.add("MouseL");

//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchPause",
//                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the pause mode...");
//                        Controls.setPaused(!Controls.isPaused());
//                    }
//                });

//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
//                Constants.getUserConfig().getKeyDebug(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the debug mode...");
//                        Constants.getGameConfig().setDebugInfoVisible(!Constants.getGameConfig().isDebugInfoVisible());
//                    }
//                });

//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFps",
//                Constants.getUserConfig().getKeyFps(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fps mode...");
//                        Constants.getGameConfig().setFpsInfoVisible(!Constants.getGameConfig().isFpsInfoVisible());
//                    }
//                });

        inputManager.addListener(this, events.toArray(new String[0]));
    }

    public void toggleFullscreen() {
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

        guiNode.detachChildNamed(NodeNames.UI_TEXT_NODE.name());
        gameControllerService.getSceneController().setupText(currentModeNode);

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек
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

    public void onExit() {
        if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        ) {
            stop();
            gameControllerService.exitTheGame(null, 0);
        } else {
            // Не закрываем окно:
            glfwSetWindowShouldClose(((LwjglWindow) context).getWindowHandle(), false);
        }
    }

    public void setAltControlMode(boolean altMode) {
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

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        System.err.println("\nAction: " + name + "; pressed: " + isPressed + "; tpf: " + tpf);

        switch (name) {
            case "ExitAction" -> onExit();
            case "ToggleStats" -> {
                if (Constants.getGameWindow().getStateManager().getState(StatsAppState.class) != null) {
                    Constants.getGameWindow().getStateManager().getState(StatsAppState.class).toggleStats();
                }
            }
            case "ToggleFullscreen" -> toggleFullscreen(); // зависает нажатое событие в слушателе!
            case "MouseL" -> log.info("ЛКМ нажата: {}", isPressed);
            default -> log.warn("Не релизовано действие {}", name);
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
//        System.out.println("Process: " + name + "; value: " + value + "; tpf: " + tpf);

//        switch (name) {
//            default -> log.warn("Не релизовано действие {}", name);
//        }
    }
}
