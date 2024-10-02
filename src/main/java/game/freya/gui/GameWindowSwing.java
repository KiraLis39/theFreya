package game.freya.gui;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import fox.images.FoxCursor;
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
    private volatile boolean isReady;
    private GameControllerService gameControllerService;
    private JmeCanvasContext jmeContext;
    private JPanel test;

    public GameWindowSwing(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;

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

        attachJmeCanvasToFrame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // log.info("Canvas location/size: {} / {}", canvas.getLocation(), canvas.getSize());
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

    private void attachJmeCanvasToFrame() {
        Constants.getGameCanvas().startCanvas();
        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        final Canvas canvas = getCanvas();
        // setLayout(null);
        add(canvas);
    }

    private Canvas getCanvas() {
        jmeContext = (JmeCanvasContext) Constants.getGameCanvas().getContext();
        jmeContext.setSystemListener(Constants.getGameCanvas());

        final Canvas canvas = jmeContext.getCanvas();

        // canvas.setLocation(0, -10);
        // canvas.setSize(1470, 760);
        return canvas;
    }
}
