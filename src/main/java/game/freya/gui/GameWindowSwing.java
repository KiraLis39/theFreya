package game.freya.gui;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import game.freya.config.Constants;
import game.freya.services.GameControllerService;
import game.freya.states.substates.DebugInfoState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

        jmeContext = (JmeCanvasContext) Constants.getGameCanvas().getContext();
        jmeContext.setSystemListener(Constants.getGameCanvas());
        final Canvas canvas = jmeContext.getCanvas();

        setTitle(settings.getTitle());
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
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Constants.getGameCanvas()
                        .enqueue(() -> Constants.getGameCanvas().getStateManager().getState(DebugInfoState.class).rebuildFullText());
                log.info("Game resize...");
            }

            @Override
            public void componentShown(ComponentEvent e) {
                log.info("Game shown...");
                Constants.getGameCanvas().enqueue(() -> Constants.getGameCanvas().gainFocus());
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                log.info("Game hidden...");
                Constants.getGameCanvas().enqueue(() -> Constants.getGameCanvas().loseFocus());
            }
        });

        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                log.info("Game shown...");
                Constants.getGameCanvas().enqueue(() -> Constants.getGameCanvas().gainFocus());
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                log.info("Game hidden...");
                Constants.getGameCanvas().enqueue(() -> Constants.getGameCanvas().loseFocus());
            }
        });

        setBackground(Color.YELLOW);
        getRootPane().setBackground(Color.GREEN);
        getContentPane().setBackground(Color.BLACK);
        getLayeredPane().setBackground(Color.MAGENTA);
//        setLayout(null);

        Constants.getGameCanvas().startCanvas();
        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        add(canvas);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

//        canvas.setLocation(0, -10);
//        canvas.setSize(1470, 760);
        log.info("Canvas location/size: {} / {}", canvas.getLocation(), canvas.getSize());
    }
}
