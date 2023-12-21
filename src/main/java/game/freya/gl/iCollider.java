package game.freya.gl;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public interface iCollider {
    boolean intersects(Rectangle2D sector);

    boolean intersects(Collider3D collider);

    double getHeight();

    Rectangle2D getFlatRectangle();

    Shape getShape();
}
