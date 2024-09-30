package game.freya.states.substates.menu.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import game.freya.config.Constants;

public class MenuBackgroundImage extends Geometry {

    public MenuBackgroundImage(AssetManager assetManager, Camera cam) {
        super("MenuBackground", new Quad(cam.getWidth() / 712f, (float) ((cam.getWidth() / 712f) / Constants.getCurrentScreenAspect())));
        float menuWidth = cam.getWidth() / 712f, menuHeight = (float) (menuWidth / Constants.getCurrentScreenAspect());

        Material mat_menu = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // ShowNormals.j3md
        mat_menu.setTexture("ColorMap", assetManager.loadTexture("images/necessary/menu.png"));
        setMaterial(mat_menu);
        setLocalTranslation(-menuWidth / 2f, -menuHeight / 2f, 0);
        setCullHint(Spatial.CullHint.Dynamic);
    }
}
