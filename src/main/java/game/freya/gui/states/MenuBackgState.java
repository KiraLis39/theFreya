package game.freya.gui.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import game.freya.services.GameControllerService;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public class MenuBackgState extends AbstractAppState {
    private final Collection<String> userData;
    private SimpleApplication app;
    protected JmeContext context;
    protected AssetManager assetManager;
    private Node currentModeNode;
    private AudioNode bkgMusic;

    public MenuBackgState(Node currentModeNode, GameControllerService gameControllerService) {
        super("MenuBackgState");
        this.currentModeNode = currentModeNode;
        this.userData = currentModeNode.getUserDataKeys();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        if (bkgMusic != null) {
            bkgMusic.play();
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        if (bkgMusic != null) {
            bkgMusic.stop();
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app;
        this.context = this.app.getContext();
        this.assetManager = this.app.getAssetManager();

        for (String userDatum : userData) {
            if (userDatum.startsWith("bkg")) {
                createAudioNode(currentModeNode.getUserData(userDatum), false, false, true, 3).play();
                break;
            }
        }

        super.initialize(stateManager, app);
    }

    public void setBackg(@NotNull String localResourceUrl, boolean isPositional, boolean isDirectional, boolean isLooping, float volume) {
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
    public void cleanup() {
        if (bkgMusic != null) {
            bkgMusic.stop();
        }
        super.cleanup();
    }
}
