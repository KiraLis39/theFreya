package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsView;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Limits;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.enums.gui.UiDebugLevel;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Setter
public class DebugInfoState extends AbstractAppState {
    private SimpleApplication app;
//    private InputManager inputManager;
//    private final GameControllerService gameControllerService;

    protected Node guiNode;
    protected Renderer renderer;
    protected BitmapFont guiFont;
    protected JmeContext context;
    protected AppSettings settings;
    protected Camera cam;
    protected FlyByCamera flyCam;
    protected AssetManager assetManager;

    private short lineCount;
    private Node infoRootNode, fullView;
    private Node currentModeNode;

    private boolean showFps;
    private boolean showBase;
    private boolean showFull;

    protected BitmapText fpsText;
    protected StatsView statsView;
    protected Geometry darkenFps;
    protected Geometry darkenBase;
    protected Geometry darkenFull;

    protected float secondCounter = 0.0f;
    protected int frameCounter = 0;

    @Getter
    private UiDebugLevel currentDebugLevel;

    public DebugInfoState(Node currentModeNode, GameControllerService gameControllerService) {
        super("DebugInfoState");
        this.currentModeNode = currentModeNode;
//        this.gameControllerService = gameControllerService;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        currentDebugLevel = UiDebugLevel.FPS_ONLY;
        showFps = currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal();
        showBase = currentDebugLevel.ordinal() >= UiDebugLevel.BASE.ordinal();
        showFull = currentDebugLevel.ordinal() >= UiDebugLevel.FULL.ordinal();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        this.flyCam = this.app.getFlyByCamera();
        this.context = this.app.getContext();
        this.settings = context.getSettings();
        this.renderer = this.app.getRenderer();
//        this.inputManager = this.app.getInputManager();
        this.assetManager = this.app.getAssetManager();
        this.guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt"); // Default.fnt | Console.fnt
        this.fpsText = new BitmapText(guiFont);
        this.infoRootNode = new Node(NodeNames.GAME_INFO_NODE.name());
        this.infoRootNode.setCullHint(currentDebugLevel.equals(UiDebugLevel.NONE) ? Spatial.CullHint.Always : Spatial.CullHint.Never);
        this.fullView = new Node("fullView");
        this.fullView.setCullHint(currentDebugLevel.equals(UiDebugLevel.FULL) ? Spatial.CullHint.Never : Spatial.CullHint.Always);

        setup(currentModeNode);

        guiNode.attachChild(infoRootNode);

        super.initialize(stateManager, app);
    }

    private void setup(Node currentModeNode) {
        // fps:
        fpsText.setLocalTranslation(1, fpsText.getLineHeight() + 2, -1);
        fpsText.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(fpsText);

        // base gl info:
        statsView = new StatsView("Statistic", app.getAssetManager(), app.getRenderer().getStatistics());
        statsView.setLocalTranslation(2, fpsText.getLineHeight() * 2, 0);
        statsView.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.BASE.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        statsView.setEnabled(currentDebugLevel.ordinal() >= UiDebugLevel.BASE.ordinal());
        infoRootNode.attachChild(statsView);

        // информация о самой игре и текущих настройках:
        lineCount = 1;
        infoRootNode.attachChild(new BitmapText(guiFont) {
            {
                setText("Mode: ".concat(currentModeNode.getName()));
                setSize(guiFont.getCharSet().getRenderedSize());
                setCullHint(Spatial.CullHint.Inherit);
                setLocalTranslation(24, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
            }
        });
        lineCount++;
        attachToNode(List.of("samplesText", "vsyncText", "fpsLimitText", "flscrnText", "mscOnText", "sndOnText", "fcamMoveSpeed",
                "fcamRotSpeed", "fcamZoomSpeed", "camRotText", "camDirText", "camPosText", "winWidth", "winHeight", "anizotropicMax", "worldLights"));
        infoRootNode.attachChild(fullView);

        // затенённые задники:
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.25f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        darkenFps = new Geometry("StatsDarken", new Quad(60, fpsText.getLineHeight()));
        darkenFps.setMaterial(mat);
        darkenFps.setLocalTranslation(1, 1, -0.5f);
        darkenFps.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenFps);

        darkenBase = new Geometry("StatsDarken", new Quad(150, statsView.getHeight() + 4));
        darkenBase.setMaterial(mat);
        darkenBase.setLocalTranslation(0, fpsText.getLineHeight() * 2 - 2, -0.5f);
        darkenBase.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.BASE.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenBase);

