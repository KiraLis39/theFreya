package game.freya.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.config.UserConfig;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConfigService {
    private final ObjectMapper mapper;

    @PostConstruct
    public void load() throws IOException {
        Path url = Path.of(Constants.getUserSaveUrl());
        try {
            log.debug("Loading the save file from a disc...");
            if (Files.notExists(url.getParent()) || Files.notExists(url)) {
                save();
            }
            Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
            if (Constants.getUserConfig() == null) {
                Files.deleteIfExists(url);
                save();
            }
        } catch (MismatchedInputException mie) {
            log.error("#010 Is the save file empty? Ex.: {}", ExceptionUtils.getFullExceptionMessage(mie));
            save();
        } catch (Exception e) {
            log.error("#011 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            new FOptionPane().buildFOptionPane("Сохранение повреждено:",
                    "Что-то не так с файлом сохранения. Он еще может быть работоспособным, но требует анализа для "
                            + "выявления проблемы. Будет создан новый файл сохранение, передайте старый, переименованный файл "
                            + "(corrupted_*) разработчику для решения проблемы.", FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            Path corrSave = Path.of(url.getParent().toString() + "/corrupted_" + url.getFileName());
            Files.deleteIfExists(corrSave);
            Files.copy(url, corrSave);
            save();
        } finally {
            Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
        }

        try {
            url = Path.of(Constants.getGameConfigUrl());
            if (Files.notExists(url.getParent()) || Files.notExists(url)) {
                createGameConfig();
            }

            Constants.setGameConfig(mapper.readValue(Files.readString(url), GameConfig.class));
            if (Constants.getGameConfig() == null) {
                Files.deleteIfExists(url);
                createGameConfig();
            }
        } catch (Exception e) {
            log.error("#011 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            new FOptionPane().buildFOptionPane("Конфигурация повреждена:",
                    "Что-то не так с файлом конфигурации игры. Она еще может быть работоспособной, но требует анализа для "
                            + "выявления проблемы. Будет создана новая, передайте старый, переименованный файл "
                            + "(corrupted_*) разработчику для решения проблемы.", FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            Path corrSave = Path.of(url.getParent().toString() + "/corrupted_" + url.getFileName());
            Files.deleteIfExists(corrSave);
            Files.copy(url, corrSave);
            createGameConfig();
        } finally {
            Constants.setGameConfig(mapper.readValue(Files.readString(url), GameConfig.class));
        }
    }

    private void createGameConfig() {
        log.debug("Saving the game config to disc...");
        try {
            if (!Files.exists(Path.of(Constants.getGameConfigUrl()).getParent())) {
                Files.createDirectories(Path.of(Constants.getGameConfigUrl()).getParent());
                mapper.writeValue(new File(Constants.getGameConfigUrl()), GameConfig.builder().build());
            } else if (!Files.exists(Path.of(Constants.getGameConfigUrl()))
                    || Files.readString(Path.of(Constants.getGameConfigUrl())).equals("null")
            ) {
                mapper.writeValue(new File(Constants.getGameConfigUrl()), GameConfig.builder().build());
            } else {
                mapper.writeValue(new File(Constants.getGameConfigUrl()), Constants.getGameConfig());
            }
        } catch (InvalidDefinitionException ide) {
            log.error("#d012 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(ide));
        } catch (Exception e) {
            log.error("#d013 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void save() {
        log.debug("Saving the save file to disc...");
        try {
            if (!Files.exists(Path.of(Constants.getUserSaveUrl()).getParent())) {
                Files.createDirectories(Path.of(Constants.getUserSaveUrl()).getParent());
                mapper.writeValue(new File(Constants.getUserSaveUrl()), UserConfig.builder().build());
            } else if (!Files.exists(Path.of(Constants.getUserSaveUrl()))) {
                mapper.writeValue(new File(Constants.getUserSaveUrl()), UserConfig.builder().build());
            } else {
                mapper.writeValue(new File(Constants.getUserSaveUrl()), Constants.getUserConfig());
            }
        } catch (InvalidDefinitionException ide) {
            log.error("#d012 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(ide));
        } catch (Exception e) {
            log.error("#d013 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }
}
