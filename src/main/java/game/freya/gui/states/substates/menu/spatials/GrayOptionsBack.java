package game.freya.gui.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import game.freya.config.Controls;
import game.freya.gui.states.substates.menu.meshes.GrayOptionsMesh;

public class GrayOptionsBack extends Geometry {
    private Material darkenMat;
    private ColorRGBA color;
    private int opacity = 158;

    public GrayOptionsBack(AssetManager assetManager) {
        super("OptionsBackDarken", new GrayOptionsMesh()); // new Quad(width, height);

        color = ColorRGBA.fromRGBA255(0, 0, 0, opacity);

        darkenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        darkenMat.setColor("Color", color);
        darkenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        setMaterial(darkenMat);
        setQueueBucket(RenderQueue.Bucket.Transparent);
        setLocalTranslation(0.33f, 0, 0.01f);
        setCullHint(Controls.isOptionsMenuVisible() ? CullHint.Never : CullHint.Always);
        // setLodLevel(0);
    }

    public void increaseOpacity() {
        opacity++;
        if (opacity > 223) {
            opacity = 223;
        }
        color.setAlpha(opacity);
        darkenMat.setColor("Color", color);
    }

    public void decreaseOpacity() {
        opacity--;
        if (opacity < 0) {
            opacity = 0;
        }
        color.setAlpha(opacity);
        darkenMat.setColor("Color", color);
    }
}