        darkenFull = new Geometry("StatsDarken", new Quad(280, fullView.getChildren().size() * fpsText.getLineHeight() * 1.2f));
        darkenFull.setMaterial(mat);
        darkenFull.setLocalTranslation(0, settings.getHeight() - fullView.getChildren().size() * fpsText.getLineHeight() * 1.2f, -0.5f);
        darkenFull.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.FULL.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenFull);
    }

    private void attachToNode(List<String> names) {
        for (String n : names) {
            fullView.attachChild(new BitmapText(guiFont) {
                {
                    setName(n);
                    setSize(guiFont.getCharSet().getRenderedSize());
                    setCullHint(Spatial.CullHint.Inherit);
                    setLocalTranslation(30, settings.getWindowHeight() - getLineHeight() * lineCount, 0);
                }
            });
            lineCount++;
        }
    }

    public void toggleStats() {
        currentDebugLevel = UiDebugLevel
                .values()[currentDebugLevel.ordinal() + 1 >= UiDebugLevel.values().length ? 0 : currentDebugLevel.ordinal() + 1];
        log.info("Current debug level: {}", currentDebugLevel);
    }

    @Override
    public void postRender() {
        // если отображается FPS:
        if (currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal()) {
            secondCounter += app.getTimer().getTimePerFrame();
            frameCounter++;
            if (secondCounter >= 1.0f) {
                fpsText.setText("FPS: %s".formatted((int) (frameCounter / secondCounter)));
                secondCounter = frameCounter = 0;
            }
        }

        switch (currentDebugLevel) {
            case NONE -> {
                if (showFps) {
                    setDisplayFps(false);
                }
                if (showBase) {
                    setBaseStats(false);
                }
                if (showFull) {
                    setFullStats(false);
                }
            }
            case FPS_ONLY -> {
                if (!showFps) {
                    setDisplayFps(true);
                }
            }
            case BASE -> {
                if (!Constants.getGameConfig().isDebugInfoVisible()) {
                    log.warn("Для отображения отладочной информации требуется активировать соответствующий флаг в файле конфигурации игры");
                    return;
                }
                if (!showFps) {
                    setDisplayFps(true);
                }
                if (!showBase) {
                    setBaseStats(true);
                }
            }
            case FULL -> {
                if (!Constants.getGameConfig().isDebugInfoVisible()) {
                    log.warn("Для отображения отладочной информации требуется активировать соответствующий флаг в файле конфигурации игры");
                    return;
                }
                if (!showFps) {
                    setDisplayFps(true);
                }
                if (!showBase) {
                    setBaseStats(true);
                }
                if (!showFull) {
                    setFullStats(true);
                }
                showDebugInfo();
            }
        }
    }

    private void setDisplayFps(boolean show) {
        showFps = show;
        if (fpsText != null) {
            fpsText.setCullHint(showFps ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            if (darkenFps != null) {
                darkenFps.setCullHint(showFps ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            }
        }
    }

    private void setBaseStats(boolean show) {
        showBase = show;
        if (statsView != null) {
            statsView.setEnabled(showBase);
            statsView.setCullHint(showBase ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            if (darkenBase != null) {
                darkenBase.setCullHint(showBase ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            }
        }
    }

    private void setFullStats(boolean show) {
        showFull = show;
        if (fullView != null) {
            fullView.setCullHint(showFull ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            if (darkenFull != null) {
                darkenFull.setCullHint(showFull ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            }
        }
    }

    private void showDebugInfo() {
        ((BitmapText) fullView.getChild("samplesText")).setText("MultiSamplingLevel: %s".formatted(context.getSettings().getSamples()));
        ((BitmapText) fullView.getChild("vsyncText")).setText("VSync: %s".formatted(context.getSettings().isVSync()));
        ((BitmapText) fullView.getChild("fpsLimitText")).setText("FPS limit: %s".formatted(context.getSettings().getFrameRate()));
        ((BitmapText) fullView.getChild("sndOnText")).setText("Sounds: %s".formatted(Constants.getUserConfig().isSoundEnabled()));
        ((BitmapText) fullView.getChild("mscOnText")).setText("Music: %s".formatted(Constants.getUserConfig().isMusicEnabled()));
        ((BitmapText) fullView.getChild("fcamMoveSpeed")).setText("FlyCam move speed: %s".formatted(flyCam.getMoveSpeed()));
        ((BitmapText) fullView.getChild("fcamRotSpeed")).setText("FlyCam rotation speed: %s".formatted(flyCam.getRotationSpeed()));
        ((BitmapText) fullView.getChild("fcamZoomSpeed")).setText("FlyCam zoom speed: %s".formatted(flyCam.getZoomSpeed()));
        ((BitmapText) fullView.getChild("camRotText")).setText("Cam rotation: %s".formatted(cam.getRotation()));
        ((BitmapText) fullView.getChild("camDirText")).setText("Cam direction: %s".formatted(cam.getDirection()));
        ((BitmapText) fullView.getChild("camPosText")).setText("Cam position: %s".formatted(cam.getLocation()));
        ((BitmapText) fullView.getChild("flscrnText")).setText("Fullscreen: %s"
                .formatted(context.getSettings().isFullscreen() && Constants.getUserConfig().isFullscreen() ? "true"
                        : !context.getSettings().isFullscreen() && Constants.getUserConfig().isFullscreen() ? "pseudo" : "false"));
        ((BitmapText) fullView.getChild("winWidth")).setText("Width: %s".formatted(settings.getWidth()));
        ((BitmapText) fullView.getChild("winHeight")).setText("Height: %s".formatted(settings.getHeight()));
        ((BitmapText) fullView.getChild("anizotropicMax"))
                .setText("Anisotropy allowed: %s".formatted(renderer.getLimits().get(Limits.TextureAnisotropy)));
        ((BitmapText) fullView.getChild("worldLights")).setText("Lights: %s / %s"
                .formatted(currentModeNode.getLocalLightList().size(), currentModeNode.getWorldLightList().size()));
    }

    @Override
    public void cleanup() {
//        if (inputManager.hasMapping(INPUT_MAPPING_CAMERA_POS))
//            inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS);
//        if (inputManager.hasMapping(INPUT_MAPPING_MEMORY))
//            inputManager.deleteMapping(INPUT_MAPPING_MEMORY);

        guiNode.detachChild(infoRootNode);

//        guiNode.detachChild(statsView);
//        guiNode.detachChild(fpsText);
//        guiNode.detachChild(darkenFps);
//        guiNode.detachChild(darkenStats);

        super.cleanup();
    }
}
