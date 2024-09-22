package game.freya.gui.panes;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.Controls;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
@Setter
@Getter
public class GameWindowJME extends SimpleApplication {
    private volatile boolean isReady;
    private Node scene;

    public GameWindowJME() {
        //        setCursor(Constants.getDefaultCursor());
        //        addWindowListener(new WindowAdapter() {
        //            public void windowClosing(WindowEvent e) {
        //                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
        //                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
        //                ) {
        //                    gameControllerService.exitTheGame(null, 0);
        //                }
        //            }
        //
        //            public void windowClosed(WindowEvent e) {
        //                log.debug("Окно закрыто!");
        //            }
        //
        //            public void windowIconified(WindowEvent e) {
        //                onGameHide();
        //            }
        //
        //            public void windowDeiconified(WindowEvent e) {
        //                onGameRestore();
        //            }
        //        });
        //        addWindowStateListener(new WindowAdapter() {
        //            public void windowStateChanged(WindowEvent e) {
        //                int oldState = e.getOldState();
        //                switch (e.getNewState()) {
        //                    case 6 -> {
        //                        log.info("Restored to fullscreen");
        //                        if ((oldState == 1 || oldState == 7)) {
        //                            onGameRestore();
        //                        }
        //                    }
        //                    case 0 -> {
        //                        log.info("Switch to windowed");
        //                        if ((oldState == 1 || oldState == 7)) {
        //                            onGameRestore();
        //                        }
        //                    }
        //                    case 1, 7 -> onGameHide();
        //                    default -> log.warn("MainMenu: Unhandled windows state: " + e.getNewState());
        //                }
        //            }
        //        });
        //
        //        setInAc();
        setSettings(Constants.getJmeSettings());
        setShowSettings(true); // not works
        setDisplayFps(Constants.getGameConfig().isFpsInfoVisible());
        setDisplayStatView(Constants.getGameConfig().isStatsInfoVisible());
        setPauseOnLostFocus(false);
        start(true);
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        setReady(true);
    }

    /* Interact with game events in the main loop */
    @Override
    public void simpleUpdate(float tpf) {

    }

    /* (optional) Make advanced modifications to frameBuffer and scene graph. */
    @Override
    public void simpleRender(RenderManager rm) {
        if (scene != null) {
            scene.getChild("Box").rotate(0.003f, 0.003f, 0.003f);
        }
    }

    public void setScene(Node scene) {
        this.scene = scene;
        rootNode.detachChildNamed("menuNode");
        rootNode.detachChildNamed("gameNode");

        log.info("Loading the scene '{}'...", scene.getName());
        rootNode.attachChild(scene);
    }

    private void setInAc() {
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFullscreen",
//                Constants.getUserConfig().getKeyFullscreen(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fullscreen mode...");
//                        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
//                        Constants.checkFullscreenMode(GameWindowJME.this, new Dimension(
//                                (int) Constants.getUserConfig().getWindowWidth(),
//                                (int) Constants.getUserConfig().getWindowHeight()));
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchPause",
//                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the pause mode...");
//                        Controls.setPaused(!Controls.isPaused());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
//                Constants.getUserConfig().getKeyDebug(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the debug mode...");
//                        Constants.getGameConfig().setDebugInfoVisible(!Constants.getGameConfig().isDebugInfoVisible());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFps",
//                Constants.getUserConfig().getKeyFps(), 0, new AbstractAction() {
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fps mode...");
//                        Constants.getGameConfig().setFpsInfoVisible(!Constants.getGameConfig().isFpsInfoVisible());
//                    }
//                });
    }

    private void onGameRestore() {
        if (Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Controls.setPaused(false);
            log.debug("Resume game...");
        }
    }

    private void onGameHide() {
        log.debug("Hide or minimized");
        if (!Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Controls.setPaused(true);
            log.debug("Paused...");
        }
    }

    public void toggleFullscreen() {
        if (Constants.getUserConfig().isFullscreen()) {
            settings.setFrequency(Constants.getUserConfig().getFpsLimit());
            settings.setFullscreen(false);
        } else {
            DisplayMode[] modes = FoxVideoMonitorUtil.getDisplayModes();
            int i = 0; // note: there are usually several, let's pick the first
            settings.setResolution(modes[i].getWidth(), modes[i].getHeight());
            settings.setFrequency(modes[i].getRefreshRate());
            settings.setBitsPerPixel(modes[i].getBitDepth());
            settings.setFullscreen(FoxVideoMonitorUtil.isFullScreenSupported());
        }
        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());

//        setSettings(settings);
        restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек
    }
}
