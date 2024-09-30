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
import com.jme3.scene.shape.Quad;
import game.freya.annotations.DevelopOnly;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.panes.JMEApp;
import game.freya.services.GameControllerService;
import game.freya.states.substates.menu.MenuBackgState;
import game.freya.states.substates.menu.MenuHotKeysState;
import game.freya.states.substates.menu.spatials.GrayMenuCorner;
import game.freya.states.substates.menu.spatials.MenuBackgroundImage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MainMenuState extends BaseAppState {
    private ExecutorService executor;
    private SimpleApplication app;
    private JMEApp appRef;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private Camera cam;
    private FlyByCamera flyByCamera;
    private MenuHotKeysState hotKeysState;
    private MenuBackgState backgState;
    private Node menuNode, rootNode, guiNode;
    private Spatial centerMarker, cmr, cml;

    @Setter
    @Getter
    private volatile float fov = 45.0f;

    public MainMenuState(GameControllerService gameControllerService) {
        super(MainMenuState.class.getSimpleName());
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.appRef = (JMEApp) this.app;
        this.cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        this.rootNode = this.app.getRootNode();
        this.flyByCamera = this.app.getFlyByCamera();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();

        this.app.getRenderer().setMainFrameBufferSrgb(true);
        this.app.getRenderer().setLinearizeSrgbImages(true);

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
        hotKeysState = new MenuHotKeysState(menuNode);
        if (stateManager.attach(hotKeysState)) {
            executor.execute(() -> {
                hotKeysState.startingAwait();
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
        setupMenuCamera();

        menuNode = new Node(NodeNames.MENU_SCENE_NODE.name());
        menuNode.attachChild(new MenuBackgroundImage(assetManager, cam));
        menuNode.attachChild(new GrayMenuCorner(assetManager));

        setupMenuAudio(menuNode);
        setupMenuLights(menuNode);
        setupGui();

        rootNode.attachChild(menuNode);
    }

    @DevelopOnly
    public void setupGui() {
        if (guiNode == null) {
            return;
        }
        if (guiNode.hasChild(centerMarker)) {
            guiNode.detachChild(centerMarker);
        }
        if (guiNode.hasChild(cmr)) {
            guiNode.detachChild(cmr);
        }
        if (guiNode.hasChild(cml)) {
            guiNode.detachChild(cml);
        }

        float cpWidth = 4.f, cpHeight = cpWidth * 2f;
        centerMarker = new Geometry("GreenCenterMarker", new Quad(cpWidth, cpHeight));
        Material mat_center = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_center.setColor("Color", ColorRGBA.Green);
        centerMarker.setMaterial(mat_center);
        centerMarker.setLocalTranslation(cam.getWidth() / 2f - cpWidth / 2f, cam.getHeight() / 2f - cpHeight / 2f, 0);
        guiNode.attachChild(centerMarker);

        cmr = centerMarker.clone();
        cmr.setLocalTranslation(cam.getWidth() / 4f - cpWidth / 2f, cam.getHeight() / 2f - cpHeight / 2f, 0);
        guiNode.attachChild(cmr);

        cml = centerMarker.clone();
        cml.setLocalTranslation(cam.getWidth() * 0.75f - cpWidth / 2f, cam.getHeight() / 2f - cpHeight / 2f, 0);
        guiNode.attachChild(cml);
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

    private void setupMenuCamera() {
        // для меню отключено (иначе можно случайно сбить движением мыши при запуске игры)
        flyByCamera.setEnabled(false);
//        flyByCamera.setMoveSpeed(0.01f);
//        flyByCamera.setZoomSpeed(0.1f);
        cam.setFrustumPerspective(fov, (float) Constants.getCurrentScreenAspect(), 0.25f, 1.5f);
        cam.setLocation(new Vector3f(0.f, 0.f, 1.354f));
        cam.lookAt(new Vector3f(0.f, 0.f, -1.f), Vector3f.UNIT_Y);
    }

    private void setupMenuLights(Spatial aim) {
        aim.addLight(new AmbientLight() {{
            setName("Ambient light");
            setEnabled(true);
            setColor(ColorRGBA.fromRGBA255(255, 255, 255, 255));
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
