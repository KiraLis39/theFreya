package game.freya.gl.font_mesh_creator;

import game.freya.gl.render_engine.FontRenderer;
import game.freya.gl.render_engine.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.glFrontFace;

public class TextMaster {
    private final Map<FontType, List<GUIText>> texts = new HashMap<>();

    private final FontRenderer renderer;

    private final Loader loader;

    public TextMaster(Loader loader) {
        this.renderer = new FontRenderer();
        this.loader = loader;
    }

    public void render() {
        glFrontFace(GL_CCW);
        renderer.render(texts);
    }

    public void loadText(GUIText text) {
        FontType font = text.getFont();
        TextMeshData data = font.loadText(text);
        int vao = loader.loadToVAO(data.getVertexPositions(), data.getTextureCoords());
        text.setMeshInfo(vao, data.getVertexCount());
        List<GUIText> textBatch = texts.getOrDefault(font, null);
        if (textBatch == null) {
            textBatch = new ArrayList<>();
            texts.put(font, textBatch);
        }
        textBatch.add(text);
    }

    public void removeText(GUIText text) {
        List<GUIText> textBatch = texts.get(text.getFont());
        textBatch.remove(text);
        if (textBatch.isEmpty()) {
            texts.remove(text.getFont());
        }
    }

    public void close() {
        renderer.close();
    }
}
