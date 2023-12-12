package game.freya.gui.panes.sub.components;

import game.freya.config.Constants;
import lombok.Getter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

public class Chat {
    @Getter
    private Point location;

    @Getter
    private Dimension size;

    public Chat(Point location, Dimension size) {
        this.location = location;
        this.size = size;
    }

    public void draw(Graphics2D v2D) {
        AffineTransform tr = v2D.getTransform();
        v2D.translate(getLocation().x, getLocation().y);

        v2D.setColor(new Color(9, 28, 42, 255));
        v2D.fillRoundRect(0, 0, (int) getWidth(), (int) getHeight(), 8, 8);
        v2D.setStroke(new BasicStroke(3f));
        v2D.setColor(new Color(9, 28, 42, 127));
        v2D.drawRoundRect(0, 0, (int) getWidth(), (int) getHeight(), 8, 8);

        v2D.setColor(Color.BLACK);
        v2D.setFont(Constants.DEBUG_FONT);
        v2D.drawString("Общий чат:", 6, 12);
        v2D.drawLine(0, 14, (int) getWidth(), 14);

        v2D.setTransform(tr);
    }

    private double getHeight() {
        return getSize().getHeight();
    }

    private double getWidth() {
        return getSize().getWidth();
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }
}
