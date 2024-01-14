package game.freya.gl.textures;

import lombok.Getter;

public class TerrainTexture {
    @Getter
    private final int id;

    public TerrainTexture(int textureID) {
        this.id = textureID;
    }
}
