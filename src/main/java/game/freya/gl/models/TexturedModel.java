package game.freya.gl.models;

import game.freya.gl.textures.ModelTexture;
import lombok.Builder;
import lombok.Getter;

@Builder
public record TexturedModel(@Getter RawModel rawModel, @Getter ModelTexture texture) {
}
