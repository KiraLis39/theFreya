package game.freya.gui.states.substates.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.enums.gui.UiDebugLevel;
import game.freya.gui.states.substates.menu.spatials.GrayMenuCorner;
import game.freya.gui.states.substates.menu.spatials.GrayOptionsBack;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
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
    private AssetManager assetManager;
    private Camera cam;
    private BitmapFont guiFont;
    private Node rootNode, guiNode, gearNode;
    private GrayOptionsBack darkenOptions;
    private Geometry optionsGear;
    private GrayMenuCorner gmc;
    private boolean isGearHovered;
    private float gearRotationsSpeed = 0.02f;
    @Getter
    private UiDebugLevel currentDebugLevel;
    private Vector3f mouseDestination;
    private final String downInfoString1, downInfoString2;
    private final String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText, repaintButtonText;
    private final String audioSettingsButtonText, videoSettingsButtonText, hotkeysSettingsButtonText, gameplaySettingsButtonText;
    private final String backToGameButtonText, saveButtonText, backButtonText, exitButtonText, pausedString;

    public OptionsState(GameControllerService gameControllerService, ApplicationProperties props) {
        super(OptionsState.class.getSimpleName());

        this.audioSettingsButtonText = "Настройки звука";
        this.videoSettingsButtonText = "Настройки графики";
        this.hotkeysSettingsButtonText = "Управление";
        this.gameplaySettingsButtonText = "Геймплей";
        this.backToGameButtonText = "Вернуться";
        this.saveButtonText = "Сохранить";
        this.backButtonText = "← Назад";
        this.exitButtonText = "← Выход";

        this.startGameButtonText = "Начать игру";
        this.coopPlayButtonText = "Игра по сети";
        this.createNewButtonText = "Создать"; // героя или карту
        this.optionsButtonText = "Настройки";
        this.repaintButtonText = "Обновить"; // текущий контент (список героев, карт и т.п.)
        this.randomButtonText = "Случайно"; // генерация героя или карты
        this.resetButtonText = "Сброс"; // сброс генератора по умолчанию

        this.pausedString = "- PAUSED -";
        this.downInfoString1 = props.getAppCompany();
        this.downInfoString2 = props.getAppName().concat(" v.").concat(props.getAppVersion());
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.assetManager = this.app.getAssetManager();
        this.guiNode = this.app.getGuiNode();
        this.guiFont = Constants.getFontDefault();
        this.rootNode = this.app.getRootNode();
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        rebuild();
    }

    @Override
    protected void onDisable() {
        if (rootNode.hasChild(darkenOptions)) {
            rootNode.detachChild(darkenOptions);
        }
        if (rootNode.hasChild(gmc)) {
            rootNode.detachChild(gmc);
        }
        if (guiNode.hasChild(gearNode)) {
            guiNode.detachChild(gearNode);
        }
    }

    public void rebuild() {
        if (rootNode.hasChild(darkenOptions)) {
            rootNode.detachChild(darkenOptions);
        }
        if (rootNode.hasChild(gmc)) {
            rootNode.detachChild(gmc);
        }
        if (guiNode.hasChild(gearNode)) {
            guiNode.detachChild(gearNode);
        }

        // attach left gray corner:
        gmc = new GrayMenuCorner(assetManager);
        rootNode.attachChild(gmc);

        // затенённый задник:
        darkenOptions = new GrayOptionsBack(app.getAssetManager());
        rootNode.attachChild(darkenOptions);

        // шестерёнка:
        final int gearImageDim = (int) (32 + cam.getWidth() * 0.01f);
        BufferedImage gearBImage = drawOptionsGear(gearImageDim);
        gearNode = new Node() {{
            optionsGear = new Geometry("OptionGear", new Quad(gearImageDim, gearImageDim)) {{
                Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
                mat_menu.setTexture("ColorMap", new Texture2D(new AWTLoader().load(gearBImage, false)));
                mat_menu.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

                setMaterial(mat_menu);
                setLocalTranslation(-gearImageDim / 2f, -gearImageDim / 2f, 0);
            }};

            attachChild(optionsGear);
            setLocalTranslation(cam.getWidth() - gearImageDim / 2f, gearImageDim / 2f, 0);
        }};

        guiNode.attachChild(gearNode);
    }

    private BufferedImage drawOptionsGear(int gearDim) {
        // Image load = new AWTLoader().load(avatarBImage, false);
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
        mouseDestination = new Vector3f(mousePointer.x, mousePointer.y, 0);
        float dist = gearNode.getWorldTranslation().distance(mouseDestination);
//        log.info("Mouse moved: {}. Dist: {}", mousePointer, dist);
        isGearHovered = dist < 20;
    }

    public void checkClick() {
        // if the gear is pressed:
        if (isGearHovered) {
            if (!Controls.isOptionsMenuVisible()) {
                showOptionsMenu();
            }
            return;
        }

        // other place clicks:
        if (Controls.isOptionsMenuVisible()) {
            hideOptionsMenu();
        }
    }

    private void hideOptionsMenu() {
        getState(NiftyTestState.class).setEnabled(false);

        SwingUtilities.invokeLater(() -> {
            while (darkenOptions.getLocalTranslation().x < 1f) {
                try {
                    app.enqueue(() -> {
                        darkenOptions.move(+0.05f, 0, 0);
                    });
                    darkenOptions.decreaseOpacity();
                    Thread.sleep(3);
                } catch (Exception _) {
                }
            }
            darkenOptions.setCullHint(Spatial.CullHint.Always);
            Controls.setOptionsMenuVisible(false);
        });
    }

    private void showOptionsMenu() {
        darkenOptions.setLocalTranslation(1, 0, 0);
        darkenOptions.setOpacity(0);
        darkenOptions.setCullHint(Spatial.CullHint.Never);
        Controls.setOptionsMenuVisible(true);
        SwingUtilities.invokeLater(() -> {
            while (darkenOptions.getLocalTranslation().x > 0.37f) {
                try {
                    app.enqueue(() -> {
                        darkenOptions.move(-0.02f, 0, 0);
                    });
                    darkenOptions.increaseOpacity();
                    Thread.sleep(5);
                } catch (Exception _) {
                }
            }
            darkenOptions.setOpacity(0.75f);
            darkenOptions.setLocalTranslation(0.327f, 0, 0.01f);
            getState(NiftyTestState.class).setEnabled(true);
        });
    }
}
