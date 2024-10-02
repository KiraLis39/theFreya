package game.freya.gui.states.substates.menu;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Collection;

@Slf4j
public class MenuBackgState extends BaseAppState {
    private final Collection<String> userDataKeys;
    private final Node currentModeNode;
    private final float volumeFlowMod = 0.25f;
    private AssetManager assetManager;
    private AudioNode bkgMusic;
    private SimpleApplication app;
    private float wasLvl;

    public MenuBackgState(Node currentModeNode) {
        super(MenuBackgState.class.getSimpleName());
        this.currentModeNode = currentModeNode;
        this.userDataKeys = currentModeNode.getUserDataKeys();

        // какая-то настройка аудио?
//        float[] eax = new float[]{15, 38.0f, 0.300f, -1000, -3300, 0,
//                1.49f, 0.54f, 1.00f, -2560, 0.162f, 0.00f, 0.00f,
//                0.00f, -229, 0.088f, 0.00f, 0.00f, 0.00f, 0.125f, 1.000f,
//                0.250f, 0.000f, -5.0f, 5000.0f, 250.0f, 0.00f, 0x3f};
//        Environment env = new Environment(eax);
//        audioRenderer.setEnvironment(env);
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.assetManager = this.app.getAssetManager();

        for (String key : userDataKeys) {
            if (key.startsWith("bkg")) {
                createAudioNode(currentModeNode.getUserData(key), false, false, true, 3).play();
                break;
            }
        }
    }

    @Override
    protected void onEnable() {
        if (bkgMusic != null) {
            bkgMusic.play();
        }
    }

    @Override
    protected void onDisable() {
        if (bkgMusic != null) {
            bkgMusic.pause();
        }
    }

    protected void setBackg(@NotNull String localResourceUrl, boolean isPositional, boolean isDirectional, boolean isLooping, float volume) {
        if (bkgMusic != null) {
            bkgMusic.stop();
            bkgMusic.getAudioData().dispose();
        }
        createAudioNode(localResourceUrl, isPositional, isDirectional, isLooping, volume).play();
    }

    private AudioNode createAudioNode(String localResourceUrl, boolean isPositional, boolean isDirectional, boolean isLooping, float volume) {
        bkgMusic = new AudioNode(assetManager, localResourceUrl, AudioData.DataType.Stream);
        bkgMusic.setLooping(isLooping);
        bkgMusic.setPositional(isPositional); // Активирует 3D-аудио: Звук кажется идущим из определенной позиции, где он громче всего
        bkgMusic.setDirectional(isDirectional);
//        bkgMusic.setDryFilter(new LowPassFilter(1f, .1f));
        bkgMusic.setVolume(volume);
        return bkgMusic;
    }

    @Override
    protected void cleanup(Application app) {
        if (bkgMusic != null) {
            bkgMusic.stop();
            bkgMusic.getAudioData().dispose();
        }
    }

    public void pause() {
        if (bkgMusic != null) {
            log.info("Try to pause audio...");
            SwingUtilities.invokeLater(() -> {
                wasLvl = bkgMusic.getVolume();
                while (bkgMusic.getVolume() - volumeFlowMod > 0) {
                    app.enqueue(() -> bkgMusic.setVolume(bkgMusic.getVolume() - volumeFlowMod));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException _) {
                    }
                }
                log.info("Audio paused.");
                app.enqueue(() -> bkgMusic.pause());
            });
        }
    }

    public void resume() {
        if (bkgMusic != null) {
            log.info("Try to resume audio...");
            bkgMusic.play();
            SwingUtilities.invokeLater(() -> {
                while (bkgMusic.getVolume() + volumeFlowMod < wasLvl) {
                    app.enqueue(() -> bkgMusic.setVolume(bkgMusic.getVolume() + volumeFlowMod));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException _) {
                    }
                }
                log.info("Audio resumed...");
            });
        }
    }
}
