package game.freya.gui;

import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.gui.panes.GamePaneRunnable;
import game.freya.gui.panes.GameWindow;
import game.freya.gui.panes.MenuCanvasRunnable;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameWindowController {
    private final UIHandler uIHandler;
    private final ApplicationProperties props;
    private GameControllerService gameControllerService;
    private CharacterService characterService;

    public void showMainMenu(GameControllerService gameControllerService, CharacterService characterService) {
        this.gameControllerService = gameControllerService;
        this.characterService = characterService;

        SwingUtilities.invokeLater(() -> {
            Constants.setGameWindow(new GameWindow(gameControllerService, props));

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
        });

        while (Constants.getGameWindow() == null || !Constants.getGameWindow().isReady()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException _) {
            }
        }

        log.info("The game is started!");
        loadScreen(ScreenType.MENU_SCREEN);
    }

    public void loadScreen(ScreenType screenType) {
        if (Constants.getGameWindow() == null) {
            throw new RuntimeException("frame is NULL? Why?");
        }

        log.info("Try to load screen {}...", screenType);
        switch (screenType) {
            case MENU_SCREEN -> loadMenuScreen();
            case GAME_SCREEN -> loadGameScreen();
            default -> log.error("Unknown screen failed to load: {}", screenType);
        }
    }

    public void loadMenuScreen() {
        log.info("Try to load Menu screen...");
        Constants.getGameWindow()
                .setScene(new MenuCanvasRunnable(uIHandler, Constants.getGameWindow(), gameControllerService, characterService, props));
    }

    public void loadGameScreen() {
        log.info("Try to load World '{}' screen...", gameControllerService.getCurrentWorldTitle());
        Constants.getGameWindow()
                .setScene(new GamePaneRunnable(uIHandler, Constants.getGameWindow(), gameControllerService, characterService, this, props));
    }
}
