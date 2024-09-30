package game.freya.states.substates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsView;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
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
import com.jme3.util.BufferUtils;
import game.freya.config.Constants;
import game.freya.enums.gui.NodeNames;
import game.freya.enums.gui.UiDebugLevel;
import game.freya.states.MainMenuState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.jme3.app.SimpleApplication.INPUT_MAPPING_HIDE_STATS;

@Slf4j
@Setter
public class DebugInfoState extends BaseAppState {
    private SimpleApplication app;

    private Renderer renderer;
    private JmeContext context;
    private AssetManager assetManager;
    private AppSettings settings;
    private FlyByCamera flyCam;
    private BitmapFont guiFont;
    private Camera cam;
    private Node guiNode, infoRootNode, fullView;

    private Material darkenMat;
    private BitmapText fpsText, modTitle;
    private StatsView statsView;
    private Geometry darkenFps, darkenBase, darkenFull;

    private String currentStateName;
    private boolean showFps, showBase, showFull, needUpdate, isDetached;
    private short lineCount;
    private float secondCounter = 0.0f;
    private int frameCounter = 0;

    @Getter
    private UiDebugLevel currentDebugLevel;

    public DebugInfoState() {
        super(DebugInfoState.class.getSimpleName());
    }

    @Override
//    @SuppressWarnings("ConstantConditions")
    public void stateAttached(AppStateManager stateManager) {
        currentDebugLevel = UiDebugLevel.FPS_ONLY;
        showFps = true;
        showBase = false;
        showFull = false;

        if (darkenFull != null) {
            rebuildFullText();
        }
        this.isDetached = false;
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        this.isDetached = true;
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
        this.infoRootNode = new Node(NodeNames.GAME_INFO_NODE.name());
        this.infoRootNode.setCullHint(currentDebugLevel.equals(UiDebugLevel.NONE) ? Spatial.CullHint.Always : Spatial.CullHint.Never);
        this.fullView = new Node("fullView");
        this.fullView.setCullHint(currentDebugLevel.equals(UiDebugLevel.FULL) ? Spatial.CullHint.Never : Spatial.CullHint.Always);

        this.darkenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        this.darkenMat.setColor("Color", new ColorRGBA(0, 0, 0, 0.25f));
        this.darkenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        setup();

        guiNode.attachChild(infoRootNode);
    }

    @Override
    protected void onEnable() {
        BufferUtils.setTrackDirectMemoryEnabled(true);
        app.getInputManager().deleteMapping(INPUT_MAPPING_HIDE_STATS); // дефолтные гор. клавиши отображения инфо.

        if (!isDetached) {
            if (darkenFull != null) {
                rebuildFullText();
            }
        }
    }

    @Override
    protected void onDisable() {
        BufferUtils.setTrackDirectMemoryEnabled(false);
        app.getInputManager().addMapping(INPUT_MAPPING_HIDE_STATS, new KeyTrigger(KeyInput.KEY_F5)); // дефолтные гор. клавиши отображения инфо.
    }

    private void setup() {
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

        // затенённые задники:
        darkenFps = new Geometry("StatsDarken", new Quad(60, fpsText.getLineHeight()));
        darkenFps.setMaterial(darkenMat);
        darkenFps.setLocalTranslation(1, 1, -0.5f);
        darkenFps.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenFps);

