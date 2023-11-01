package game.freya.gui;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.FoxCanvas;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.services.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameFrame implements WindowListener, WindowStateListener {
    private static final Dimension LAUNCHER_DIM_MIN = new Dimension(1280, 768);
    private static final Dimension LAUNCHER_DIM = new Dimension(1440, 900);

    private final WorldService worldService;
    private GameController gameController;
    private WorldDTO world;
    private JFrame frame;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
    }

    @PostConstruct
    public void showMainMenu() {
        frame = new JFrame(Constants.getGameName().concat(" v.")
                .concat(Constants.getGameVersion()), Constants.getGraphicsConfiguration());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        frame.addWindowStateListener(this);

        loadMenuScreen();

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

    public void loadMenuScreen() {
        log.info("Try to load Menu screen...");
        clearFrame();
        frame.add(new MenuCanvas(gameController));
        frame.revalidate();
    }

    public void loadGameScreen() {
        if (worldService.count() > 0) {
            world = worldService.findAll().stream().findAny().orElse(null);
        } else {
            world = worldService.save(new WorldDTO("Demo world"));
        }

        if (world == null) {
            log.error("The World variable is null. Can`t loaded here!");
            return;
        }
        log.info("Try to load World '{}' screen...", world.getTitle());
        clearFrame();
        frame.add(new GameCanvas(world));
        frame.revalidate();
    }

    private void clearFrame() {
        for (java.awt.Component comp : frame.getComponents()) {
            if (comp instanceof FoxCanvas fc) {
                log.info("Found to remove from frame: {}", fc.getName());
                fc.stop();
                frame.remove(fc);
            }
            if (comp instanceof JRootPane rp) {
                for (java.awt.Component cmp : rp.getContentPane().getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        log.info("Found to remove from frame: {}", fc.getName());
                        fc.stop();
                        frame.remove(fc);
                    }
                }
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        gameController.exitTheGame(world);
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
            log.info("Auto resume the game on frame restore is temporary off.");
//            Constants.setPaused(false);
//            log.info("Resume game...");
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
