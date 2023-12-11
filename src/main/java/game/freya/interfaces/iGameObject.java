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

    UUID getCreator(); // UUID создателя данного объекта

    String getName();

    Point2D.Double getLocation();

    Dimension getSize();

    boolean isVisible();

    boolean hasCollision();

    String getImageNameInCache();

    void draw(Graphics2D g2D);

    Rectangle getCollider();
}
