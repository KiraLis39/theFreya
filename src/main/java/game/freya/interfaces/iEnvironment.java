package game.freya.interfaces;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

public interface iEnvironment extends iGameObject {
    Point2D.Double getCenterPoint();

    boolean isInSector(Rectangle sector);
}
