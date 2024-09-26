package game.freya.gui;

import com.jme3.system.AppSettings;
import game.freya.config.Constants;
import game.freya.dto.roots.WorldDto;
import game.freya.enums.other.ScreenType;
import game.freya.gui.panes.GameWindowJME;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.services.WorldService;
import game.freya.states.GamePlayState;
import game.freya.states.MainMenuState;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SceneController {
    private final UIHandler uIHandler;
    private final CharacterService characterService;
    private final WorldService worldService;
    private GameControllerService gameControllerService;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void showGameWindow(AppSettings settings) {
        Constants.setGameWindow(new GameWindowJME(gameControllerService, settings));

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
        while (!Constants.getGameWindow().isReady()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException _) {
            }
        }

        log.info("Игровое окно запущено в потоке {}", Thread.currentThread().getName());
        loadScene(ScreenType.MENU_SCREEN);
    }

    public void loadScene(ScreenType screenType) {
        while (Constants.getGameWindow().getAudioRenderer() == null) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        WorldDto w = worldService.getCurrentWorld();
        log.info("Try to load screen '".concat(screenType.toString()).concat(w != null ? "' with World " + w.getName() : "'").concat("..."));
        Constants.getGameWindow().setScene(switch (screenType) {
            case MENU_SCREEN -> new MainMenuState(gameControllerService);
            case GAME_SCREEN -> new GamePlayState(gameControllerService);
        });
    }
}
