package game.freya.entities.dto.interfaces;

import game.freya.entities.dto.PlayerDTO;
import game.freya.gui.panes.GameCanvas;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface iWorld {

    void init(GameCanvas canvas);

    void addPlayer(PlayerDTO playerDTO);

    void removePlayer(PlayerDTO playerDTO);

    void draw(Graphics2D g2D, Rectangle visibleRect);
}
