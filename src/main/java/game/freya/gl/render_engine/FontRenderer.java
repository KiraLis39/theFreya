package game.freya.gl.render_engine;

import game.freya.config.Constants;
import game.freya.gl.font_mesh_creator.FontType;
import game.freya.gl.font_mesh_creator.GUIText;
import game.freya.gl.shaders.FontShader;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class FontRenderer {

    private final FontShader shader;

    public FontRenderer() {
        shader = new FontShader();
    }

    public void render(Map<FontType, List<GUIText>> texts) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        shader.start();

        for (FontType font : texts.keySet()) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, font.getId());
            for (GUIText text : texts.get(font)) {
                renderText(text);
            }
        }

        shader.stop();

        glDisable(GL_BLEND);
        if (Constants.getGameConfig().isDepthEnabled()) {
            glEnable(GL_DEPTH_TEST);
        }
    }

    private void renderText(GUIText text) {
        glBindVertexArray(text.getTextMeshVao());

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        shader.loadColor(text.getColor());
        shader.loadTranslation(text.getPosition());
        glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    public void close() {
        shader.close();
    }
}
