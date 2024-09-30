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
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.util.BufferUtils;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.panes.JMEApp;
import game.freya.services.GameControllerService;
import game.freya.states.substates.menu.MenuBackgState;
import game.freya.states.substates.menu.MenuHotKeysState;
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
        setupMenuCamera();

        menuNode = new Node(NodeNames.MENU_SCENE_NODE.name());

        // content:
        float menuWidth = cam.getWidth() / 712f, menuHeight = (float) (menuWidth / Constants.getCurrentScreenAspect());
        log.info("Camera width {}, MenuBox width: {}", cam.getWidth(), menuWidth);
        Spatial menu = new Geometry("MenuBox", new Quad(menuWidth, menuHeight));
        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", assetManager.loadTexture("images/necessary/menu.png"));
        menu.setMaterial(mat_menu);
        menu.setLocalTranslation(-menuWidth / 2f, -menuHeight / 2f, 0);
        menu.setCullHint(Spatial.CullHint.Dynamic);
        menuNode.attachChild(menu);

        Vector2f[] vertices = new Vector2f[4];
        // Vector2f[] texCoord = new Vector2f[4];

        vertices[0] = new Vector2f(-1, -0.57f);
        // texCoord[0] = new Vector2f(0,0);

        vertices[1] = new Vector2f(-0.59f, -0.57f);
        // texCoord[1] = new Vector2f(0.5f,0);

        vertices[2] = new Vector2f(-1, 0.57f);
        // texCoord[2] = new Vector2f(0,0.5f);

        vertices[3] = new Vector2f(-0.5f, 0.57f);
        // texCoord[3] = new Vector2f(0.5f,0.5f);

        int[] indexes = {2, 0, 1, 1, 3, 2};
        // Этот синтаксис означает:
        //  Индексы 0,1,2,3 обозначают четыре вершины, которые вы указали для четырехугольника в vertices[].
        //  Треугольник 2,0,1 начинается слева вверху, продолжается слева внизу и заканчивается справа внизу.
        //  Треугольник 1,3,2 начинается внизу справа, продолжается вверху справа и заканчивается вверху слева.
        Mesh grayCorner = new Mesh(); // menuWidth / 4, menuHeight
        grayCorner.setBuffer(VertexBuffer.Type.Position, 2, BufferUtils.createFloatBuffer(vertices));
//        grayCorner.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        grayCorner.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));
        grayCorner.updateBound();

        Spatial grayPane = new Geometry("GrayMenuPanel", grayCorner);
        Material mat_gp = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_gp.setColor("Color", ColorRGBA.fromRGBA255(0, 0, 0, 223));
        mat_gp.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        grayPane.setQueueBucket(RenderQueue.Bucket.Transparent);
        grayPane.setMaterial(mat_gp);
//        grayPane.setLocalTranslation(-(menuWidth / 2), -menuHeight / 2, 0);
        grayPane.setCullHint(Spatial.CullHint.Dynamic);
//        grayPane.setLodLevel(0);
        menuNode.attachChild(grayPane);

        setupMenuAudio(menuNode);

        setupMenuLights(menuNode);

        setupGui();

        rootNode.attachChild(menuNode);
    }

    private Spatial centerMarker, cmr, cml;

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
