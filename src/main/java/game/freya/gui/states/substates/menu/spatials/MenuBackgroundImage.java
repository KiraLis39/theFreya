package game.freya.gui.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import game.freya.config.Constants;

public class MenuBackgroundImage extends Geometry {

    public MenuBackgroundImage(AssetManager assetManager, Camera cam) {
        super("MenuBackground", new Quad(2f, (float) (2f / Constants.getCurrentScreenAspect())));
        float menuWidth = 2f, menuHeight = (float) (menuWidth / Constants.getCurrentScreenAspect()); // 1424

        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", assetManager.loadTexture("images/necessary/menu.png"));
        setMaterial(mat_menu);
        setLocalTranslation(-1f, -menuHeight / 2f, 0);
        setCullHint(Spatial.CullHint.Dynamic);
    }
}
