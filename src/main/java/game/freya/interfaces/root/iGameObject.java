package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.UUID;

/**
 * Базовый интерфейс, описывающий все игровые объекты.
 */
@RootInterface
public interface iGameObject extends Serializable {
    UUID getUid();

    UUID getCreatedBy();

    String getName();

    Dimension getSize();

    Point2D.Double getLocation();

    boolean isVisible();

    boolean hasCollision();

    String getCacheKey();

    void draw(Graphics2D g2D) throws AWTException;

    Rectangle2D.Double getCollider();

    Rectangle2D.Double getShape();

    boolean isInSector(Rectangle2D.Double sector);
}
