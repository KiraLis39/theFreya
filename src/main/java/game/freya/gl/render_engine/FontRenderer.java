package game.freya.gl.render_engine;

import game.freya.gl.font_mesh_creator.FontType;
import game.freya.gl.font_mesh_creator.GUIText;
import game.freya.gl.shaders.FontShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class FontRenderer {

    private final FontShader shader;

    public FontRenderer() {
        shader = new FontShader();
    }

    public void render(Map<FontType, List<GUIText>> texts) {
        prepare();
        for (FontType font : texts.keySet()) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, font.getId());
            for (GUIText text : texts.get(font)) {
                renderText(text);
            }
        }
        endRendering();
    }

    public void close() {
        shader.close();
    }

    private void prepare() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        shader.start();
    }

    private void renderText(GUIText text) {
        GL30.glBindVertexArray(text.getMesh());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        shader.loadColor(text.getColor());
        shader.loadTranslation(text.getPosition());
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    private void endRendering() {
        shader.stop();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
