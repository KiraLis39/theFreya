package game.freya.gui;

import com.jme3.asset.AssetManager;
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
import game.freya.enums.other.ScreenType;
import game.freya.gui.panes.GameWindowJME;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SceneController {
    private final UIHandler uIHandler;
    private final CharacterService characterService;
    private final WorldService worldService;
    private GameControllerService gameControllerService;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void showGameWindow() {
        Constants.setGameWindow(new GameWindowJME());

        // ждём пока кончится показ лого:
        if (Constants.getLogo() != null && Constants.getLogo().getEngine().isAlive()) {
            try {
                log.info("Logo finished await...");
                Constants.getLogo().getEngine().join(3_000);
            } catch (InterruptedException ie) {
                log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
                Constants.getLogo().getEngine().interrupt();
            } finally {
                Constants.getLogo().finalLogo();
            }
        }

        while (!Constants.getGameWindow().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        log.info("Игровое окно запущено в потоке {}", Thread.currentThread().getName());
        loadScene(ScreenType.MENU_SCREEN);
    }

    public void loadScene(ScreenType screenType) {
        log.info("Try to load screen {}...", screenType);
        switch (screenType) {
            case MENU_SCREEN -> loadMenuScene();
            case GAME_SCREEN -> loadGameScreen();
            default -> log.error("Unknown screen failed to load: {}", screenType);
        }
    }

    private void loadMenuScene() {
        log.info("Try to load Menu screen...");
        Constants.getGameWindow().setScene(buildMenuNode());
    }

    private void loadGameScreen() {
        log.info("Try to load World '{}' screen...", worldService.getCurrentWorld().getName());
        Constants.getGameWindow().setScene(buildGameNode());
    }

    // menu node:
    private Node buildMenuNode() {
        final Node menuNode = new Node("menuNode");

        setupMenuCamera(menuNode);

        // content:
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
        Box b = new Box(Vector3f.ZERO, new Vector3f(1, 1, 1));
        Spatial wall = new Geometry("Box", b);
        Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_brick.setTexture("ColorMap", assetManager.loadTexture("images/logo.png"));
        //  mat_brick.setColor("Color", ColorRGBA.Blue);
        wall.setMaterial(mat_brick);
        wall.setLocalTranslation(2.0f,-2.5f,0.0f);
        menuNode.attachChild(wall);

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setName("ambiLight");
        ambientLight.setEnabled(true);
        ambientLight.setColor(ColorRGBA.fromRGBA255(127, 117, 120, 255));
        ambientLight.setFrustumCheckNeeded(true);
        ambientLight.setIntersectsFrustum(true);
        menuNode.addLight(ambientLight);

        setupMenuText(menuNode);

        return menuNode;
    }

    private void setupMenuCamera(Node menuNode) {
        FlyByCamera flyCam = Constants.getGameWindow().getFlyByCamera();
        if (flyCam == null) {
            throw new NullPointerException();
        }

        flyCam.setMoveSpeed(0); // default 1.0f
        flyCam.setZoomSpeed(0);
    }

    private void setupMenuText(Node menuNode) {
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();

        /* Display a line of text (default font from jme3-testdata) */
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Console.fnt"); // Default.fnt
        BitmapText helloText = new BitmapText(guiFont);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Menu mode active");
        helloText.setLocalTranslation(300, helloText.getLineHeight() * 2, 0);
//        Constants.getGameWindow().getGuiNode().detachAllChildren();
        Constants.getGameWindow().getGuiNode().attachChild(helloText);
    }


    // game node:
    private Node buildGameNode() {
        final Node gameNode = new Node("gameNode");

        setupGameCamera(gameNode);

        // content:
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
        Spatial landWithHouse = assetManager.loadModel("misc/wildhouse/main.mesh.xml");
        gameNode.attachChild(landWithHouse);

        Spatial ship = assetManager.loadModel("misc/ship/ship.obj");
        Material mat_default = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_default.setTexture("ColorMap", assetManager.loadTexture("misc/ship/ship.png"));
        //  mat_default.setColor("Color", ColorRGBA.Blue);
        ship.setMaterial(mat_default);
        gameNode.attachChild(ship);

        /* Load a Ninja model (OgreXML + material + texture from test_data) */
//        Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
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

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setName("ambiLight");
        ambientLight.setEnabled(true);
        ambientLight.setColor(ColorRGBA.fromRGBA255(127, 117, 120, 255));
        ambientLight.setFrustumCheckNeeded(true);
        ambientLight.setIntersectsFrustum(true);
        gameNode.addLight(ambientLight);

        /* You must add a light to make the model visible. */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        gameNode.addLight(sun);

        setupGameText(gameNode);

        return gameNode;
    }

    private void setupGameCamera(Node gameNode) {
        AppSettings settings = Constants.getJmeSettings();
        float aspect = (float) settings.getWindowWidth() / settings.getWindowHeight();
        Camera cam = Constants.getGameWindow().getCamera();
        FlyByCamera flyCam = Constants.getGameWindow().getFlyByCamera();

//        cam.setFov(Constants.getUserConfig().getFov());
//        cam.setFrustumNear(1.f); // default 1.0f
//        cam.setFrustumFar(1000); // default 1000
//        cam.setFrustum(0.25f, 1_000.f, -0.19f, 0.19f, 0.11f, -0.11f);
        cam.setFrustumPerspective(Constants.getUserConfig().getFov(), aspect, 0.25f, 1_000.f);

        flyCam.setMoveSpeed(gameControllerService.getCharacterService().getCurrentHero().getSpeed());
        flyCam.setZoomSpeed(Constants.getGameConfig().getZoomSpeed());
    }

    private void setupGameText(Node gameNode) {
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();

        /* Display a line of text (default font from jme3-testdata) */
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Console.fnt"); // Default.fnt
        BitmapText helloText = new BitmapText(guiFont);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Game mode active");
        helloText.setLocalTranslation(300, helloText.getLineHeight() * 2, 0);
//        Constants.getGameWindow().getGuiNode().detachAllChildren();
        Constants.getGameWindow().getGuiNode().attachChild(helloText);
    }
}
