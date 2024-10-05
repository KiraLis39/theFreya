package game.freya.gui;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import fox.images.FoxCursor;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
@Setter
@Getter
public class GameWindowSwing extends JFrame {
    private final ApplicationProperties props;
    private volatile boolean isReady;
    private GameControllerService gameControllerService;
    private JmeCanvasContext jmeContext;
    private Canvas canvas;

    public GameWindowSwing(GameControllerService gameControllerService, AppSettings settings, ApplicationProperties props) {
        this.gameControllerService = gameControllerService;
        this.props = props;

        setTitle(settings.getTitle());
        setDefaultCursor();
        setIconImage(Constants.CACHE.getBufferedImage("icon16"));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight()));
        setPreferredSize(new Dimension(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight()));
        setResizable(Constants.getGameConfig().isGameWindowResizable());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Constants.getGameCanvas().requestClose(false);
            }
        });

        setBackground(Color.YELLOW);
        getRootPane().setBackground(Color.GREEN);
        getContentPane().setBackground(Color.BLACK);
        getLayeredPane().setBackground(Color.MAGENTA);

        setLayout(null);
        prepareMenuPanes();
        attachJmeCanvasToFrame(settings);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        reloadCanvasDim();
    }

    public void reloadCanvasDim() {
        canvas.setSize(getContentPane().getBounds().width, getContentPane().getBounds().height);
        canvas.setLocation(0, 0);
    }

    private void prepareMenuPanes() {
//        setVideosPane(new AudioSettingsPane(this));
//        setVideosPane(new VideoSettingsPane(this));
//        setHotkeysPane(new HotkeysSettingsPane(this));
//        setGameplayPane(new GameplaySettingsPane(this));
//        setWorldCreatingPane(new WorldCreatingPane(this, gameControllerService));
//        setHeroCreatingPane(new HeroCreatingPane(this, gameControllerService));
//        setWorldsListPane(new WorldsListPane(this, gameControllerService));
//        setHeroesListPane(new HeroesListPane(this, gameControllerService));
//        setNetworkListPane(new NetworkListPane(this, gameControllerService));
//        setNetworkCreatingPane(new NetCreatingPane(this, gameControllerService));
    }

    private void setDefaultCursor() {
        try {
            // set JFrame default cursor:
            setCursor(FoxCursor.createCursor(Constants.CACHE.getBufferedImage("curP"), "curP"));
            getRootPane().setCursor(FoxCursor.createCursor(Constants.CACHE.getBufferedImage("curP"), "curP"));
            getGlassPane().setCursor(FoxCursor.createCursor(Constants.CACHE.getBufferedImage("curP"), "curP"));
            getContentPane().setCursor(FoxCursor.createCursor(Constants.CACHE.getBufferedImage("curP"), "curP"));
            getLayeredPane().setCursor(FoxCursor.createCursor(Constants.CACHE.getBufferedImage("curP"), "curP"));
        } catch (Exception _) {
        }
    }

    private void attachJmeCanvasToFrame(AppSettings settings) {
        Constants.setGameCanvas(new JMEApp(gameControllerService, settings, props));
        Constants.getGameCanvas().createCanvas();
        Constants.getGameCanvas().startCanvas();

        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        jmeContext = (JmeCanvasContext) Constants.getGameCanvas().getContext();
        jmeContext.setSystemListener(Constants.getGameCanvas());

        canvas = jmeContext.getCanvas();
        add(canvas);
    }
}
