package game.freya.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.panes.GameWindowJME;
import game.freya.services.GameControllerService;
import game.freya.states.substates.menu.MenuBackgState;
import game.freya.states.substates.menu.MenuHotKeysState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MainMenuState extends BaseAppState {
    private ExecutorService executor;
    private SimpleApplication app;
    private GameWindowJME appRef;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private GameControllerService gameControllerService;
    private Camera cam;
    private FlyByCamera flyByCamera;
    private MenuHotKeysState hotKeysState;
    private MenuBackgState backgState;
    private Node menuNode, rootNode;

    public MainMenuState(GameControllerService gameControllerService) {
        super(MainMenuState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (GameWindowJME) this.app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.flyByCamera = this.app.getFlyByCamera();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        buildMenu();
    }

    @Override
    protected void onEnable() {
        if (this.executor == null) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        }
        if (menuNode == null || !rootNode.hasChild(menuNode)) {
            buildMenu();
        }

        // включаем фоновую музыку меню:
        backgState = new MenuBackgState(menuNode);
        stateManager.attach(backgState);

        // подключаем модуль горячих клавиш:
        hotKeysState = new MenuHotKeysState();
        if (stateManager.attach(hotKeysState)) {
            executor.execute(() -> {
                stateManager.getState(hotKeysState.getClass()).startingAwait();
                // перевод курсора в режим меню:
                appRef.setAltControlMode(true);
            });
            log.info("Запуск главного меню произведён!");
        }
    }

    @Override
    protected void onDisable() {
        this.executor.shutdown();
        this.rootNode.detachChild(menuNode);
        this.stateManager.detach(backgState);
        this.stateManager.detach(hotKeysState);
        this.stateManager.detach(this);
    }

    private void buildMenu() {
        menuNode = new Node(NodeNames.MENU_SCENE_NODE.name());

        setupMenuCamera(menuNode);

        // content:
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
        Spatial menu = new Geometry("MenuBox", new Box(Vector3f.ZERO, new Vector3f(1, 1, 1)));
        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", assetManager.loadTexture("images/necessary/menu.png"));
        //  mat_brick.setColor("Color", ColorRGBA.Blue);
        menu.setMaterial(mat_menu);
        menu.setLocalTranslation(-1.5f, -1.0f, 0.0f);
        menu.setLocalScale(3.25f, 2, 0.1f);
//        menu.setLodLevel(0);
        menuNode.attachChild(menu);

        setupMenuLights(menuNode);

        setupMenuAudio(menuNode);

        rootNode.attachChild(menuNode);
    }

    private void setupMenuAudio(Node menuNode) {
        AudioNode audio_gun = new AudioNode(assetManager, "sound/weapon/sniper_rifle.wav", AudioData.DataType.Buffer);
        audio_gun.setName("gun");
        audio_gun.setPositional(false);
        audio_gun.setReverbEnabled(true); // эффект 3D-эха, который имеет смысл только с позиционными AudioNodes
        audio_gun.setLooping(false);
        audio_gun.setVolume(2);
        menuNode.attachChild(audio_gun);

        menuNode.setUserData("bkgRain", "sound/weather/rain1.ogg");
    }

    private void setupMenuCamera(Node menuNode) {
        // для меню отключено
        flyByCamera.setEnabled(false);

        cam.setLocation(new Vector3f(0.f, 0.f, 2.5f));
        cam.lookAt(new Vector3f(0.f, 0.f, -1.f), Vector3f.UNIT_Y);
    }

    private void setupMenuLights(Node menuNode) {
        menuNode.addLight(new AmbientLight() {{
            setName("Ambient light");
            setEnabled(true);
            setColor(ColorRGBA.fromRGBA255(127, 127, 127, 255));
            setFrustumCheckNeeded(false);
            setIntersectsFrustum(false);
        }});
    }

    @Override
    protected void cleanup(Application app) {
        this.rootNode.detachChild(menuNode);
        this.executor.shutdownNow();
    }
}
