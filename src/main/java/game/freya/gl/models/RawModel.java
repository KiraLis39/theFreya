package game.freya.gl.models;

import lombok.Builder;
import lombok.Getter;

@Builder
public record RawModel(@Getter int id, @Getter int vertexCount) {
}
