package game.freya;

import game.freya.config.GameConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class GameTest extends JFrame {
    private GameConfig config;
    private GameTest frame;

    public void open(GraphicsConfiguration gConf, GameConfig config) {
        frame = new GameTest(gConf);
        config = context.getBean("GameConfig", GameConfig.class);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(config.getGameTitle() + " v." + config.getGameVersion());

        setPreferredSize(new Dimension(1440, 1280));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
