package game.freya.gui.panes.handlers;

import lombok.extern.slf4j.Slf4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

@Slf4j
public final class UIHandler {

    private UIHandler() {
    }

    public static void drawUI(Rectangle canvasRect, Graphics2D g2D) {
        g2D.setStroke(new BasicStroke(2f));
        g2D.setColor(Color.GREEN);
        g2D.drawRect(1, 1, (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));

        g2D.setColor(Color.MAGENTA);
        g2D.drawRect((int) (canvasRect.getWidth() * 0.666f), 1,
                (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));
    }
}
