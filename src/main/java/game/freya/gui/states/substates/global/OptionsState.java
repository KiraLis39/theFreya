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
import game.freya.config.ApplicationProperties;
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
    private Vector3f mouseDestination;
    private final String downInfoString1, downInfoString2;
//    private final String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText, repaintButtonText;
//    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
//    private final String backToGameButtonText, saveButtonText, backButtonText, exitButtonText, pausedString;

    public OptionsState(Node parentNode, GameControllerService gameControllerService, ApplicationProperties props) {
        super(OptionsState.class.getSimpleName());
        this.gameControllerService = gameControllerService;

//        this.audioSettingsButtonText = "Настройки звука";
//        this.videoSettingsButtonText = "Настройки графики";
//        this.hotkeysSettingsButtonText = "Управление";
//        this.gameplaySettingsButtonText = "Геймплей";
//        this.backToGameButtonText = "Вернуться";
//        this.saveButtonText = "Сохранить";
//        this.backButtonText = "← Назад";
//        this.exitButtonText = "← Выход";

//        this.startGameButtonText = "Начать игру";
//        this.coopPlayButtonText = "Игра по сети";
//        this.createNewButtonText = "Создать"; // героя или карту
//        this.optionsButtonText = "Настройки";
//        this.repaintButtonText = "Обновить"; // текущий контент (список героев, карт и т.п.)
//        this.randomButtonText = "Случайно"; // генерация героя или карты
//        this.resetButtonText = "Сброс"; // сброс генератора по умолчанию

//        this.pausedString = "- PAUSED -";
        this.downInfoString1 = props.getAppCompany();
        this.downInfoString2 = props.getAppName().concat(" v.").concat(props.getAppVersion());
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

        preparePanes();
    }

    private void preparePanes() {
//        setAudiosPane(new AudioSettingsPane(this));
//        setVideosPane(new VideoSettingsPane(this));
//        setHotkeysPane(new HotkeysSettingsPane(this));
//        setGameplayPane(new GameplaySettingsPane(this));
//        setWorldCreatingPane(new WorldCreatingPane(this, gameControllerService));
//        setHeroCreatingPane(new HeroCreatingPane(this, gameControllerService));
//        setWorldsListPane(new WorldsListPane(this, gameControllerService));
//        setHeroesListPane(new HeroesListPane(this, gameControllerService));
//        setNetworkListPane(new NetworkListPane(this, gameControllerService));
//        setNetworkCreatingPane(new NetCreatingPane(this, gameControllerService));
//
//        // добавляем панели на слой:
//        parentFrame.getContentPane().add(getAudiosPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getVideosPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getHotkeysPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getGameplayPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getHeroCreatingPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getWorldCreatingPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getWorldsListPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getHeroesListPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getNetworkListPane(), PALETTE_LAYER, 0);
//        parentFrame.getContentPane().add(getNetworkCreatingPane(), PALETTE_LAYER, 0);
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

    public void checkPointer(Vector2f mousePointer) {
//        log.info("Mouse moved: {}", mousePointer);
        mouseDestination = new Vector3f(mousePointer.x, mousePointer.y, 0);
//        log.info("\nMouse crossing: {}", crossing);
        float dist = gearNode.getWorldTranslation().distance(mouseDestination);
//        log.info("dist: {}", dist);
        isGearHovered = dist < 20;
    }

    public void checkClick() {
        log.info("Gear pressed: {}", isGearHovered);
        Controls.setOptionsMenuVisible(isGearHovered);
        darkenOptions.setCullHint(Controls.isOptionsMenuVisible() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }
}
