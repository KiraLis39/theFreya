package game.freya.entities.dto.interfaces;

import game.freya.GameController;
import game.freya.gui.panes.GameCanvas;

import java.awt.Graphics2D;

public interface iWorld {

    void init(GameCanvas canvas, GameController controller);

    void draw(Graphics2D g2D);
}
