package game.freya.gui.panes;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class GameWindowJME extends SimpleApplication {
    @Getter
    @Setter
    private volatile boolean isReady;
    private volatile RunnableCanvasPanel scene;
    private ApplicationProperties props;
    private GameControllerService gameControllerService;

    public GameWindowJME(GameControllerService gameControllerService, ApplicationProperties props) {
        this.gameControllerService = gameControllerService;
        this.props = props;

//        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//        setCursor(Constants.getDefaultCursor());

//        setBackground(Color.DARK_GRAY);
//        getRootPane().setBackground(Color.MAGENTA);
//        getLayeredPane().setBackground(Color.YELLOW);

        // настройка фокуса для работы горячих клавиш:
//        setFocusable(false);
        // в полноэкранном режиме рисуется именно он:
//        getRootPane().setFocusable(true);

//        setLayout(null); // for frame null is BorderLayout
//        setResizable(false);
//        setIgnoreRepaint(true);

//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
//                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
//                ) {
//                    gameControllerService.exitTheGame(null, 0);
//                }
//            }
//
//            @Override
//            public void windowClosed(WindowEvent e) {
//                log.debug("Окно закрыто!");
//            }
//
//            @Override
//            public void windowIconified(WindowEvent e) {
//                onGameHide();
//            }
//
//            @Override
//            public void windowDeiconified(WindowEvent e) {
//                onGameRestore();
//            }
//
//            @Override
//            public void windowActivated(WindowEvent e) {
//                onGameRestore();
//            }
//
//            @Override
//            public void windowDeactivated(WindowEvent e) {
//                onGameHide();
//            }
//        });
//        addWindowStateListener(new WindowAdapter() {
//            @Override
//            public void windowStateChanged(WindowEvent e) {
//                int oldState = e.getOldState();
//
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

        AppSettings settings = new AppSettings(true);
        settings.setTitle(props.getAppName().concat(" v.").concat(props.getAppVersion()));
        settings.setWindowSize((int) Constants.getUserConfig().getWindowWidth(), (int) Constants.getUserConfig().getWindowHeight());

        setSettings(settings);
        start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, new Vector3f(1, 1, 1));
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void setScene(RunnableCanvasPanel scene) {
//        this.scene = scene;
//        clearFrame();
//        setVisible(true);
//        revalidate();
//        createBufferStrategy(Constants.getUserConfig().getBufferedDeep());
    }

    private void clearFrame() {
//        Arrays.stream(getComponents())
//                .filter(JRootPane.class::isInstance)
//                .forEach(rp -> Arrays.stream(((JRootPane) rp).getContentPane().getComponents())
//                        .filter(RunnableCanvasPanel.class::isInstance)
//                        .forEach(fc -> {
//                            ((RunnableCanvasPanel) fc).stop();
//                            remove(fc);
//                        }));
//        add(this.scene);
    }

    private void setInAc() {
//        final String frameName = "mainFrame";
//
//        Constants.INPUT_ACTION.add(frameName, getRootPane());
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFullscreen",
//                Constants.getUserConfig().getKeyFullscreen(), 0, new AbstractAction() {
//                    @Override
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
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the pause mode...");
//                        Controls.setPaused(!Controls.isPaused());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
//                Constants.getUserConfig().getKeyDebug(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the debug mode...");
//                        Constants.getGameConfig().setDebugInfoVisible(!Constants.getGameConfig().isDebugInfoVisible());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFps",
//                Constants.getUserConfig().getKeyFps(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fps mode...");
//                        Constants.getGameConfig().setFpsInfoVisible(!Constants.getGameConfig().isFpsInfoVisible());
//                    }
//                });
    }

    private void onGameRestore() {
//        if (Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
//            Controls.setPaused(false);
//            log.debug("Resume game...");
//        }
    }

    private void onGameHide() {
//        log.debug("Hide or minimized");
//        if (!Controls.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
//            Controls.setPaused(true);
//            log.debug("Paused...");
//        }
    }

    public void checkFullscreenMode() {
        log.info("Show the MainFrame...");
        Constants.checkFullscreenModeJME(this, new Dimension(
                (int) Constants.getUserConfig().getWindowWidth(),
                (int) Constants.getUserConfig().getWindowHeight()));
    }
}
