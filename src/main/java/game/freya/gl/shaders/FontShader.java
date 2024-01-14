package game.freya.gl.shaders;

import game.freya.config.Constants;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class FontShader extends ShaderProgram {
    private int locationColor;

    private int locationTranslation;

    public FontShader() {
        super(Constants.FONT_VERTEX_FILE, Constants.FONT_FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        locationColor = super.getUniformLocation("color");
        locationTranslation = super.getUniformLocation("translation");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
    }

    public void loadColor(Vector3f color) {
        super.loadVector(locationColor, color);
    }

    public void loadTranslation(Vector2f translation) {
        super.load2DVector(locationTranslation, translation);
    }
}
