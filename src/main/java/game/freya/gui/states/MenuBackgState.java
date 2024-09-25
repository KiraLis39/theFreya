package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public class MenuBackgState extends BaseAppState {
    private final Collection<String> userData;
    private AssetManager assetManager;
    private final Node currentModeNode;
    private AudioNode bkgMusic;

    public MenuBackgState(Node currentModeNode) {
        super("MenuBackgState");
        this.currentModeNode = currentModeNode;
        this.userData = currentModeNode.getUserDataKeys();
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();

        for (String userDatum : userData) {
            if (userDatum.startsWith("bkg")) {
                createAudioNode(currentModeNode.getUserData(userDatum), false, false, true, 3).play();
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
            bkgMusic.stop();
        }
    }

    protected void setBackg(@NotNull String localResourceUrl, boolean isPositional, boolean isDirectional, boolean isLooping, float volume) {
        if (bkgMusic != null) {
            bkgMusic.stop();
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
        }
        super.cleanup();
    }
}
