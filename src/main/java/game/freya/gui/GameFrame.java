package game.freya.gui;

import fox.FoxLogo;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.FoxCanvas;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Frame;
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
    private final UIHandler uIHandler;
    private Dimension monitorSize;
    private Dimension windowSize;
    private GameController gameController;
    private JFrame frame;
    private FoxLogo logo;

    public void showMainMenu(GameController gameController) {
        this.gameController = gameController;

        monitorSize = Constants.MON.getConfiguration().getBounds().getSize();
        double delta = monitorSize.getWidth() / monitorSize.getHeight();
        double newWidth = monitorSize.getWidth() * 0.75d;
        double newHeight = newWidth / delta;
        windowSize = new Dimension((int) newWidth, (int) newHeight);

        if (Constants.isShowStartLogo()) {
            try {
                logo = new FoxLogo();
                InputStream is = Constants.class.getResourceAsStream("/images/logo.png");
                if (is != null) {
                    logo.start(gameController.getGameConfig().getAppVersion(),
                            FoxLogo.IMAGE_STYLE.FILL, FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is));
                    logo.getEngine().join(10_000);
                }
            } catch (IOException e) {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/logo.png");
            } catch (InterruptedException e) {
                log.error("Logo thread was interrupted: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        }

        frame = new JFrame(gameController.getGameConfig().getAppName().concat(" v.")
                .concat(gameController.getGameConfig().getAppVersion()), Constants.getGraphicsConfiguration());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(null);
        frame.setCursor(Constants.getDefaultCursor());

        // настройка фокуса для работы горячих клавиш:
        frame.setFocusable(false);
        frame.getRootPane().setFocusable(true);

        frame.addWindowListener(this);
        frame.addWindowStateListener(this);

        setInAc();

        if (logo != null && logo.getEngine().isAlive()) {
            try {
                log.info("Logo finished await...");
                logo.getEngine().join(10_000);
            } catch (InterruptedException ie) {
                log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
            }
        }

        log.info("Show the MainFrame...");
        checkFullscreenMode();

        gameController.loadScreen(ScreenType.MENU_SCREEN, null);
    }

    private void checkFullscreenMode() {
        if (Constants.getUserConfig().isFullscreen()) {
            if (Constants.getUserConfig().getFullscreenType() == UserConfig.FullscreenType.EXCLUSIVE) {
                log.info("Switch to fullscreen by UserConfig...");
                Constants.MON.switchFullscreen(Constants.getUserConfig().isFullscreen() ? frame : null);
            } else if (Constants.getUserConfig().getFullscreenType() == UserConfig.FullscreenType.MAXIMIZE_WINDOW) {
                frame.dispose();

                frame.setResizable(true);
                frame.setUndecorated(true);

                frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
            }
        } else {
            frame.dispose();

            frame.setResizable(true);
            frame.setUndecorated(false);

            frame.setExtendedState(Frame.NORMAL);

            frame.setPreferredSize(windowSize);
            frame.setMaximumSize(windowSize);
            frame.setSize(windowSize);

            // frame.pack();
            frame.setLocationRelativeTo(null);
        }

        frame.setVisible(true);
        frame.setResizable(false);
    }

    private void setInAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.add(frameName, frame.getRootPane());
        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFullscreen",
                UserConfig.HotKeys.FULLSCREEN.getEvent(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.info("Try to switch the fullscreen mode...");
                        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
                        checkFullscreenMode();
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchPause",
                UserConfig.HotKeys.PAUSE.getEvent(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the pause mode...");
                        Constants.setPaused(!Constants.isPaused());
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
                UserConfig.HotKeys.DEBUG.getEvent(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        log.debug("Try to switch the debug mode...");
                        Constants.setDebugInfoVisible(!Constants.isDebugInfoVisible());
                    }
                });
    }

    public void loadMenuScreen() {
        log.info("Try to load Menu screen...");
        clearFrame();
        frame.getLayeredPane().add(new MenuCanvas(frame, gameController), Integer.valueOf(0));
        frame.revalidate();
    }

    public void loadGameScreen(WorldDTO worldDto) {
        if (worldDto == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "worldDto is NULL");
        }

        log.info("Try to load World '{}' screen...", worldDto.getTitle());
        clearFrame();
        gameController.setCurrentWorld(worldDto);
        frame.add(new GameCanvas(uIHandler, frame, gameController)); // мир уже должен быть с игроком (-ами)!
        frame.revalidate();
    }

    private void clearFrame() {
        boolean success = false;
        for (java.awt.Component comp : frame.getComponents()) {
            if (comp instanceof FoxCanvas fc) {
                log.debug("Found to remove from frame: {}", fc.getName());
                fc.stop();
                frame.remove(fc);
                success = true;
            }
            if (comp instanceof JRootPane rp) {
                for (java.awt.Component cmp : rp.getContentPane().getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        log.debug("Found to remove from frame: {}", fc.getName());
                        fc.stop();
                        frame.remove(fc);
                        success = true;
                    }
                }
                for (java.awt.Component cmp : rp.getLayeredPane().getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        log.debug("Found to remove from frame: {}", fc.getName());
                        fc.stop();
                        frame.remove(fc);
                        success = true;
                    }
                }
            }
            if (comp instanceof JLayeredPane lp) {
                for (java.awt.Component cmp : lp.getComponents()) {
                    if (cmp instanceof FoxCanvas fc) {
                        log.debug("Found to remove from frame: {}", fc.getName());
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
            gameController.exitTheGame(null);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        log.warn("Окно закрыто!");
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
        if (Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
//            log.info("Auto resume the game on frame restore is temporary off.");
            Constants.setPaused(false);
            log.info("Resume game...");
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
