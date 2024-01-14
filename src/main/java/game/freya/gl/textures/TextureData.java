package game.freya.gl.textures;

import lombok.Getter;

import java.nio.ByteBuffer;

public class TextureData {
    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private final ByteBuffer buffer;

    public TextureData(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }
}
