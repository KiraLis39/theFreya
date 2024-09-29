package game.freya.gui;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
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

        jmeContext = (JmeCanvasContext) Constants.getGameCanvas().getContext();
        jmeContext.setSystemListener(Constants.getGameCanvas());
        final Canvas canvas = jmeContext.getCanvas();

        setTitle(settings.getTitle());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(settings.getMinWidth(), settings.getMinWidth()));
        setPreferredSize(new Dimension(settings.getWidth(), settings.getHeight() + 30));
        setResizable(settings.isResizable());
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
//        setLayout(null);

        Constants.getGameCanvas().startCanvas();
        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

//        canvas.setSize(getSize());
        add(canvas);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
