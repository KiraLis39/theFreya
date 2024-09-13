package game.freya.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.utils.ExceptionUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameConfigService {
    private final ObjectMapper mapper;

    @PostConstruct
    public void load() {
        Path gameConfigFile = Path.of(Constants.getGameConfigFile());
        try {
            log.debug("Loading the game config file from a disc...");
            if (Files.notExists(gameConfigFile.getParent()) || Files.notExists(gameConfigFile)) {
                createOrSaveGameConfig();
            }
            Constants.setGameConfig(mapper.readValue(Files.readString(gameConfigFile), GameConfig.class));
            if (Constants.getGameConfig() == null) {
                Files.deleteIfExists(gameConfigFile);
                createOrSaveGameConfig();
                Constants.setGameConfig(mapper.readValue(Files.readString(gameConfigFile), GameConfig.class));
            }
        } catch (Exception e) {
            log.error("#010 Is the save file empty? Ex.: {}", ExceptionUtils.getFullExceptionMessage(e));
            createOrSaveGameConfig();
            System.exit(46);
        }
    }

    public void createOrSaveGameConfig() {
        log.debug("Saving the game config to disc...");
        try {
            Path configFile = Path.of(Constants.getGameConfigFile());
            if (Files.notExists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
                mapper.writeValue(configFile.toFile(), new GameConfig());
            } else if (Files.notExists(configFile)) {
                mapper.writeValue(configFile.toFile(), new GameConfig());
            } else {
                mapper.writeValue(configFile.toFile(), Constants.getGameConfig());
            }
            Constants.setGameConfig(mapper.readValue(configFile.toFile(), GameConfig.class));
        } catch (InvalidDefinitionException ide) {
            log.error("#d012 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(ide));
        } catch (Exception e) {
            log.error("#d013 Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }
}
