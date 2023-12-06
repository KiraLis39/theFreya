package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;
import game.freya.gui.panes.GameCanvas;

import java.awt.Color;
import java.awt.Graphics2D;

public class Chat {
    private final GameCanvas canvas;

    public Chat(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void draw(Graphics2D v2D) {
        v2D.setColor(new Color(9, 28, 42, 220));
        v2D.drawRoundRect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1, 32, 32);

        v2D.setColor(Color.BLACK);
        v2D.setFont(Constants.DEBUG_FONT);
        v2D.drawString("Общий чат:", 6, 12);
        v2D.drawLine(0, 14, canvas.getWidth(), 14);
    }
}
