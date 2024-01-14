package game.freya.gl;

import game.freya.config.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

@Slf4j
public class Texture {
    @Getter
    private final String url;

    @Getter
    private final int id;

    @Setter
    private boolean isFont;

    /*
        url => String absolutePath = Texture.class.getClassLoader().getResource(file).getPath().substring(1);
        if (!System.getProperty("os.name").contains("Windows")) {
            // stbi_load requires a file system path, NOT a classpath resource path
            absolutePath = File.separator + absolutePath;
        }
     */
    public Texture(String absolutePath) {
        this.url = absolutePath;
        this.id = glGenTextures();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.id);

        /*
         * GL_REPEAT : поведение текстур по умолчанию. Повторяет изображение текстуры.
         * GL_MIRRORED_REPEAT : То же, что и GL_REPEAT , но зеркально отражает изображение при каждом повторе.
         * GL_CLAMP_TO_EDGE : фиксирует координаты между 0и 1. В результате более высокие координаты привязываются к краю, что приводит к растягиванию края.
         * GL_CLAMP_TO_BORDER : координатам за пределами диапазона теперь присваивается цвет границы, указанный пользователем.
         */

        // set the texture wrapping/filtering options (on the currently bound texture object)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        //        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        //        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        //        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        //        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

        /*
         * Если мы выберем опцию GL_CLAMP_TO_BORDER, следует указать цвет границы.
         *  Это делается с использованием эквивалента glTexParameter с GL_TEXTURE_BORDER_COLOR, где передаем
         *  массив с плавающей запятой значения цвета границы:
         * float borderColor[] = { 1.0f, 1.0f, 0.0f, 1.0f };
         * glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
         */

        /*
         * GL_NEAREST_MIPMAP_NEAREST : принимает ближайшее MIP-карту, соответствующую размеру пикселя, и использует интерполяцию ближайшего соседа для выборки текстуры.
         * GL_LINEAR_MIPMAP_NEAREST : берет ближайший уровень MIP-карты и производит выборку этого уровня с помощью линейной интерполяции.
         * GL_NEAREST_MIPMAP_LINEAR : линейно интерполирует между двумя MIP-картами, которые наиболее точно соответствуют размеру пикселя, и производит выборку интерполированного уровня посредством интерполяции ближайшего соседа.
         * GL_LINEAR_MIPMAP_LINEAR : линейно интерполирует между двумя ближайшими MIP-картами и производит выборку интерполированного уровня посредством линейной интерполяции.
         *
         * glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         * glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
         *
         * Распространенной ошибкой является установка одного из параметров фильтрации MIP-карт в качестве фильтра увеличения.
         *  Это не имеет никакого эффекта, поскольку MIP-карты в основном используются при уменьшении масштаба текстур:
         *  увеличение текстур не использует MIP-карты, и установка опции фильтрации MIP-карт приведет к генерации кода ошибки OpenGL GL_INVALID_ENUM
         *
         * Фильтрация текстур может быть установлена для увеличении или уменьшении масштаба, чтобы вы могли, например,
         *  использовать фильтрацию GL_NEAREST, когда текстуры масштабируются вниз, и GL_LINEAR для текстур с повышением масштаба
         * glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
         * glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
         *
         */
        switch (Constants.getUserConfig().getTexturesFilteringLevel()) {
            case NEAREST -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            }
            case LINEAR -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            }
            case MIPMAP -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
            }
            case CUSTOM -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // или наоборот?
            }
            default ->
                    log.error("Нет такого типа фильтрации текстур: {}", Constants.getUserConfig().getTexturesFilteringLevel());

            //            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            //            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
            //            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        }

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer data = stbi_load(this.url, width, height, channels, 0); // STBI_rgb_alpha | STBI_rgb
        if (data == null) {
            log.error("Could not decode image file [" + this.url + "]: [" + STBImage.stbi_failure_reason() + "]");
            return;
        }

        // Load and generate the texture:
        /*
         * Первый аргумент определяет цель текстуры; установка для этого параметра значения GL_TEXTURE_2D означает, что эта операция будет
         *  генерировать текстуру для текущего привязанного объекта текстуры в той же цели
         *  (поэтому любые текстуры, привязанные к целям GL_TEXTURE_1D или GL_TEXTURE_3D, не будут затронуты).
         *
         * Второй аргумент указывает уровень MIP-карты, для которого мы хотим создать текстуру, если вы хотите установить каждый уровень
         *  MIP-карты вручную, но мы оставим его на базовом уровне, который равен 0.
         *
         * Третий аргумент сообщает OpenGL, в каком формате мы хотим сохранить текстуру. Наше изображение имеет только значения, поэтому
         *  мы сохраним и RGB текстуру со значениями RGB.
         *
         * Четвертый и пятый аргументы задают ширину и высоту результирующей текстуры. Мы сохранили их ранее при загрузке изображения,
         *  поэтому будем использовать соответствующие переменные.
         *
         * Следующим аргументом всегда должно быть 0(некоторые устаревшие вещи).
         *  7-й и 8-й аргументы определяют формат и тип данных исходного изображения. Мы загрузили изображение со RGB значениями и сохранили
         *  их как chars (байты), поэтому передадим соответствующие значения.
         *
         * Последний аргумент — это фактические данные изображения.
         */
        int mipLevel = 0;
        if (channels.get(0) == 2) {
            glTexImage2D(GL_TEXTURE_2D, mipLevel, GL_RG, width.get(0), height.get(0), 0, GL_RG, GL_UNSIGNED_BYTE, data);
        } else if (channels.get(0) == 3) {
            glTexImage2D(GL_TEXTURE_2D, mipLevel, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, data);
        } else if (channels.get(0) == 4) {
            glTexImage2D(GL_TEXTURE_2D, mipLevel, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        } else {
            log.error("Неожиданный набор каналов текстуры: {}", channels.get(0));
        }

        /*
         * Создание коллекции текстур с мип-отображением для каждого изображения текстуры вручную затруднительно,
         *  но, к счастью, OpenGL может выполнить всю работу за нас с помощью одного вызова glGenerateMipmap после того, как мы создали текстуру.
         */
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(data);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void delete() {
        glDeleteTextures(this.id);
    }
}
