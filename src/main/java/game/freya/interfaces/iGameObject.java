package game.freya.interfaces;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

/**
 * Базовый интерфейс, описывающий все игровые объекты.
 */
public interface iGameObject extends Serializable {
    UUID getUid();

    UUID getAuthor();

    String getName();

    Dimension getSize();

    Point2D.Double getLocation();

    Point2D.Double getCenterPoint();

    boolean isVisible();

    boolean hasCollision();

    String getImageNameInCache();

    void draw(Graphics2D g2D);

    Rectangle getCollider();

    boolean isInSector(Rectangle sector);
}
