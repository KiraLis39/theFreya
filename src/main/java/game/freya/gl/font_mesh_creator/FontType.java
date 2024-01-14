package game.freya.gl.font_mesh_creator;

import lombok.Getter;

import java.io.File;

/**
 * Represents a font. It holds the font's texture atlas as well as having the
 * ability to create the quad vertices for any text using this font.
 *
 * @author Karl
 */
public class FontType {
    @Getter
    private final int id;

    private final TextMeshCreator loader;

    /**
     * Creates a new font and loads up the data about each character from the font file.
     *
     * @param textureAtlasId - the ID of the font atlas texture.
     * @param fontFile       - the font file containing information about each character in the texture atlas.
     */
    public FontType(int textureAtlasId, File fontFile, double aspect) {
        this.id = textureAtlasId;
        this.loader = new TextMeshCreator(fontFile, aspect);
    }

    /**
     * Takes in an unloaded text and calculate all of the vertices for the quads
     * on which this text will be rendered. The vertex positions and texture
     * coords and calculated based on the information from the font file.
     *
     * @param text - the unloaded text.
     * @return Information about the vertices of all the quads.
     */
    public TextMeshData loadText(GUIText text) {
        return loader.createTextMesh(text);
    }
}
