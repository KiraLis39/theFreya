package game.freya.gl.font_mesh_creator;

import game.freya.gl.shaders.ShaderProgram;

public class FontShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/fontRendering/fontVertex.txt";

    private static final String FRAGMENT_FILE = "src/fontRendering/fontFragment.txt";

    public FontShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {

    }

    @Override
    protected void bindAttributes() {

    }
}
