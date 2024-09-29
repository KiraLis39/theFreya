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

    public GameWindowSwing(GameControllerService gameControllerService, AppSettings settings) {
        this.gameControllerService = gameControllerService;

        jmeContext = (JmeCanvasContext) Constants.getGameCanvas().getContext();
        jmeContext.setSystemListener(Constants.getGameCanvas());
        final Canvas canvas = jmeContext.getCanvas();

        setTitle(settings.getTitle());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(settings.getMinWidth(), settings.getMinWidth()));
        setPreferredSize(new Dimension(settings.getWidth(), settings.getHeight()));
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

        Constants.getGameCanvas().startCanvas();
        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        add(canvas);

        JPanel test = new JPanel(new BorderLayout()) {{
//            setDoubleBuffered(false);
//            setIgnoreRepaint(true);
//            setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));

            add(new JButton("Some Swing Component") {{
                setPreferredSize(new Dimension(120, 30));
                setLocation(200, 300);
            }});

            setLocation(200, 300);
            setSize(new Dimension(120, 30));
//            setBorder(new EmptyBorder(3, 3, 3, 3));
        }
            @Override
            public void paintComponent(Graphics g) {
//                g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
            }
        };

        getContentPane().add(new JLayeredPane() {{
            add(test, PALETTE_LAYER, 0);
        }});

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        canvas.setSize(getContentPane().getWidth(), getContentPane().getHeight());
    }
}
