package game.freya.gui.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import game.freya.gui.states.substates.menu.meshes.GrayCorner;

public class GrayMenuCorner extends Geometry {

    public GrayMenuCorner(AssetManager assetManager) {
        super("GrayMenuPanel", new GrayCorner());

        Material mat_gp = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_gp.setColor("Color", new ColorRGBA(0, 0, 0, 0.75f));
        mat_gp.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat_gp.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        setMaterial(mat_gp);
        setQueueBucket(RenderQueue.Bucket.Transparent);
        setLocalTranslation(0, 0, 0.01f);

        // Вам нужно указать несколько буферов вершин, по одному для каждого желаемого
        // уровня детализации (очень далеко с небольшим количеством деталей, близко со всеми деталями и что-то посередине)
        // setLodLevel(0);
    }
}
