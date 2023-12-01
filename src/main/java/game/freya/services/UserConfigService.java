package game.freya.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private GameController gameController;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
    }

    @PostConstruct
    public void load() throws IOException {
        Path url = Path.of(Constants.getUserSave());
        try {
            log.debug("Loading the save file from a disc...");
            if (Files.notExists(url.getParent()) || Files.notExists(url)) {
                save();
            }
            Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
            if (Constants.getUserConfig() == null) {
                Files.deleteIfExists(url);
                save();
                Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
            }
        } catch (MismatchedInputException mie) {
            log.error("#010 Is the save file empty? Ex.: {}", ExceptionUtils.getFullExceptionMessage(mie));
            save();
            gameController.exitTheGame(null);
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
            Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
        }
    }

    public void save() {
        log.debug("Saving the save file to disc...");
        try {
            if (!Files.exists(Path.of(Constants.getUserSave()).getParent())) {
                Files.createDirectories(Path.of(Constants.getUserSave()).getParent());
                mapper.writeValue(new File(Constants.getUserSave()), UserConfig.builder().build());
            } else if (!Files.exists(Path.of(Constants.getUserSave()))) {
                mapper.writeValue(new File(Constants.getUserSave()), UserConfig.builder().build());
            } else {
                mapper.writeValue(new File(Constants.getUserSave()), Constants.getUserConfig());
            }
        } catch (InvalidDefinitionException ide) {
            log.error("#d012 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(ide));
        } catch (Exception e) {
            log.error("#d013 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }
}
