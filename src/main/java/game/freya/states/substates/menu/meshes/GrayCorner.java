package game.freya.states.substates.menu.meshes;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class GrayCorner extends Mesh {

    public GrayCorner() {
        Vector2f[] vertices = new Vector2f[4];
        Vector2f[] texCoord = new Vector2f[4];

        vertices[0] = new Vector2f(-1, -0.57f);
        // texCoord[0] = new Vector2f(0,0);

        vertices[1] = new Vector2f(-0.59f, -0.57f);
        // texCoord[1] = new Vector2f(0.5f,0);

        vertices[2] = new Vector2f(-1, 0.57f);
        // texCoord[2] = new Vector2f(0,0.5f);

        vertices[3] = new Vector2f(-0.5f, 0.57f);
        // texCoord[3] = new Vector2f(0.5f,0.5f);

        int[] indexes = {2, 0, 1, 1, 3, 2};
        // Этот синтаксис означает:
        //  Индексы 0,1,2,3 обозначают четыре вершины, которые вы указали для четырехугольника в vertices[].
        //  Треугольник 2,0,1 начинается слева вверху, продолжается слева внизу и заканчивается справа внизу.
        //  Треугольник 1,3,2 начинается внизу справа, продолжается вверху справа и заканчивается вверху слева.

        setBuffer(VertexBuffer.Type.Position, 2, BufferUtils.createFloatBuffer(vertices));
        // setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));
        updateBound();
    }
}
