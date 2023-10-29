package game.freya.gui;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.GameConfig;
import game.freya.entities.World;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.MainMenuCP;
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
    private static final Dimension LAUNCHER_DIM_MIN = new Dimension(800, 600);
    private static final Dimension LAUNCHER_DIM = new Dimension(1280, 1050);

    private final WorldService worldService;
    private final GameConfig config;
    private GameController gameController;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
    }

    @Autowired
    public void showMainMenu() {
        JFrame frame = new JFrame(config.getGameTitle().concat(" v.")
                .concat(config.getGameVersion())
                .concat(" (")
                .concat(String.valueOf(Constants.getScreenDiscreteValue()))
                .concat(")"),
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        frame.addWindowStateListener(this);

        WorldDTO world;
        if (worldService.count() > 0) {
            world = WorldMapper.toDto(worldService.findAll().stream().findAny().orElse(null));
        } else {
            world = WorldMapper.toDto(worldService.save(World.builder().title("Demo world").build()));
        }

        frame.add(new MainMenuCP(frame.getGraphicsConfiguration(), world));

        frame.setMinimumSize(LAUNCHER_DIM_MIN);
        frame.setPreferredSize(LAUNCHER_DIM);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        gameController.closeConnections();
        gameController.exitTheGame();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        log.warn("fail closing 2..");
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == 6) {
            log.info("Restored to fullscreen");
        } else if (e.getNewState() == 0) {
            log.info("Switch to windowed");
        } else {
            log.warn("GameFrame: Unhandled windows state: " + e.getNewState());
        }
    }
}
