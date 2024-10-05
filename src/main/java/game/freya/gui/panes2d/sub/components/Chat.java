package game.freya.gui.panes2d.sub.components;

import game.freya.config.Constants;
import lombok.Getter;
import lombok.Setter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

@Setter
@Getter
public class Chat {
    private Point location;

    private Dimension size;

    public Chat(Point location, Dimension size) {
        this.location = location;
        this.size = size;
    }

    public void draw(Graphics2D v2D) {
        AffineTransform tr = v2D.getTransform();
        v2D.translate(getLocation().x, getLocation().y);

        v2D.setColor(new Color(9, 28, 42, 127));
        v2D.fillRoundRect(0, 0, (int) getWidth(), (int) getHeight(), 6, 6);
        v2D.setStroke(new BasicStroke(2f));
        v2D.setColor(new Color(9, 28, 42, 255));
        v2D.drawRoundRect(0, 0, (int) getWidth(), (int) getHeight(), 6, 6);

        v2D.setColor(Color.BLACK);
        v2D.setFont(Constants.DEBUG_FONT);
        v2D.drawString("Общий чат:", 6, 14);
        v2D.setStroke(new BasicStroke(1f));
        v2D.drawLine(2, 18, (int) getWidth() - 4, 18);

        v2D.drawRoundRect(2, (int) (getHeight() - 35), (int) getWidth() - 4, 32, 3, 3);

        v2D.setTransform(tr);
    }

    private double getHeight() {
        return getSize().getHeight();
    }

    private double getWidth() {
        return getSize().getWidth();
    }

}
