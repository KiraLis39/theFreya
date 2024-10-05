package game.freya.gui.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import game.freya.config.Controls;
import game.freya.gui.states.substates.menu.meshes.GrayOptionsMesh;
import lombok.Setter;

public class GrayOptionsBack extends Geometry {
    private Material darkenMat;
    private ColorRGBA color;
    @Setter
    private float opacity = 0;

    public GrayOptionsBack(AssetManager assetManager) {
        super("OptionsBackDarken", new GrayOptionsMesh()); // new Quad(width, height);

        color = new ColorRGBA(0, 0, 0, opacity);

        darkenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        darkenMat.setColor("Color", color);
        darkenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        setMaterial(darkenMat);
        setQueueBucket(RenderQueue.Bucket.Transparent);
        setLocalTranslation(0.33f, 0, 0.01f);
        setCullHint(Controls.isOptionsMenuVisible() ? CullHint.Never : CullHint.Always);
        // setLodLevel(0);
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        color.setAlpha(opacity);
        darkenMat.setColor("Color", color);
    }

    public void increaseOpacity() {
        opacity += 0.015f;
        if (opacity > 0.8f) {
            opacity = 0.8f;
        }
        setOpacity(opacity);
    }

    public void decreaseOpacity() {
        opacity -= 0.02f;
        if (opacity < 0) {
            opacity = 0;
        }
        setOpacity(opacity);
    }
}
