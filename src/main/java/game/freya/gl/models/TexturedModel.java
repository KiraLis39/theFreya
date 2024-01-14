package game.freya.gl.models;

import game.freya.gl.textures.ModelTexture;

public class TexturedModel {

    private final RawModel rawModel;

    private final ModelTexture texture;


    public TexturedModel(RawModel model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }

}
