package game.freya.gl;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class Collider2D implements iCollider {

    private double x, y, xw, yw;

    public boolean intersects(Rectangle2D sector) {
        return sector.intersects(getFlatRectangle());
    }

    @Override
    public boolean intersects(Collider3D collider) {
        return getFlatRectangle().intersects(collider.getFlatRectangle());
    }

    public double getHeight() {
        return 0;
    }

    public Rectangle2D getFlatRectangle() {
        return new Rectangle2D.Double(x, y, xw, yw);
    }

    public Shape getShape() {
        return getFlatRectangle();
    }
}
