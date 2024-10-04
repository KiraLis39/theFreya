package game.freya.gui;

import com.jme3.system.AppSettings;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.gui.states.GamePlayState;
import game.freya.gui.states.MainMenuState;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SceneController {
    private final ApplicationProperties props;
    private final CharacterService characterService;
    private final WorldService worldService;
    private GameControllerService gameControllerService;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void createGameCanvasAndWindow(AppSettings settings) {
        Constants.setGameCanvas(new JMEApp(gameControllerService, settings));
        Constants.getGameCanvas().createCanvas();

        SwingUtilities.invokeLater(() -> {
            Constants.setGameFrame(new GameWindowSwing(gameControllerService, settings));

            // ждём пока кончится показ лого:
            if (Constants.getLogo() != null && Constants.getLogo().getEngine().isAlive()) {
                try {
                    log.info("Logo finished await...");
                    Constants.getLogo().getEngine().join(3_000);
                } catch (InterruptedException ie) {
                    log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
                    Constants.getLogo().getEngine().interrupt();
                } finally {
                    Constants.getLogo().finalLogo();
                }
            }

            // ждём пока JME-окно не прогрузится:
            while (Constants.getGameCanvas() == null || !Constants.getGameCanvas().isReady()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException _) {
                }
            }

            if (Constants.getCurrentScreenAspect() == 0) {
                // запоминаем аспект разрешения монитора для дальнейших преобразований окон:
                Constants.setCurrentScreenAspect(FoxVideoMonitorUtil
                        .getConfiguration().getBounds().getWidth() / FoxVideoMonitorUtil.getConfiguration().getBounds().getHeight());
            }

            if (Constants.getGameFrame() == null || Constants.getGameCanvas().isShowSettings())
                log.info("Игровое окно запущено в потоке {}", Thread.currentThread().getName());
            loadScene(ScreenType.MENU_SCREEN);
        });
    }

    public void loadScene(ScreenType screenType) {
        while (Constants.getGameCanvas().getAudioRenderer() == null) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException _) {
            }
        }

        log.info("Try to load screen '".concat(screenType.toString())
                .concat(worldService.getCurrentWorld() != null ? "' with World " + worldService.getCurrentWorld().getName() : "'")
                .concat("..."));

        Constants.getGameCanvas().setScene(switch (screenType) {
            case MENU_SCREEN -> new MainMenuState(gameControllerService, props);
            case GAME_SCREEN -> new GamePlayState(gameControllerService, props);
        });
    }
}
