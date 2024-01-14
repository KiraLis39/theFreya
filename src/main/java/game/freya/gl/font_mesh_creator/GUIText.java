package game.freya.gl.font_mesh_creator;

import lombok.Getter;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.Color;

/**
 * Represents a piece of text in the game.
 *
 * @author Karl
 */
public class GUIText {
    private static TextMaster master;

    @Getter
    private final Vector3f color = new Vector3f(0f, 0f, 0f);

    @Getter
    private final String text;

    @Getter
    private final float size;

    @Getter
    private final Vector2f position;

    @Getter
    private final float lineLength;

    @Getter
    private final FontType font;

    @Getter
    private int textMeshVao;

    @Getter
    private int vertexCount;

    @Getter
    private int numberOfLines;

    @Getter
    private boolean isCentered;

    /**
     * Creates a new text, loads the text's quads into a VAO, and adds the text to the screen.
     *
     * @param text       - the text.
     * @param size       - the font size of the text, where a font size of 1 is the default size.
     * @param font       - the font that this text should use.
     * @param position   - the position on the screen where the top left corner of the text should be rendered.
     *                   The top left corner of the screen is (0, 0) and the bottom right is (1, 1).
     * @param lineLength - basically the width of the virtual page in terms of screen width
     *                   (1 is full screen width, 0.5 is half the width of the screen, etc.)
     *                   Text cannot go off the edge of the page, so if the text is longer than this length it will go onto the next
     *                   line. When text is centered it is centered into the middle of the line, based on this line length value.
     * @param isCentered - whether the text should be centered or not.
     */
    public GUIText(TextMaster _master, String text, float size, FontType font, Vector2f position, float lineLength, boolean isCentered) {
        this.text = text;
        this.size = size;
        this.font = font;
        this.position = position;
        this.lineLength = lineLength;
        this.isCentered = isCentered;

        if (master == null) {
            master = _master;
        }
        master.loadText(this);
    }

    /**
     * Remove the text from the screen.
     */
    public void remove() {
        master.removeText(this);
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

    public void setColor(Color c) {
        color.set(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
    }

    /**
     * Sets the number of lines.
     *
     * @param number - number of lines that this text covers (method used only in loading).
     */
    protected void setNumberOfLines(int number) {
        this.numberOfLines = number;
    }

    /**
     * Set the VAO and vertex count for this text.
     *
     * @param vao           - the VAO containing all the vertex data for the quads on which the text will be rendered.
     * @param verticesCount - the total number of vertices in all of the quads.
     */
    public void setMeshInfo(int vao, int verticesCount) {
        this.textMeshVao = vao;
        this.vertexCount = verticesCount;
    }
}
