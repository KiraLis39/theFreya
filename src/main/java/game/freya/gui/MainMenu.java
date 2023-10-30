package game.freya.gui;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.World;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.DemoCanvas;
import game.freya.mappers.WorldMapper;
import game.freya.services.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainMenu implements WindowListener, WindowStateListener {
    private static final Dimension LAUNCHER_DIM_MIN = new Dimension(1280, 768);
    private static final Dimension LAUNCHER_DIM = new Dimension(1440, 900);

    private final WorldService worldService;
    private final WorldMapper worldMapper;
    private final GameConfig config;
    private GameController gameController;

    private WorldDTO world;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
    }

    @Autowired
    public void showMainMenu() {
        JFrame frame = new JFrame(config.getGameTitle().concat(" v.")
                .concat(config.getGameVersion()), Constants.getDefaultConfiguration());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        frame.addWindowStateListener(this);

        if (worldService.count() > 0) {
            world = worldMapper.toDto(worldService.findAll().stream().findAny().orElse(null));
        } else {
            world = worldMapper.toDto(worldService.save(World.builder().title("Demo world").build()));
        }

        frame.add(new DemoCanvas(frame.getGraphicsConfiguration(), world));

        frame.setMinimumSize(LAUNCHER_DIM_MIN);
        frame.setPreferredSize(LAUNCHER_DIM);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // todo: поднять на ноги:
//        FoxLogo logo = new FoxLogo();
//        try {
//            BufferedImage[] logoImages = {ImageIO.read(new File(Constants.getLogoImageUrl()))};
//            logo.start("12345", logoImages, FoxLogo.IMAGE_STYLE.FILL, FoxLogo.BACK_STYLE.ASIS, KeyEvent.VK_ESCAPE);
//            logo.getEngine().join(6000);
//        } catch (IOException ex) {
//            log.error("Logo can not be displayed: {}", ExceptionUtils.getFullExceptionMessage(ex));
//        } catch (InterruptedException e) {
//            log.error("Logo thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
//        }

        frame.setCursor(Constants.DEFAULT_CUR);
        frame.setVisible(true);

        // todo: доработать фулскрин:
        //Constants.MON.switchFullscreen(frame);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        gameController.saveTheGame(world);
        gameController.closeConnections();
        gameController.exitTheGame();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        log.warn("fail closing 2..");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        onGameHide();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        onGameHide();
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        onGameRestore();
    }

    @Override
    public void windowActivated(WindowEvent e) {
        onGameRestore();
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        int oldState = e.getOldState();
        int newState = e.getNewState();

        switch (newState) {
            case 6 -> {
                log.info("Restored to fullscreen");
                if ((oldState == 1 || oldState == 7)) {
                    onGameRestore();
                }
            }
            case 0 -> {
                log.info("Switch to windowed");
                if ((oldState == 1 || oldState == 7)) {
                    onGameRestore();
                }
            }
            case 1, 7 -> onGameHide();
            default -> log.warn("MainMenu: Unhandled windows state: " + e.getNewState());
        }
    }

    private void onGameRestore() {
        if (Constants.PAUSE_ON_HIDDEN && Constants.isPaused()) {
            Constants.setPaused(false);
            log.info("Resume game...");
        }
    }

    private void onGameHide() {
        log.info("Hide or minimized");
        if (!Constants.isPaused() && Constants.PAUSE_ON_HIDDEN) {
            Constants.setPaused(true);
            log.info("Paused...");
        }
    }
}
