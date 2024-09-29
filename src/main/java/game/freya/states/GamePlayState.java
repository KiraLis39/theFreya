package game.freya.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.Listener;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.panes.GameWindowJME;
import game.freya.services.GameControllerService;
import game.freya.states.substates.gameplay.GameplayHotKeysState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GamePlayState extends BaseAppState {
    private Node gameNode, rootNode;
    private SimpleApplication app;
    private GameWindowJME appRef;
    private AppStateManager stateManager;
    private Listener listener;
    private Camera cam;
    private FlyByCamera flyCam;
    private GameControllerService gameControllerService;
    private GameplayHotKeysState hotKeysState;
    private ExecutorService executor;

    public GamePlayState(GameControllerService gameControllerService) {
        super(GamePlayState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (GameWindowJME) this.app;
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.rootNode = this.app.getRootNode();
        this.listener = this.app.getListener();
        buildGame();
    }

    @Override
    protected void onEnable() {
        if (this.executor == null) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        }
        if (this.gameNode == null || !this.rootNode.hasChild(this.gameNode)) {
            buildGame();
        }

        // подключаем модуль горячих клавиш:
        this.hotKeysState = new GameplayHotKeysState(gameControllerService);
        if (stateManager.attach(hotKeysState)) {
            executor.execute(() -> {
                stateManager.getState(hotKeysState.getClass()).startingAwait();
                // перевод курсора в режим игры:
                appRef.setAltControlMode(false);
            });

            // отладка геометрии, шейдеров, перезагрузка материала и т.п.:
//        if (Constants.getGameConfig().isDebugInfoVisible()) {
//            MaterialDebugAppState matDebug = new MaterialDebugAppState();
//            stateManager.attach(matDebug);
//            matDebug.registerBinding(new KeyTrigger(KeyInput.KEY_R), anyGeometry);
//        }

            // renderer.setLinearizeSrgbImages(Constants.getGameConfig().isLinearSrgbImagesEnable());

            log.info("Запуск игры произведён!");
        }
    }

    @Override
    protected void onDisable() {
        this.executor.shutdown();
        this.rootNode.detachChild(gameNode);
        this.stateManager.detach(hotKeysState);
        this.stateManager.detach(this);
    }

    @Override
    public void update(float tpf) {
        gameNode.getChild("Box").rotate(0.003f * tpf, 0.003f * tpf, 0.003f * tpf);

        // first-person: keep the audio listener moving with the camera
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());

        super.update(tpf);
    }

    private void buildGame() {
        this.gameNode = new Node(NodeNames.GAME_SCENE_NODE.name());

        setupGameCamera(gameNode);

        // content:
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
//        Spatial landWithHouse = assetManager.loadModel("misc/wildhouse/main.mesh.xml"); // .j3o
//        gameNode.attachChild(landWithHouse);

        Spatial town = assetManager.loadModel("misc/town/level.mesh.xml"); // .j3o
        town.setLocalTranslation(0, -5.2f, 0);
        town.setLocalScale(2);
        gameNode.attachChild(town);

        Spatial ship = assetManager.loadModel("misc/ship/ship.obj");
        Material mat_default = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_default.setTexture("ColorMap", assetManager.loadTexture("misc/ship/ship.png"));
        //  mat_default.setColor("Color", ColorRGBA.Blue);
        ship.setMaterial(mat_default);
        gameNode.attachChild(ship);

        /* Load a Ninja model (OgreXML + material + texture from test_data) */
//        Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml"); // .j3o
//        ninja.scale(0.05f, 0.05f, 0.05f);
//        ninja.rotate(0.0f, -3.0f, 0.0f);
//        ninja.setLocalTranslation(0.0f, -5.0f, -2.0f);
//        gameNode.attachChild(ninja);

        // HttpZipLocator, который может скачивать сжатые модели и загружать их:
//        assetManager.registerLocator(wildhouseZipUrl, HttpZipLocator.class);
//        Spatial scene = assetManager.loadModel("main.scene");
//        gameNode.attachChild(scene);

        // FileLocator, который позволяет assetManager открывать файл актива из определенного каталога:
//        assetManager.registerLocator("<Path to directory containing asset>", FileLocator.class);
//        Spatial model = assetManager.loadModel("ModelName.gltf");
//        gameNode.attachChild(model);

        setupGameLights(gameNode);

        rootNode.attachChild(gameNode);
    }

    private void setupGameCamera(Node gameNode) {
//        AppSettings settings = Constants.getGameWindow().getContext().getSettings();
//        float aspect = (float) settings.getWindowWidth() / settings.getWindowHeight();

//        cam.setFov(Constants.getUserConfig().getFov());
//        cam.setFrustumNear(1.f); // default 1.0f
//        cam.setFrustumFar(1000); // default 1000
//        cam.setFrustum(0.25f, 1_000.f, -0.19f, 0.19f, 0.11f, -0.11f);
//        cam.setFrustumPerspective(Constants.getUserConfig().getFov(), aspect, 0.25f, 1_000.f);

        flyCam.setMoveSpeed(gameControllerService.getCharacterService().getCurrentHero().getSpeed());
        flyCam.setZoomSpeed(Constants.getGameConfig().getZoomSpeed());
    }

    private void setupGameLights(Node gameNode) {
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setName("Ambient light");
        ambientLight.setEnabled(true);
        ambientLight.setColor(ColorRGBA.fromRGBA255(127, 117, 120, 255));
        ambientLight.setFrustumCheckNeeded(true);
        ambientLight.setIntersectsFrustum(true);
        gameNode.addLight(ambientLight);

        /* You must add a light to make the model visible. */
        DirectionalLight sun = new DirectionalLight();
        sun.setName("Sun light");
        sun.setEnabled(true);
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        gameNode.addLight(sun);
    }

    @Override
    protected void cleanup(Application app) {
        this.rootNode.detachChild(gameNode);
        this.executor.shutdownNow();
    }
}
