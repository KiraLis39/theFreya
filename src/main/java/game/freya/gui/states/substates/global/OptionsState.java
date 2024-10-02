package game.freya.gui.states.substates.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
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
import game.freya.enums.gui.UiDebugLevel;
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
    private Node guiNode, optionsRootNode, optionsGearNode;

    private Material darkenMat;
    private BitmapText fpsText;
    private Geometry darkenOptions, optionsGear;
    private GameControllerService gameControllerService;
    @Setter
    private boolean isGearHovered;
    private float gearRotationsSpeed = 0.01f;

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
    }

    @Override
    protected void onEnable() {
        rebuild();
    }

    @Override
    protected void onDisable() {
        if (guiNode.hasChild(optionsRootNode)) {
            guiNode.detachChild(optionsRootNode);
        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    public void rebuild() {
        if (optionsRootNode != null) {
            if (optionsRootNode.hasChild(darkenOptions)) {
                optionsRootNode.detachChild(darkenOptions);
            }
            if (optionsRootNode.hasChild(optionsGear)) {
                optionsRootNode.detachChild(optionsGear);
            }
            if (guiNode != null && guiNode.hasChild(optionsRootNode)) {
                guiNode.detachChild(optionsRootNode);
            }
        }

        this.optionsRootNode = new Node("OptionsNode");
        this.optionsRootNode.setCullHint(Spatial.CullHint.Inherit);

        // затенённые задники:
        darkenOptions = new Geometry("OptionsBackDarken", new Quad(cam.getWidth() / 2f, cam.getHeight()));
        darkenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        darkenMat.setColor("Color", new ColorRGBA(0, 0, 0, 0.5f));
        darkenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        darkenOptions.setMaterial(darkenMat);
//        darkenOptions.setLocalTranslation(1, 1, -0.5f);

        // options gear:
        final int gearDim = (int) (32 + cam.getWidth() * 0.01f);
        BufferedImage gearBImage = drawOptionsGear(gearDim);
//        AWTLoader imgLoader = new AWTLoader();
//        Image load = imgLoader.load(avatarBImage, false);
        optionsGearNode = new Node("optionsGearNode");
        optionsGear = new Geometry("OptionGear", new Quad(gearDim, gearDim));
        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", new Texture2D(new AWTLoader().load(gearBImage, false)));
        mat_menu.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        optionsGear.setMaterial(mat_menu);
//        optionsGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
//        optionsGearNode.setLocalScale();
        optionsGearNode.setLocalTranslation(cam.getWidth() / 2f - gearDim / 2f, gearDim / 2f, 0);
        optionsGearNode.attachChild(optionsGear);
        optionsGear.setLocalTranslation(-gearDim / 2f, -gearDim / 2f, 0);

        optionsRootNode.attachChild(darkenOptions);
        optionsRootNode.attachChild(optionsGearNode);
        optionsRootNode.setLocalTranslation(cam.getWidth() / 2f, 0, 0);

        guiNode.attachChild(optionsRootNode); // todo: err
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
        if (isGearHovered) {
            optionsGearNode.rotate(0, 0, gearRotationsSpeed);
            isGearHovered = false;
        }
    }
}
