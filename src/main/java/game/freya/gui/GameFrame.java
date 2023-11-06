package game.freya.gui;

import fox.FoxLogo;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.gui.panes.FoxCanvas;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.net.SocketService;
import game.freya.services.WorldService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameFrame implements WindowListener, WindowStateListener {
    private static final Dimension LAUNCHER_DIM_MIN = new Dimension(1280, 768);
    private static final Dimension LAUNCHER_DIM = new Dimension(1440, 900);

    private final WorldService worldService;
    private final SocketService socketService;
    private GameController gameController;
    private WorldDTO worldDto;
    private JFrame frame;
    private FoxLogo logo;

    public void showMainMenu(GameController gameController) {
        this.gameController = gameController;

        frame = new JFrame(Constants.getGameName().concat(" v.")
                .concat(Constants.getGameVersion()), Constants.getGraphicsConfiguration());

        if (Constants.isShowStartLogo()) {
            try {
                logo = new FoxLogo();
                InputStream is = Constants.class.getResourceAsStream("/images/logo.png");
                if (is != null) {
                    logo.start(Constants.getGameVersion(), FoxLogo.IMAGE_STYLE.FILL, FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is));
                    logo.getEngine().join(10_000);
                }
            } catch (IOException ex) {
                log.error("Logo can not be displayed: {}", ExceptionUtils.getFullExceptionMessage(ex));
            } catch (InterruptedException e) {
                log.error("Logo thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        frame.addWindowStateListener(this);

        gameController.loadScreen(ScreenType.MENU_SCREEN);

        frame.setMinimumSize(LAUNCHER_DIM_MIN);
        frame.setPreferredSize(LAUNCHER_DIM);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setCursor(Constants.getDefaultCursor());

        if (logo != null && logo.getEngine().isAlive()) {
            try {
                log.info("Logo finished await...");
                logo.getEngine().join(10_000);
            } catch (InterruptedException ie) {
                log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
            }
        }

        // настройка фокуса для работы горячих клавиш:
        frame.setFocusable(false);
        frame.getRootPane().setFocusable(true);

        log.info("Show the MainFrame...");
        frame.setVisible(true);
        setInAc();
//        frame.requestFocusInWindow();

        if (Constants.getUserConfig().isFullscreen()) {
            log.info("Switch to fullscreen by UserConfig...");
            Constants.MON.switchFullscreen(frame);
        }
    }

    private void setInAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.add(frameName, frame.getRootPane());
        Constants.INPUT_ACTION.set(JComponent.WHEN_FOCUSED, frameName, "switchFullscreen", Constants.getUserConfig().getKeyFullscreen(), 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Try to switch fullscreen mode...");
                Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
                Constants.MON.switchFullscreen(Constants.getUserConfig().isFullscreen() ? frame : null);
            }
        });

        Constants.INPUT_ACTION.set(JComponent.WHEN_FOCUSED, frameName, "switchPause", Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Try to switch pause mode...");
                Constants.setPaused(!Constants.isPaused());
            }
        });
    }

    public void loadMenuScreen() {
        log.info("Try to load Menu screen...");
        clearFrame();
        frame.add(new MenuCanvas(gameController));
        frame.revalidate();
    }

    public void loadGameScreen() {
        if (worldService.count() > 0) {
            worldDto = worldService.findAll().stream().findAny().orElse(null);
        } else {
            worldDto = worldService.save(new WorldDTO("Demo world"));
        }

        if (worldDto == null) {
            log.error("The World variable is null. Can`t loaded here!");
            return;
        }
        if (worldDto.getPlayers().values().stream().noneMatch(pl -> pl.getUid().equals(gameController.getCurrentPlayer().getUid()))) {
            worldDto = worldService.addPlayerToWorld(worldDto, gameController.getCurrentPlayer());
        }

        log.info("Try to load World '{}' screen...", worldDto.getTitle());
        clearFrame();
        frame.add(new GameCanvas(worldDto, gameController)); // мир уже должен быть с игроком (-ами)!
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
        gameController.exitTheGame(worldDto);
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
        if (Constants.getUserConfig().isPauseOnHidden() && Constants.isPaused()) {
            log.info("Auto resume the game on frame restore is temporary off.");
//            Constants.setPaused(false);
//            log.info("Resume game...");
        }
    }

    private void onGameHide() {
        log.info("Hide or minimized");
        if (!Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.info("Paused...");
        }
    }
}
