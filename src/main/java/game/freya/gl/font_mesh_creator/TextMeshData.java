package game.freya.gl.font_mesh_creator;

import lombok.Getter;

import java.util.Arrays;

/**
 * Stores the vertex data for all the quads on which a text will be rendered.
 *
 * @author Karl
 */
public record TextMeshData(@Getter float[] vertexPositions, @Getter float[] textureCoords) {
    public int getVertexCount() {
        return vertexPositions.length / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextMeshData that = (TextMeshData) o;
        return Arrays.equals(getVertexPositions(), that.getVertexPositions()) && Arrays.equals(getTextureCoords(), that.getTextureCoords());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getVertexPositions());
        result = 31 * result + Arrays.hashCode(getTextureCoords());
        return result;
    }

    @Override
    public String toString() {
        return "TextMeshData: vertexPositions.length: " + vertexPositions.length + ", textureCoords.length: " + textureCoords.length;
    }
}
