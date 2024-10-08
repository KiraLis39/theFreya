package game.freya.gui.states.substates.menu.meshes;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class GrayCorner extends Mesh {
    private final Vector3f[] vertices = new Vector3f[4];
    private final Vector2f[] texCoord = new Vector2f[4];

    public GrayCorner() {
        float height = 0.57f;

        vertices[0] = new Vector3f(-1, -height, 0);
        // texCoord[0] = new Vector2f(0,0);

        vertices[1] = new Vector3f(-0.6f, -height, 0);
        // texCoord[1] = new Vector2f(0.5f,0);

        vertices[2] = new Vector3f(-1, height, 0);
        // texCoord[2] = new Vector2f(0,0.5f);

        vertices[3] = new Vector3f(-0.5f, height, 0);
        // texCoord[3] = new Vector2f(0.5f,0.5f);

        int[] indexes = {2, 0, 1, 1, 3, 2};

        setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        // setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));

        setMode(Mode.Triangles);
        updateBound();
        setStatic();
    }
}
