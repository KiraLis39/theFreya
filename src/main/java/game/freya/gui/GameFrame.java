package game.freya.gui;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.ScreenType;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameFrame implements WindowListener, WindowStateListener {
    private final UIHandler uIHandler;

    private Dimension windowSize;

    private GameController gameController;

    private JFrame frame;

    public void showMainMenu(GameController gameController) {
        this.gameController = gameController;

        Dimension monitorSize = Constants.MON.getConfiguration().getBounds().getSize();
        double delta = monitorSize.getWidth() / monitorSize.getHeight();
        double newWidth = monitorSize.getWidth() * 0.75d;
        double newHeight = newWidth / delta;
        windowSize = new Dimension((int) newWidth, (int) newHeight);

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame(gameController.getGameConfig().getAppName().concat(" v.")
                    .concat(gameController.getGameConfig().getAppVersion()), Constants.getGraphicsConfiguration());

            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setCursor(Constants.getDefaultCursor());

            frame.setBackground(Color.CYAN);
            frame.getRootPane().setBackground(Color.MAGENTA);
            frame.getLayeredPane().setBackground(Color.YELLOW);

            // настройка фокуса для работы горячих клавиш:
            frame.setFocusable(false);

            frame.setLayout(null); // for frame null is BorderLayout
            frame.setIgnoreRepaint(true);

            frame.addWindowListener(this);
            frame.addWindowStateListener(this);

            // в полноэкранном режиме рисуется именно он:
            frame.getRootPane().setFocusable(true);

            setInAc();
        });

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

        log.info("Show the MainFrame...");
        Constants.checkFullscreenMode(frame, windowSize);

        gameController.loadScreen(ScreenType.MENU_SCREEN);
    }

    private void setInAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.add(frameName, frame.getRootPane());
        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFullscreen",
                Constants.getUserConfig().getKeyFullscreen(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the fullscreen mode...");
                        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
                        Constants.checkFullscreenMode(frame, windowSize);
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchPause",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the pause mode...");
                        Constants.setPaused(!Constants.isPaused());
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
                Constants.getUserConfig().getKeyDebug(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the debug mode...");
                        Constants.setDebugInfoVisible(!Constants.isDebugInfoVisible());
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFps",
                Constants.getUserConfig().getKeyFps(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the fps mode...");
                        Constants.setFpsInfoVisible(!Constants.isFpsInfoVisible());
                    }
                });
    }

    public void loadMenuScreen() {
        log.info("Try to load Menu screen...");
        clearFrame();
        frame.add(new MenuCanvas(uIHandler, frame, gameController));
        frame.revalidate();
    }

    public void loadGameScreen() {
        log.info("Try to load World '{}' screen...", gameController.getCurrentWorldTitle());
        clearFrame();
        frame.add(new GameCanvas(uIHandler, frame, gameController));
        frame.revalidate();
    }

    private void clearFrame() {
        boolean success = false;
        for (java.awt.Component comp : frame.getComponents()) {
            if (comp instanceof FoxCanvas fc) {
                fc.stop();
                frame.remove(fc);
                success = true;
            }
            if (comp instanceof JRootPane rp) {
                for (java.awt.Component cmp : rp.getContentPane().getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        fc.stop();
                        frame.remove(fc);
                        success = true;
                    }
                }
                for (java.awt.Component cmp : rp.getLayeredPane().getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        fc.stop();
                        frame.remove(fc);
                        success = true;
                    }
                }
            }
            if (comp instanceof JLayeredPane lp) {
                for (java.awt.Component cmp : lp.getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        fc.stop();
                        frame.remove(fc);
                        success = true;
                    }
                }
            }
        }

        if (!success) {
            log.error("Nor cleared frame!");
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        ) {
            gameController.saveCurrentWorld();
            gameController.exitTheGame(null);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        log.debug("Окно закрыто!");
    }

    @Override
    public void windowIconified(WindowEvent e) {
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
    public void windowDeactivated(WindowEvent e) {
        onGameHide();
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
        if (Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
//            log.info("Auto resume the game on frame restore is temporary off.");
            Constants.setPaused(false);
            log.debug("Resume game...");
        }
    }

    private void onGameHide() {
        log.debug("Hide or minimized");
        if (!Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.debug("Paused...");
        }
    }
}
