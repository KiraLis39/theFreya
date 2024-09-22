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
import game.freya.dto.roots.WorldDto;
import game.freya.enums.NodeNames;
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
    private short lineCount;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void showGameWindow(AppSettings settings) {
        Constants.setGameWindow(new GameWindowJME(gameControllerService, settings));

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

        // ждём пока JME-окно не прогрузится:
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
        WorldDto w = worldService.getCurrentWorld();
        log.info("Try to load screen '".concat(screenType.toString()).concat(w != null ? "' with World " + w.getName() : "'").concat("..."));
        Constants.getGameWindow().setScene(switch (screenType) {
            case MENU_SCREEN -> buildMenuNode();
            case GAME_SCREEN -> buildGameNode();
        });
    }

    // menu node:
    private Node buildMenuNode() {
        final Node menuNode = new Node(NodeNames.MENU_SCENE.name());

        setupMenuCamera(menuNode);

        // content:
        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
        Box b = new Box(Vector3f.ZERO, new Vector3f(1, 1, 1));
        Spatial menu = new Geometry("Box", b);
        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", assetManager.loadTexture("images/necessary/menu.png"));
        //  mat_brick.setColor("Color", ColorRGBA.Blue);
        menu.setMaterial(mat_menu);
        menu.setLocalTranslation(-1.5f, -1.0f, 0.0f);
        menu.setLocalScale(3.25f, 2, 0.1f);
//        menu.setLodLevel(0);
        menuNode.attachChild(menu);

        setupMenuLights(menuNode);

        setupText(menuNode);

        return menuNode;
    }

    private void setupMenuCamera(Node menuNode) {
        // для меню отключено
        Constants.getGameWindow().getFlyByCamera().setEnabled(false);

        Camera cam = Constants.getGameWindow().getCamera();
        cam.setLocation(new Vector3f(0.f, 0.f, 2.5f));
        cam.lookAt(new Vector3f(0.f, 0.f, -1.f), Vector3f.UNIT_Y);
    }

    private void setupMenuLights(Node menuNode) {
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setName("ambiLight");
        ambientLight.setEnabled(true);
        ambientLight.setColor(ColorRGBA.fromRGBA255(127, 127, 127, 255));
        ambientLight.setFrustumCheckNeeded(false);
        ambientLight.setIntersectsFrustum(false);
        menuNode.addLight(ambientLight);
    }


    // game node:
    private Node buildGameNode() {
        final Node gameNode = new Node(NodeNames.GAME_SCENE.name());

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

        setupText(gameNode);

        return gameNode;
    }

    private void setupGameCamera(Node gameNode) {
        AppSettings settings = Constants.getGameWindow().getContext().getSettings();
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

    private void setupGameLights(Node gameNode) {
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
    }

    // gui text:
    public void setupText(Node node) {
        log.info("UI texts reload...");

        AssetManager assetManager = Constants.getGameWindow().getAssetManager();
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt"); // Default.fnt | Console.fnt
        Node uiTextNode = new Node(NodeNames.UI_TEXT_NODE.name());
        AppSettings settings = Constants.getGameWindow().getContext().getSettings();

        lineCount = 1;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("modeText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setText("Mode: %s".formatted(node.getName()));
                setLocalTranslation(24, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("samplesText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("vsyncText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("fpsLimitText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("flscrnText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("mscOnText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("sndOnText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("fcamMoveSpeed");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("fcamRotSpeed");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("fcamZoomSpeed");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("camRotText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("camDirText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("camPosText");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("winWidth");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("winHeight");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("wwinWidth");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        uiTextNode.attachChild(new BitmapText(guiFont) {
            {
                setName("wwinHeight");
                setSize(guiFont.getCharSet().getRenderedSize());
                setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;

        Constants.getGameWindow().getGuiNode().attachChild(uiTextNode);
    }
}
