package game.freya.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fox.components.FOptionPane;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.utils.ExceptionUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
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
        Path userSaveFile = Path.of(Constants.getUserSaveFile());
        try {
            log.debug("Loading the user save file from a disc...");
            if (Files.notExists(userSaveFile.getParent()) || Files.notExists(userSaveFile)) {
                createOrSaveUserConfig();
            }

            Constants.setUserConfig(mapper.readValue(Files.readString(userSaveFile), UserConfig.class));
            if (Constants.getUserConfig() == null) {
                Files.deleteIfExists(userSaveFile);
                createOrSaveUserConfig();
                Constants.setUserConfig(mapper.readValue(Files.readString(userSaveFile), UserConfig.class));
            }
        } catch (MismatchedInputException mie) {
            log.error("#010 Is the save file empty? Ex.: {}", ExceptionUtils.getFullExceptionMessage(mie));
            createOrSaveUserConfig();
            Constants.setUserConfig(mapper.readValue(Files.readString(userSaveFile), UserConfig.class));
        } catch (Exception e) {
            log.error("#011 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            new FOptionPane().buildFOptionPane("Сохранение повреждено:",
                    "Что-то не так с файлом сохранения. Он еще может быть работоспособным, но требует анализа для "
                            + "выявления проблемы. Будет создан новый файл сохранение, передайте старый, переименованный файл "
                            + "(corrupted_*) разработчику для решения проблемы.", FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            Path corrSave = Path.of(userSaveFile.getParent().toString() + "/corrupted_" + userSaveFile.getFileName());
            Files.deleteIfExists(corrSave);
            Files.copy(userSaveFile, corrSave);
            createOrSaveUserConfig();
            Constants.setUserConfig(mapper.readValue(Files.readString(userSaveFile), UserConfig.class));
        }
    }

    public void createOrSaveUserConfig() throws IOException {
        log.debug("Saving the user save file to disc...");
        try {
            UserConfig uConf = UserConfig.builder().build();
            if (uConf.getWindowWidth() == 0 || uConf.getWindowHeight() == 0) {
                Dimension dmode = FoxVideoMonitorUtil.getConfiguration().getBounds().getSize();
                double aspect = dmode.getWidth() / dmode.getHeight();

                // запоминаем аспект разрешения монитора для дальнейших преобразований окон:
                Constants.setCurrentScreenAspect(aspect);

                double width = dmode.getWidth() * 0.75d;
                double height = dmode.getHeight() * 0.75d;
                uConf.setWindowWidth((int) width);
                uConf.setWindowHeight((int) height);
            }

            Path saveFile = Path.of(Constants.getUserSaveFile());
            if (Files.notExists(saveFile.getParent())) {
                Files.createDirectories(saveFile.getParent());
                mapper.writeValue(saveFile.toFile(), uConf);
            } else if (Files.notExists(saveFile)) {
                mapper.writeValue(saveFile.toFile(), uConf);
            }
            Constants.setUserConfig(mapper.readValue(new File(Constants.getUserSaveFile()), UserConfig.class));
            mapper.writeValue(saveFile.toFile(), Constants.getUserConfig());
        } catch (InvalidDefinitionException ide) {
            log.error("#d012 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(ide));
        } catch (Exception e) {
            log.error("#d013 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            throw e;
        }
    }
}
