package game.freya.gui.panes;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
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
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Setter
@Getter
public class GameWindowJME extends SimpleApplication {
    private volatile boolean isReady;
    private Node scene;
    private GameControllerService gameControllerService;
    private final List<String> eventNames = new ArrayList<>();

    public GameWindowJME(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;
//        super(new StatsAppState(), new DebugKeysAppState());

        //        setCursor(Constants.getDefaultCursor());
        //        addWindowListener(new WindowAdapter() {
        //            public void windowClosing(WindowEvent e) {
        //                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
        //                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        //                ) {
        //                    gameControllerService.exitTheGame(null, 0);
        //                }
        //            }
        //
        //            public void windowClosed(WindowEvent e) {
        //                log.debug("Окно закрыто!");
        //            }
        //
        //            public void windowIconified(WindowEvent e) {
        //                onGameHide();
        //            }
        //
        //            public void windowDeiconified(WindowEvent e) {
        //                onGameRestore();
        //            }
        //        });
        //        addWindowStateListener(new WindowAdapter() {
        //            public void windowStateChanged(WindowEvent e) {
        //                int oldState = e.getOldState();
        //                switch (e.getNewState()) {
        //                    case 6 -> {
        //                        log.info("Restored to fullscreen");
        //                        if ((oldState == 1 || oldState == 7)) {
        //                            onGameRestore();
        //                        }
        //                    }
        //                    case 0 -> {
        //                        log.info("Switch to windowed");
        //                        if ((oldState == 1 || oldState == 7)) {
        //                            onGameRestore();
        //                        }
        //                    }
        //                    case 1, 7 -> onGameHide();
        //                    default -> log.warn("MainMenu: Unhandled windows state: " + e.getNewState());
        //                }
        //            }
        //        });
        //
        //        setInAc();
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

        inputManager.addListener(new GameActionListener(), eventNames.toArray(new String[0]));
        inputManager.addListener(new GameAnalogListener());

        setReady(true);
    }

    /* Interact with game events in the main loop */
    // tpf большой на медленных ПК и маленький на быстрых ПК.
    @Override
    public void simpleUpdate(float tpf) {
        if (scene != null && scene.getName().equals(NodeNames.GAME_SCENE.name())) {
            scene.getChild("Box").rotate(0.003f, 0.003f, 0.003f);
        }

        // debug info:
        if (scene != null && Constants.getGameConfig().isDebugInfoVisible()) {
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
        this.scene = scene;

        if (scene.getName().equals(NodeNames.MENU_SCENE.name())) {
            // если загружается меню - удаляем игру и камеру:
            stateManager.detach(stateManager.getState(FlyCamAppState.class));

            flyCam.setDragToRotate(false);
            flyCam.setMoveSpeed(0);
            flyCam.setRotationSpeed(0);
            flyCam.setZoomSpeed(0);
            flyCam.setEnabled(true);

            rootNode.detachChildNamed(NodeNames.GAME_SCENE.name());

            // перевод курсора в режим меню:
//            setAltControlMode(true, glfwGetPrimaryMonitor());
        } else {
            // если загружается игра:
            rootNode.detachChildNamed(NodeNames.MENU_SCENE.name());

            // перевод курсора в режим игры:
//            setAltControlMode(false, glfwGetPrimaryMonitor());
        }

        log.info("Loading the scene '{}'...", scene.getName());
        rootNode.attachChild(scene);
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

    public void toggleFullscreen() {
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
        gameControllerService.getSceneController().setupText(scene);

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек
    }

    public void setAltControlMode(boolean altMode, long window) {
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        if (altMode) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
//            glfwSetCursor(window, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
//            glfwSetCursor(window, glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR));
            glfwSetCursor(window, glfwCreateStandardCursor(GLFW_HAND_CURSOR));
        } else {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
//            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
            glfwSetCursor(window, NULL);
        }
    }

    private void setInAc() {
        inputManager.addMapping("ExitAction", new KeyTrigger(KeyInput.KEY_ESCAPE));
        eventNames.add("ExitAction");

        inputManager.addMapping("ToggleStats", new KeyTrigger(KeyInput.KEY_F4));
        eventNames.add("ToggleStats");

        inputManager.addMapping("ToggleFullscreen", new KeyTrigger(KeyInput.KEY_F11));
        eventNames.add("ToggleFullscreen");

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
    }

    private class GameActionListener implements ActionListener {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            System.out.println("Name: " + name + "; pressed: " + isPressed + "; tpf: " + tpf);

            if (isPressed) {
                switch (name) {
                    case "ExitAction" -> onExit();
                    case "ToggleStats" -> {
                        if (Constants.getGameWindow().getStateManager().getState(StatsAppState.class) != null) {
                            Constants.getGameWindow().getStateManager().getState(StatsAppState.class).toggleStats();
                        }
                    }
                    case "ToggleFullscreen" -> toggleFullscreen();
                }
            }
        }
    }

    private void onExit() {
        if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти на рабочий стол?",
                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        ) {
            super.stop();
            gameControllerService.exitTheGame(null, 0);
        } else {
            // Не закрываем окно:
            glfwSetWindowShouldClose(((LwjglWindow) context).getWindowHandle(), false);
        }
    }

    private class GameAnalogListener implements AnalogListener {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            System.out.println("Name: " + name + "; value: " + value + "; tpf: " + tpf);


        }
    }
}
