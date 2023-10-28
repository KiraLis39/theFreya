package game.freya.gui;

import game.freya.GameStarter;
import game.freya.config.GameConfig;
import game.freya.engines.WorldsEngine;
import game.freya.players.Player;
import game.freya.worlds.World;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

@Slf4j
public class MainMenu extends JFrame implements WindowListener {
    private static final Dimension LAUNCHER_DIM = new Dimension(1440, 1050);
    private final transient GameConfig config;
    private final transient GameStarter starter;

    public MainMenu(GraphicsConfiguration gConf, GameConfig config, GameStarter starter) {
        super(config.getGameTitle() + " v." + config.getGameVersion(), gConf);
        this.config = config;
        this.starter = starter;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        WorldsEngine worldsEngine = new WorldsEngine();
        World demoWorld = worldsEngine.create("some_new", null);
        log.info("The world {} was created successfully!", demoWorld.getTitle());

        Player player01 = new Player("KiraLis39", "angelicalis39@mail.ru", null);
        demoWorld.addPlayer(player01);
        log.info("The world {} has players: {}", demoWorld.getTitle(), demoWorld.getPlayers());

        setPreferredSize(LAUNCHER_DIM);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        starter.closeConnections();
        starter.exitTheGame();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        log.warn("fail closing 2..");
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
