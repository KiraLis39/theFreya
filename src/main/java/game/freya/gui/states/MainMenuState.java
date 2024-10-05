package game.freya.gui.states;

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
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import game.freya.annotations.DevelopOnly;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.gui.states.substates.menu.MenuBackgState;
import game.freya.gui.states.substates.menu.MenuHotKeysState;
import game.freya.gui.states.substates.menu.spatials.MenuBackgroundImage;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сцена главного меню игры.
 * Здесь собран лишь контент для главного меню - фоновая музыка меню, горячие клавиши меню, фоновый рисунок или видео,
 * аватар.
 */
@Slf4j
public class MainMenuState extends BaseAppState {
    private GameControllerService gameControllerService;
    private SimpleApplication app;
    private ExecutorService executor;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private Camera cam;
    private FlyByCamera flyByCamera;
    private MenuHotKeysState menuHotKeysState;
    private MenuBackgState menuBackgroundMusicState;
    private Node menuNode, rootNode, guiNode;
    private Spatial centerMarker, cmr, cml, avatarGeo;

    @Setter
    @Getter
    private volatile float fov = 45.0f;
    private Renderer renderer;

    public MainMenuState(GameControllerService gameControllerService) {
        super(MainMenuState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        this.rootNode = this.app.getRootNode();
        this.flyByCamera = this.app.getFlyByCamera();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.renderer = this.app.getRenderer();
    }

    @Override
    protected void cleanup(Application app) {
        this.executor.shutdownNow();
        this.menuNode.detachAllChildren();
        this.rootNode.detachChild(menuNode);
        this.stateManager.detach(this);
    }

    @Override
    protected void onEnable() {
        // check network connections closed:
        if (Constants.getServer() != null && Constants.getServer().isOpen()) {
            gameControllerService.closeConnections();
            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
        }
        if (Constants.getLocalSocketConnection() != null && Constants.getLocalSocketConnection().isOpen()) {
            Constants.getLocalSocketConnection().close();
            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
        }

        // create executor:
        if (this.executor == null) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        }

        // build the main menu:
        if (rootNode.hasChild(menuNode)) {
            menuNode = (Node) rootNode.getChild(NodeNames.MENU_SCENE_NODE.name());
        }
        if (menuNode == null || !rootNode.hasChild(menuNode)) {
            buildMenu();
        }

        // включаем фоновую музыку меню:
        menuBackgroundMusicState = new MenuBackgState(menuNode);
        stateManager.attach(menuBackgroundMusicState);

        // подключаем модуль горячих клавиш:
        menuHotKeysState = new MenuHotKeysState(menuNode);
        if (stateManager.attach(menuHotKeysState)) {
            executor.execute(() -> {
                menuHotKeysState.startingAwait();
                // перевод курсора в режим меню:
                Constants.getGameCanvas().setAltControlMode(true);
            });
            log.info("Запуск главного меню произведён!");
        }
    }

    @Override
    protected void onDisable() {
        this.executor.shutdown();
        this.rootNode.detachChild(menuNode);
        this.stateManager.detach(menuBackgroundMusicState);
        this.stateManager.detach(menuHotKeysState);
    }

    private void buildMenu() {
        setupMenuCamera();

        menuNode = new Node(NodeNames.MENU_SCENE_NODE.name());
        menuNode.attachChild(new MenuBackgroundImage(assetManager));

        setupMenuAudio(menuNode);
//        setupMenuLights(menuNode);
        setupGui();

        rootNode.attachChild(menuNode);
    }

    public void setupGui() {
        if (guiNode == null) {
            return;
        }
        if (guiNode.hasChild(avatarGeo)) {
            guiNode.detachChild(avatarGeo);
        }

        int avatarDim = 128;
        BufferedImage avatarBImage = drawAvatarImage(avatarDim);
//        AWTLoader imgLoader = new AWTLoader();
//        Image load = imgLoader.load(avatarBImage, false);

        avatarGeo = new Geometry("MenuAvatar", new Quad(avatarDim, avatarDim));
        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", new Texture2D(new AWTLoader().load(avatarBImage, true)));
        mat_menu.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        avatarGeo.setMaterial(mat_menu);
//        avatarGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        avatarGeo.setLocalTranslation(cam.getWidth() - avatarDim - 3f, cam.getHeight() - avatarDim - 6f, 0.01f);
        avatarGeo.setCullHint(Spatial.CullHint.Inherit);
        guiNode.attachChild(avatarGeo);

//        createGreenMarkers();
    }

    private BufferedImage drawAvatarImage(int avatarDim) {
        String playerNickName = gameControllerService.getPlayerService().getCurrentPlayer().getNickName();
        BufferedImage result = new BufferedImage(avatarDim + 4, avatarDim + 24, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2D = result.createGraphics();
        g2D.drawImage(gameControllerService.getPlayerService().getCurrentPlayer().getAvatar(), 2, 2, avatarDim, avatarDim, null);
        g2D.setColor(Color.BLACK);
        g2D.setStroke(new BasicStroke(3f));
        g2D.drawRoundRect(1, 1, avatarDim - 1, avatarDim + 2, 8, 8);
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(playerNickName, (int) (avatarDim / 2 - Constants.FFB.getHalfWidthOfString(g2D, playerNickName)) + 1, avatarDim + 25);
        g2D.setColor(Color.LIGHT_GRAY);
        g2D.drawString(playerNickName, (int) (avatarDim / 2 - Constants.FFB.getHalfWidthOfString(g2D, playerNickName)), avatarDim + 24);
        g2D.dispose();

        return result;
    }

    @DevelopOnly
    private void createGreenMarkers() {
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

    public void setupMenuCamera() {
        flyByCamera.setEnabled(false);
//        flyByCamera.setMoveSpeed(0.01f);
//        flyByCamera.setZoomSpeed(0.1f);

        cam.setFrustumPerspective(fov, (float) Constants.getCurrentScreenAspect(), 0.01f, 1000f);
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
}
