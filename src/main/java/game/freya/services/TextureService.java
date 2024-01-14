package game.freya.services;

import game.freya.config.Constants;
import game.freya.gl.Texture;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

@Slf4j
@Component
public class TextureService {
    private final HashMap<String, Texture> textures = new HashMap<>();

    public void loadMenuTextures() {
        URL imgsResource = getClass().getResource("/images");
        if (imgsResource == null) {
            log.error("Не обнаружен источник изображений меню!");
            return;
        }

        log.info("Начата загрузка текстур меню...");
        loadTextures(imgsResource.getPath().substring(1));
    }

    public void loadGameTextures() {
        URL imgsResource = getClass().getResource("/images");
        if (imgsResource == null) {
            log.error("Не обнаружен источник изображений игры!");
            return;
        }

        log.info("Начата загрузка текстур игры...");
        loadTextures(imgsResource.getPath().substring(1));
    }

    private void loadTextures(String imgPath) {
        if (!Constants.getGameConfig().isUseTextures()) {
            return;
        }
        clearTextures();
        try (Stream<Path> images = Files.walk(Path.of(imgPath))) {
            images
                    .filter(path -> !Files.isDirectory(path)
                            && !path.getFileName().toString().endsWith(".ico")
                            && !path.getFileName().toString().endsWith(".txt"))
                    .forEach(this::loadTexture);
            log.info("Загрузка текстур завершена.");
        } catch (Exception e) {
            log.error("Проблема возникла при обработке текстуры: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public int loadTexture(Path imagePath) {
        String name = imagePath.getFileName().toString().split("\\.")[0];
        log.debug("Bind texture {}...", name);
        Texture t = new Texture(imagePath.toAbsolutePath().toString());
        textures.put(name, t);
        return t.getId();
    }

    public boolean isTextureExist(String textureName) {
        return textures.containsKey(textureName);
    }

    public void bindTexture(String textureName) {
        if (!textures.containsKey(textureName)) {
            log.warn("Не найдена текстура {} для привязки", textureName);
            return;
        }
        textures.get(textureName).bind();
    }

    public void unbindTexture(String textureName) {
        if (textures.containsKey(textureName)) {
            textures.get(textureName).unbind();
        }
    }

    public void clearTextures() {
        log.info("Удаление текстур...");
        for (Texture tex : textures.values()) {
            tex.unbind();
            tex.delete();
        }
        textures.clear();
    }
}
