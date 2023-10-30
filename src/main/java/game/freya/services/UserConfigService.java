package game.freya.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConfigService {
    private final ObjectMapper mapper;

    public void load(Path url) {
        try {
            if (!Files.exists(url.getParent())) {
                Files.createDirectories(url.getParent());
            }
            Constants.setUserConfig(mapper.readValue(Files.readString(url), UserConfig.class));
        } catch (Exception e) {
            log.error("Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void save() {
        try {
            if (!Files.exists(Path.of(Constants.getUserSave()).getParent())) {
                Files.createDirectories(Path.of(Constants.getUserSave()).getParent());
            }
            mapper.writeValue(new File(Constants.getUserSave()), Constants.getUserConfig());
        } catch (Exception e) {
            log.error("Save all methode exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

//    public void resetControlKeys() {
//        Constants.getConfig().setKeyLeft(KeyEvent.VK_LEFT);
//        Constants.getConfig().setKeyLeftMod(0);
//        Constants.getConfig().setKeyRight(KeyEvent.VK_RIGHT);
//        Constants.getConfig().setKeyRightMod(0);
//        Constants.getConfig().setKeyDown(KeyEvent.VK_DOWN);
//        Constants.getConfig().setKeyDownMod(0);
//        Constants.getConfig().setKeyStuck(KeyEvent.VK_UP);
//        Constants.getConfig().setKeyStuckMod(0);
//        Constants.getConfig().setKeyRotate(KeyEvent.VK_Z);
//        Constants.getConfig().setKeyRotateMod(0);
//        Constants.getConfig().setKeyConsole(KeyEvent.VK_BACK_QUOTE);
//        Constants.getConfig().setKeyConsoleMod(0);
//        Constants.getConfig().setKeyFullscreen(KeyEvent.VK_F);
//        Constants.getConfig().setKeyFullscreenMod(0);
//        Constants.getConfig().setKeyPause(KeyEvent.VK_ESCAPE);
//        Constants.getConfig().setKeyPauseMod(0);
//
//        saveAll();
//    }
}
