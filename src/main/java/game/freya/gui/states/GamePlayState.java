package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.Listener;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import game.freya.config.Constants;
import game.freya.enums.gui.CrosshairType;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.JMEApp;
import game.freya.gui.states.substates.gameplay.GameplayHotKeysState;
import game.freya.gui.states.substates.global.OptionsState;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GamePlayState extends BaseAppState {
    private CrosshairType crosshairType = CrosshairType.SIMPLE_CROSS;
    private Node gameNode, rootNode, guiNode;
    private SimpleApplication app;
    private JMEApp appRef;
    private AppStateManager stateManager;
    private Listener listener;
    private AssetManager assetManager;
    private Camera cam;
    private FlyByCamera flyCam;
    private GameControllerService gameControllerService;
    private GameplayHotKeysState hotKeysState;
    private OptionsState optionsState;
    private ExecutorService executor;
    private BitmapFont crosshairFont;
    private BitmapText crosshair;
    private Geometry mark;

    public GamePlayState(GameControllerService gameControllerService) {
        super(GamePlayState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (JMEApp) this.app;
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.rootNode = this.app.getRootNode();
        this.guiNode = this.app.getGuiNode();
        this.stateManager = this.app.getStateManager();
        this.assetManager = this.app.getAssetManager();
        this.listener = this.app.getListener();

        this.app.getRenderer().setMainFrameBufferSrgb(true);
        this.app.getRenderer().setLinearizeSrgbImages(true);

        try {
            URL necUrl = getClass().getResource("/images/game/");
            assert necUrl != null;
            Constants.CACHE.addAllFrom(necUrl);
        } catch (Exception e) {
            log.error("Game state can not initialize. Resources exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return;
        }

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

        optionsState = new OptionsState(gameNode, gameControllerService);
        stateManager.attach(optionsState);

        // подключаем модуль горячих клавиш:
        this.hotKeysState = new GameplayHotKeysState(gameNode, gameControllerService);
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
        this.stateManager.detach(optionsState);
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
        this.crosshairFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        this.crosshair = new BitmapText(crosshairFont);

        this.gameNode = new Node(NodeNames.GAME_SCENE_NODE.name());

        setupGameCamera(gameNode);

        // content:
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

        initCrossHairs();

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
        gameNode.addLight(new AmbientLight() {{
            setName("Ambient light");
            setEnabled(true);
            setColor(ColorRGBA.fromRGBA255(127, 117, 120, 255));
            setFrustumCheckNeeded(true);
            setIntersectsFrustum(true);
        }});

        gameNode.addLight(new DirectionalLight() {{
            setName("Sun light");
            setEnabled(true);
            setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        }});
    }

    /**
     * A floor.
     */
    private Geometry makeFloor() {
        Box box = new Box(15, .2f, 15);
        Geometry floor = new Geometry("the Floor", box);
        floor.setLocalTranslation(0, -4, -5);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        floor.setMaterial(mat1);
        return floor;
    }

    /**
     * A centred plus sign to help the player aim.
     */
    private void initCrossHairs() {
        if (guiNode.hasChild(crosshair)) {
            guiNode.detachChild(crosshair);
        }

        AppSettings settings = this.app.getContext().getSettings();
//        setDisplayStatView(false);
        crosshair.setSize(crosshairFont.getCharSet().getRenderedSize() * 2);
        crosshair.setText(crosshairType.getView());
        crosshair.setLocalTranslation(
                settings.getWidth() / 2f - crosshair.getLineWidth() / 2f,
                settings.getHeight() / 2f + crosshair.getLineHeight() / 2f, 0);
        guiNode.attachChild(crosshair);
    }

    public void setCrosshairType(CrosshairType crosshairType) {
        this.crosshairType = crosshairType;
        initCrossHairs();
    }

    @Override
    protected void cleanup(Application app) {
        this.rootNode.detachChild(gameNode);
        this.executor.shutdownNow();
    }
}
