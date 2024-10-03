package game.freya.gui.states.substates.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.AnalogListener;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.enums.gui.UiDebugLevel;
import game.freya.gui.states.substates.menu.spatials.GrayOptionsBack;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Отвечает чисто за настройки клавиатуры, экрана, звука, и т.п.
 * Всё то, что будет доступно для настройки и в главном меню и в процессе игры.
 */
@Slf4j
@Setter
public class OptionsState extends BaseAppState {
    private SimpleApplication app;

    private JmeContext context;
    private AppSettings settings;
    private Renderer renderer;
    private AssetManager assetManager;
    private Camera cam;
    private FlyByCamera flyCam;
    private BitmapFont guiFont;
    private Node guiNode, optionsRootNode, gearNode;

    private Material darkenMat;
    private BitmapText fpsText;
    private Geometry darkenOptions, optionsGear;
    private GameControllerService gameControllerService;
    private boolean isGearHovered;
    private float gearRotationsSpeed = 0.01f;
    private AnalogListener anlList;
    @Getter
    private UiDebugLevel currentDebugLevel;

    public OptionsState(Node parentNode, GameControllerService gameControllerService) {
        super(OptionsState.class.getSimpleName());
        this.gameControllerService = gameControllerService;
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        this.flyCam = this.app.getFlyByCamera();
        this.context = this.app.getContext();
        this.settings = context.getSettings();
        this.renderer = this.app.getRenderer();
        this.assetManager = this.app.getAssetManager();
        this.guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt"); // Default.fnt | Console.fnt
        this.fpsText = new BitmapText(guiFont);

        this.optionsRootNode = new Node("OptionsNode");
        this.optionsRootNode.setCullHint(Spatial.CullHint.Inherit);
        this.anlList = new MenuAnalogListener(this.app, this.optionsRootNode);
    }

    @Override
    protected void onEnable() {
        rebuild();
    }

    @Override
    protected void onDisable() {
        if (guiNode.hasChild(optionsRootNode)) {
            optionsRootNode.detachAllChildren();
            guiNode.detachChild(optionsRootNode);
        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    public void rebuild() {
        float darkenOptionsWidth = cam.getWidth() * 0.75f;
        if (guiNode != null && guiNode.hasChild(optionsRootNode)) {
            optionsRootNode.detachAllChildren();
            guiNode.detachChild(optionsRootNode);
        }
        this.optionsRootNode.setLocalTranslation(cam.getWidth() - darkenOptionsWidth, 0, 0);

        // options gear:
        final int gearDim = (int) (32 + cam.getWidth() * 0.01f);
        BufferedImage gearBImage = drawOptionsGear(gearDim);
//        AWTLoader imgLoader = new AWTLoader();
//        Image load = imgLoader.load(avatarBImage, false);

        gearNode = new Node("GearNode") {{
            optionsGear = new Geometry("OptionGear", new Quad(gearDim, gearDim)) {{
                Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
                mat_menu.setTexture("ColorMap", new Texture2D(new AWTLoader().load(gearBImage, false)));
                mat_menu.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                setMaterial(mat_menu);
//                setQueueBucket(RenderQueue.Bucket.Transparent);
                setLocalTranslation(-gearDim / 2f, -gearDim / 2f, 0);
            }};
            attachChild(optionsGear);
            setLocalTranslation(darkenOptionsWidth - gearDim / 2f, gearDim / 2f, 0);
        }};
        optionsRootNode.attachChild(gearNode);

        // затенённые задники:
        darkenOptions = new GrayOptionsBack(darkenOptionsWidth, app.getCamera().getHeight(), app.getAssetManager());
        optionsRootNode.attachChild(darkenOptions);

        guiNode.attachChild(optionsRootNode);
    }

    private BufferedImage drawOptionsGear(int gearDim) {
        BufferedImage result = new BufferedImage(gearDim, gearDim, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2D = result.createGraphics();
        g2D.drawImage(Constants.CACHE.getBufferedImage("optionsGear"), 0, 0, gearDim, gearDim, null);
        g2D.dispose();

        return result;
    }

    @Override
    public void postRender() {
        if (isGearHovered && !Controls.isOptionsMenuVisible()) {
            gearNode.rotate(0, 0, gearRotationsSpeed);
        }
    }

    private Vector3f mouseDestination;

    public void checkPointer(Vector2f mousePointer) {
//        log.info("Mouse moved: {}", mousePointer);
        mouseDestination = new Vector3f(mousePointer.x, mousePointer.y, 0);
//        log.info("\nMouse crossing: {}", crossing);
        float dist = gearNode.getWorldTranslation().distance(mouseDestination);
//        log.info("dist: {}", dist);
        isGearHovered = dist < 20;
    }

    public void checkClick() {
        Controls.setOptionsMenuVisible(isGearHovered);
        log.info("Gear pressed: {}", Controls.isOptionsMenuVisible());
        darkenOptions.setCullHint(Controls.isOptionsMenuVisible() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }
}
