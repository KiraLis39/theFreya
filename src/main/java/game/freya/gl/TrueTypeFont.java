package game.freya.gl;

import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.BufferUtils.createByteBuffer;

@Slf4j
public class TrueTypeFont {
    private static final int BITMAP_W = 1024;

    private static final int BITMAP_H = 1024;

    @Getter
    private final int fontHeight;

    private final STBTTBakedChar.Buffer cdata;

    private final ByteBuffer bitmap;

    private ByteBuffer ttf;

    public TrueTypeFont(int size) {
        this.fontHeight = size;

        int texID = GL11.glGenTextures();
        cdata = STBTTBakedChar.malloc(96);
        bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);

        try (InputStream gRes = getClass().getResourceAsStream("/fonts/Propaganda.ttf")) { // papyrus | Lucida Sans Unicode | Propaganda
            assert gRes != null;

            byte[] wwf = gRes.readAllBytes();
            ttf = createByteBuffer(wwf.length);
            ttf.order(ByteOrder.nativeOrder());
            ttf.put(wwf, 0, wwf.length);
            ttf.flip();

            STBTruetype.stbtt_BakeFontBitmap(ttf, this.fontHeight, bitmap, BITMAP_W, BITMAP_H, 64, cdata);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            STBImageWrite.stbi_write_png("./fonts/font.png", BITMAP_W, BITMAP_H, 1, bitmap, 4);
        } catch (Exception e) {
            log.error("Ошибка при создании шрифта: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void free() {
        cdata.free();
        ttf.clear();
        bitmap.clear();
    }
}
