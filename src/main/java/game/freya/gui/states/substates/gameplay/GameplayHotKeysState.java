package game.freya.gui.states.substates.gameplay;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.RootNodeAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.config.UserConfig;
import game.freya.controls.combo.ComboMove;
import game.freya.controls.combo.ComboMoveExecution;
import game.freya.dto.roots.WorldDto;
import game.freya.gui.JMEApp;
import game.freya.gui.states.global.DebugInfoState;
import game.freya.gui.states.global.ExitHandlerState;
import game.freya.gui.states.global.OptionsState;
import game.freya.services.GameControllerService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Slf4j
public class GameplayHotKeysState extends BaseAppState {
    private final GameControllerService gameControllerService;
    private final List<String> actions = new ArrayList<>();
    private final List<String> processes = new ArrayList<>();
    private final Node gameNode;
    private List<ComboMoveExecution> combos;
    private UserConfig.Hotkeys hotKeys;
    private InputManager inputManager;
    private SimpleApplication app;
    private JMEApp appRef;
    private ActionListener actList;
    private AnalogListener anlList;
    private Renderer renderer;
    private Camera cam;
    private Geometry mark;
    private AssetManager assetManager;
    private OptionsState optionsState;

    public GameplayHotKeysState(Node gameNode, GameControllerService gameControllerService) {
        super(GameplayHotKeysState.class.getSimpleName());
        this.gameNode = gameNode;
        this.gameControllerService = gameControllerService;
    }

    @Override
    public void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (JMEApp) app;
        this.cam = this.app.getCamera();
        this.renderer = this.app.getRenderer();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.actList = new MenuActionListener();
        this.anlList = new MenuAnalogListener();

