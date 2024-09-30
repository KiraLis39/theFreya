package game.freya.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import game.freya.states.substates.menu.meshes.GrayCorner;

public class GrayMenuCorner extends Geometry {

    public GrayMenuCorner(AssetManager assetManager) {
        super("GrayMenuPanel", new GrayCorner());

        Material mat_gp = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_gp.setColor("Color", ColorRGBA.fromRGBA255(0, 0, 0, 223));
        mat_gp.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        setQueueBucket(RenderQueue.Bucket.Transparent);
        setMaterial(mat_gp);
        setCullHint(Spatial.CullHint.Dynamic);
        // setLodLevel(0);
    }
}
