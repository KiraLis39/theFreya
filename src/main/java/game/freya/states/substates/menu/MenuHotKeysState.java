package game.freya.states.substates.menu;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.RootNodeAppState;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.JMEApp;
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
    private JMEApp appRef;
    private ActionListener actList;
    private AnalogListener anlList;
    private SimpleApplication app;
    private Camera cam;
    private Node menuNode;

    public MenuHotKeysState(Node menuNode) {
        super(MenuHotKeysState.class.getSimpleName());
        this.menuNode = menuNode;
    }

    @Override
    public void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (JMEApp) this.app;
        this.inputManager = this.app.getInputManager();
        this.cam = this.app.getCamera();

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
            settings.setResolution(settings.getMinWidth(), settings.getMinHeight());
            //settings.setFullscreen(false);
            settings.setCenterWindow(true);
            FoxVideoMonitorUtil.setFullscreen(null);
//            Constants.getGameFrame().setUndecorated(false);
            Constants.getGameFrame().setSize(new Dimension(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight() + 30));
//            Constants.getGameFrame().pack();
            Constants.getGameFrame().setState(Frame.NORMAL);
            Constants.getGameFrame().setLocationRelativeTo(null);
        } else if (FoxVideoMonitorUtil.isFullScreenSupported()) {
            switch (Constants.getUserConfig().getFullscreenType()) {
                case EXCLUSIVE -> {
                    settings.setWindowSize(vMode.getWidth(), vMode.getHeight());
                    settings.setBitsPerPixel(vMode.getBitDepth());
                    settings.setFullscreen(true);
                    FoxVideoMonitorUtil.setFullscreen(Constants.getGameFrame());
                }
                case MAXIMIZE_WINDOW -> {
//                    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
//                    glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

//                    Constants.getGameFrame().dispose();
//                    Constants.getGameFrame().setUndecorated(true);
//                    Constants.getGameFrame().setVisible(true);
//                    Constants.getGameFrame().setSize(vMode.getWidth(), vMode.getHeight());
                    Constants.getGameFrame().setLocationRelativeTo(null);
                    Constants.getGameFrame().setState(Frame.MAXIMIZED_BOTH);
                    settings.setResolution(vMode.getWidth(), vMode.getHeight());
                    settings.setCenterWindow(true);
//                    settings.setWindowXPosition(3);
//                    settings.setWindowYPosition(15);
//                    GLFWVidMode glMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//                    glfwSetWindowMonitor(((LwjglWindow) context).getWindowHandle(), glfwGetPrimaryMonitor(), 0, 0, glMode.width(), glMode.height(), 60);
//                    settings.setCenterWindow(false);
                }
                case null, default ->
                        log.error("Некорректное указание режима окна '{}'", Constants.getUserConfig().getFullscreenType());
            }
        }

        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
        this.app.restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек

        // сброс расположения debug full info:
        this.app.enqueue(() -> getStateManager().getState(DebugInfoState.class).rebuildFullText());
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
                case "Click" -> {
                    log.info("\nProcess [LMBTest]: %.3f".formatted(value));

                    // Reset results list.
                    CollisionResults results = new CollisionResults();
                    // Convert screen click to 3d position
                    Vector3f click3d = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0f).clone();
                    Vector3f dir = cam.getWorldCoordinates(inputManager.getCursorPosition(), 1f)
                            .subtractLocal(click3d).normalizeLocal();
                    // Aim the ray from the clicked spot forwards.
                    Ray ray = new Ray(click3d, dir);
                    // Collect intersections between ray and all nodes in results list.
                    menuNode.collideWith(ray, results);
                    // (Print the results so we see what is going on:)
                    for (int i = 0; i < results.size(); i++) {
                        // (For each "hit", we know distance, impact point, geometry.)
                        float dist = results.getCollision(i).getDistance();
                        Vector3f pt = results.getCollision(i).getContactPoint();
                        String target = results.getCollision(i).getGeometry().getName();
                        log.info("Selection #{}: {} at {}, {} WU away.", i, target, pt, dist);
                    }
                    // Use the results -- we rotate the selected geometry.
                    if (results.size() > 0) {
                        // The closest result is the target that the player picked:
                        Geometry target = results.getClosestCollision().getGeometry();
                        // Here comes the action:
                        if (target.getName().equals("Red Box")) {
                            target.rotate(0, -tpf, 0);
                        } else if (target.getName().equals("Blue Box")) {
                            target.rotate(0, tpf, 0);
                        } else {
                            target.rotate(0, tpf * 2, 0);
                        }
                    }
                }
                case null, default -> log.warn("Не релизован процесс [{}] ({})", name, value * tpf);
            }
        }
    }
}