        this.optionsState = this.app.getStateManager().getState(OptionsState.class);
    }

    @Override
    protected void onEnable() {
        // перезагружаем хоткеи из конфига игры:
        reloadHotKeys();
        // загружаем обновленные маппинги:
        setInAc();
        // прописываем игровые комбо:
        initCombos();

        initMark();
    }

    private void initCombos() {
        this.combos = new ArrayList<>();
        ComboMove testCombo = new ComboMove("testCombo") {
            {
                pressed("Down").released("Right").done();
                pressed("Right", "Down").done();
                pressed("Right").idle(0.11f).released("Down").done();
                released("Right", "Down").done();

                // Если есть неоднозначность, вместо низкоприоритетного комбо сработает высокоприоритетное комбо.
                // Это предотвращает «перехват» другого комбо похожим шагом комбо. Используйте только один раз за ComboMove.
                priority(0.5f);

                // Это последняя команда серии.
                // Ложь: Не ждать конечного состояния (объединить шаги комбо).
                // Истина: Это конечное состояние (не объединять шаги комбо).
                isFinalState(false);
            }
        };
        this.combos.add(new ComboMoveExecution(testCombo));
    }

    @Override
    protected void onDisable() {
        combos.clear();
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

        inputManager.addMapping("CurAltMode", new KeyTrigger(hotKeys.getKeyAltCursorMode().getJmeKey()));
        actions.add("CurAltMode");

        // debug:
        inputManager.addMapping("ToggleGameInfo", new KeyTrigger(hotKeys.getKeyDebugInfo().getJmeKey()));
        actions.add("ToggleGameInfo");

        inputManager.addMapping("ToggleSLI", new KeyTrigger(KeyInput.KEY_F9));
        actions.add("ToggleSLI");

        inputManager.addMapping("ToggleAmbientLight", new KeyTrigger(KeyInput.KEY_F10));
        actions.add("ToggleAmbientLight");

        // mouse actions:
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        actions.add("Shoot");
        processes.add("Shoot");

        // mouse test:
        inputManager.addMapping("MATTest", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        processes.add("MATTest");

        inputManager.addMapping("MWTTest", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        actions.add("MWTTest");

        // player moving:
        inputManager.addMapping("MoveForward", new KeyTrigger(hotKeys.getKeyMoveForward().getJmeKey()));
        processes.add("MoveForward");

        inputManager.addMapping("MoveLeft", new KeyTrigger(hotKeys.getKeyMoveLeft().getJmeKey()));
        processes.add("MoveLeft");

        inputManager.addMapping("MoveBack", new KeyTrigger(hotKeys.getKeyMoveBack().getJmeKey()));
        processes.add("MoveBack");

        inputManager.addMapping("MoveRight", new KeyTrigger(hotKeys.getKeyMoveRight().getJmeKey()));
        processes.add("MoveRight");

        // final:
        inputManager.addListener(actList, actions.toArray(new String[0]));
        inputManager.addListener(anlList, processes.toArray(new String[0]));
    }

    private void toggleLSI() {
        if (renderer.isLinearizeSrgbImages()) {
            log.info("LSI: off");
            Constants.getGameConfig().setLinearSrgbImagesEnable(false);
        } else {
            log.info("LSI: on");
            Constants.getGameConfig().setLinearSrgbImagesEnable(true);
        }
        this.app.restart();
    }

    /**
     * A red ball that marks the last spot that was "hit" by the "shot".
     */
    private void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
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
        private final HashSet<String> pressedMappings = new HashSet<>();
        private final float time = 0f;
        private ComboMove currentMove = null;
        private float currentMoveCastTime = 0;

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            // check cursor alt mode:
            if (name.equals("CurAltMode")) {
                appRef.setAltControlMode(isPressed);
            }

            if (!isPressed || !isEnabled()) {
                pressedMappings.remove(name);
                return;
            }

            // Record pressed mappings
            pressedMappings.add(name);

            // The pressed mappings have changed: Update ComboExecution objects
            List<ComboMove> invokedMoves = new ArrayList<>();
            if (combos.get(0).updateState(pressedMappings, time)) {
                invokedMoves.add(combos.get(0).getCombo());
            }
            // ... add more ComboExecs here...

            // If any ComboMoves have been successfully triggered:
            if (!invokedMoves.isEmpty()) {
                float priority = 0; // identify the move with highest priority.
                ComboMove toExec = null;
                for (ComboMove move : invokedMoves) {
                    if (move.priority() > priority) {
                        priority = move.priority();
                        toExec = move;
                    }
                }
                if (toExec != null) {
                    if (currentMove == null || currentMove.priority() > toExec.priority()) {
                        // If a ComboMove has been identified, store it in currentMove
                        currentMove = toExec;
                        currentMoveCastTime = currentMove.castTime();
                    }
                    // else skip lower-priority moves.
                }
            }

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
                case "ToggleFullscreen" ->
                        SwingUtilities.invokeLater(() -> Constants.getGameCanvas().toggleFullscreen()); // зависает нажатое событие в слушателе!
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

                    // 1. Reset results list.
                    CollisionResults results = new CollisionResults();

                    // 2. Aim the ray from cam loc to cam direction.
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());

                    // 3. Collect intersections between Ray and Shootables in results list.
                    gameNode.collideWith(ray, results);

                    // 4. Print the results
                    System.out.println("----- Collisions? " + results.size() + "-----");
                    for (int i = 0; i < results.size(); i++) {
                        // For each hit, we know distance, impact point, name of geometry.
                        float dist = results.getCollision(i).getDistance();
                        Vector3f pt = results.getCollision(i).getContactPoint();
                        String hit = results.getCollision(i).getGeometry().getName();
                        System.out.println("* Collision #" + i);
                        System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                    }

                    // 5. Use the results (we mark the hit object)
                    if (results.size() > 0) {
                        // The closest collision point is what was truly hit:
                        CollisionResult closest = results.getClosestCollision();
                        // Let's interact - we mark the hit with a red dot.
                        mark.setLocalTranslation(closest.getContactPoint());
                        gameNode.attachChild(mark);
                    } else {
                        // No hits? Then remove the red mark.
                        gameNode.detachChild(mark);
                    }
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
                case "Shoot" -> log.info("Process [LMBTest]: %.3f".formatted(value));
                case null, default -> log.warn("Не релизован процесс [{}] ({})", name, value * tpf);
            }

//        if (name.equals("Rotate")) {
//            player.rotate(0, value * speed, 0);
//        }
        }
    }
}
