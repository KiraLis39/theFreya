package game.freya.gui.states.substates.menu.meshes;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class GrayOptionsMesh extends Mesh {
    private final Vector2f[] vertices = new Vector2f[4];
    private final Vector2f[] texCoord = new Vector2f[4];

    public GrayOptionsMesh(float width, float height) {
        vertices[0] = new Vector2f(-(width * 1.33f) * 0.1f, -height);
        // texCoord[0] = new Vector2f(0,0);

        vertices[1] = new Vector2f(width * 2, -height);
        // texCoord[1] = new Vector2f(0.5f,0);

        vertices[2] = new Vector2f(0, height);
        // texCoord[2] = new Vector2f(0,0.5f);

        vertices[3] = new Vector2f(width * 2, height);
        // texCoord[3] = new Vector2f(0.5f,0.5f);

        int[] indexes = {2, 0, 1, 1, 3, 2};

        setBuffer(VertexBuffer.Type.Position, 2, BufferUtils.createFloatBuffer(vertices));
        // setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));

        updateBound();
        setStatic();
    }
}
