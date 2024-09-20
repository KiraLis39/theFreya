package game.freya.gui;

import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.GameWindowJME;
import game.freya.gui.panes.handlers.UIHandler;
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
    private final UIHandler uIHandler;
    private final CharacterService characterService;
    private final WorldService worldService;
    private GameControllerService gameControllerService;
    private final long gameWindowShowMaxAwait = 6_000;

    @Autowired
    public void init(@Lazy GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
    }

    public void showGameWindow() {
        // в отдельном потоке запускается UI:
        SwingUtilities.invokeLater(() -> {
            Thread.currentThread().setName("GameWindowThread");

//            Constants.setGameWindow(new GameWindow(gameControllerService, props));
            Constants.setGameWindow(new GameWindowJME(gameControllerService, props));

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

            Constants.getGameWindow().checkFullscreenMode();
            Constants.getGameWindow().setReady(true);

            log.info("Игровое окно запущено в потоке {}", Thread.currentThread().getName());
        });

        // ждём запуска главного окна игры:
        long was = System.currentTimeMillis();
        while ((Constants.getGameWindow() == null || !Constants.getGameWindow().isReady())
                && System.currentTimeMillis() - was < gameWindowShowMaxAwait
        ) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        if (Constants.getGameWindow() == null || !Constants.getGameWindow().isReady()) {
            throw new GlobalServiceException(ErrorMessages.UNIVERSAL_ERROR_MESSAGE_TEMPLATE, "Игра не запустилась корректно за отведённое время");
        }

        log.info("The game is started!");
        loadScene(ScreenType.MENU_SCREEN);
    }

    public void loadScene(ScreenType screenType) {
        log.info("Try to load screen {}...", screenType);
        switch (screenType) {
            case MENU_SCREEN -> loadMenuScene();
            case GAME_SCREEN -> loadGameScreen();
            default -> log.error("Unknown screen failed to load: {}", screenType);
        }
    }

    private void loadMenuScene() {
        log.info("Try to load Menu screen...");
//        Constants.getGameWindow()
//                .setScene(new MenuCanvasRunnable(uIHandler, Constants.getGameWindow(), gameControllerService, characterService, props));
    }

    private void loadGameScreen() {
        log.info("Try to load World '{}' screen...", worldService.getCurrentWorld().getName());
//        Constants.getGameWindow()
//                .setScene(new GamePaneRunnable(uIHandler, Constants.getGameWindow(), gameControllerService, characterService, this, props));
    }
}