        darkenBase = new Geometry("StatsDarken", new Quad(150, statsView.getHeight() + 4));
        darkenBase.setMaterial(darkenMat);
        darkenBase.setLocalTranslation(0, fpsText.getLineHeight() * 2 - 2, -0.5f);
        darkenBase.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.BASE.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenBase);

        // информация о самой игре и текущих настройках:
        rebuildFullText();
    }

    public void rebuildFullText() {
        if (infoRootNode.hasChild(darkenFull)) {
            infoRootNode.detachChild(darkenFull);
        }
        if (infoRootNode.hasChild(fullView)) {
            infoRootNode.detachChild(fullView);
        }
        if (infoRootNode.hasChild(modTitle)) {
            infoRootNode.detachChild(modTitle);
        }

        this.fullView = new Node("fullView");
        this.fullView.setCullHint(currentDebugLevel.equals(UiDebugLevel.FULL) ? Spatial.CullHint.Never : Spatial.CullHint.Always);

        // lineCount = 1:
        modTitle = new BitmapText(guiFont) {
            {
                setName("mode_title");
                setText("Mode: ".concat(currentStateName != null ? currentStateName : "na"));
                setSize(guiFont.getCharSet().getRenderedSize());
                setCullHint(Spatial.CullHint.Inherit);
                setLocalTranslation(24, cam.getHeight() - getLineHeight() - 3, -1);
            }
        };
        infoRootNode.attachChild(modTitle);

        lineCount = 2;
        attachToNode(List.of("vsyncText", "fpsLimitText", "flscrnText", "mscOnText", "sndOnText", "fcamMoveSpeed",
                "fcamRotSpeed", "fcamZoomSpeed", "camRotText", "camDirText", "camPosText", "winWidth", "winHeight", "anizotropic", "anizotropicMax", "worldLights"));
        infoRootNode.attachChild(fullView);

        float darkenFullHeight = lineCount * fpsText.getLineHeight();
        darkenFull = new Geometry("StatsDarken", new Quad(270f, darkenFullHeight * 1.2f));
        darkenFull.setMaterial(darkenMat);
        darkenFull.setLocalTranslation(0f, cam.getHeight() - darkenFullHeight * 1.2f, -0.5f);
        darkenFull.setCullHint(currentDebugLevel.ordinal() >= UiDebugLevel.FULL.ordinal() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        infoRootNode.attachChild(darkenFull);

        if (getStateManager().hasState(getStateManager().getState(MainMenuState.class, false))) {
            getStateManager().getState(MainMenuState.class).setupGui();
        }
    }

    private void attachToNode(List<String> names) {
        for (String n : names) {
            fullView.attachChild(new BitmapText(guiFont) {
                {
                    setName(n);
                    setSize(guiFont.getCharSet().getRenderedSize());
                    setCullHint(Spatial.CullHint.Inherit);
                    setLocalTranslation(30, cam.getHeight() - getLineHeight() * lineCount - 15, -1);
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
        if (isDetached || !isEnabled()) {
            return;
        }

        // если отображается FPS:
        if (currentDebugLevel.ordinal() >= UiDebugLevel.FPS_ONLY.ordinal()) {
            secondCounter += app.getTimer().getTimePerFrame();
            frameCounter++;
            if (secondCounter >= 1.0f) {
                fpsText.setText("FPS: %s".formatted((int) (frameCounter / secondCounter)));
                secondCounter = frameCounter = 0;
                needUpdate = true; // нужно обновить full-блок
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

                if (needUpdate) {
                    // обновляем full-блок...
                    needUpdate = false;
                    showDebugInfo();
                }
            }
            case null, default -> log.error("Не ясен выбор уровня лога: {}", currentDebugLevel);
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
        ((BitmapText) fullView.getChild("anizotropic")).setText("MultiSamplingLevel: %s".formatted(context.getSettings().getSamples()));
        ((BitmapText) fullView.getChild("anizotropicMax"))
                .setText("Anisotropy allowed: %s".formatted(renderer.getLimits().get(Limits.TextureAnisotropy)));

        Spatial lightsNode = app.getRootNode().getChild(NodeNames.MENU_SCENE_NODE.name());
        if (lightsNode == null) {
            lightsNode = app.getRootNode().getChild(NodeNames.GAME_SCENE_NODE.name());
        }
        ((BitmapText) fullView.getChild("worldLights")).setText("Lights: %s / %s"
                .formatted(lightsNode.getLocalLightList().size(), lightsNode.getWorldLightList().size()));
    }

    @Override
    protected void cleanup(Application app) {
        guiNode.detachChild(infoRootNode);
    }

    public void currentStateId(String name) {
        this.currentStateName = name;
        modTitle.setText("Mode: ".concat(currentStateName != null ? currentStateName : "na"));
    }
}
