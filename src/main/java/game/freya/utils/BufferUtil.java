package game.freya.utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtil {

    public static IntBuffer toIntBuffer(int[] data) {
        IntBuffer buffer = org.lwjgl.BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public static FloatBuffer toFloatBuffer(float[] data) {
        FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
}
