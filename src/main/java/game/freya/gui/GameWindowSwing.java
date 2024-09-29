package game.freya.gui;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import fox.components.layouts.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
        getLayeredPane().setBackground(Color.MAGENTA);
        setLayout(null);

        Constants.getGameCanvas().startCanvas();
        // ждём пока JME-окно не прогрузится:
        while (!Constants.getGameCanvas().isReady()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException _) {
            }
        }

        add(canvas, 0, 0);

        test = new JPanel() {{
//            setDoubleBuffered(false);
//            setIgnoreRepaint(true);
            setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
//            setOpaque(false);
//            setBackground(new Color(0.5f, 0.5f, 0.5f, 0.65f));

            add(new JButton("Some Swing Component") {{
                setPreferredSize(new Dimension(120, 30));
                setLocation(200, 300);
            }});

            setLocation(200, 300);
            setSize(new Dimension(300, 300));
            setBorder(new EmptyBorder(3, 3, 3, 3));
        }

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2D = (Graphics2D) g;
                try {
                    if (menuBack == null) {
                        menuBack = ImageIO.read(getClass().getResourceAsStream("/images/necessary/menu.png")); // menu_shadowed
                    }
                    g2D.drawImage(menuBack,
                            0, 0, 300, 300,
                            200, 300, menuBack.getWidth(), menuBack.getHeight(),
                            canvas);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        getContentPane().add(test, 100, 0);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        canvas.setSize(getContentPane().getWidth(), getContentPane().getHeight());
    }

    BufferedImage menuBack;

//    @Override
//    public void paintComponents(Graphics g) {
//        test.repaint();
//    }

//    @Override
//    public void paint(Graphics g) {
//        test.repaint();
//    }
}
