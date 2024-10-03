package game.freya.gui.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import game.freya.config.Controls;
import game.freya.gui.states.substates.menu.meshes.GrayOptionsMesh;

public class GrayOptionsBack extends Geometry {

    public GrayOptionsBack(float width, float height, AssetManager assetManager) {
        super("OptionsBackDarken", new GrayOptionsMesh(width, height)); // new Quad(width, height);

        Material darkenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        darkenMat.setColor("Color", ColorRGBA.fromRGBA255(0, 0, 0, 223));
        darkenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//        setQueueBucket(RenderQueue.Bucket.Transparent);
        setMaterial(darkenMat);
        setCullHint(Controls.isOptionsMenuVisible() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }
}
