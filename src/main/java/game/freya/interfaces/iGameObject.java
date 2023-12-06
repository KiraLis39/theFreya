package game.freya.interfaces;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Базовый интерфейс, описывающий все игровые объекты.
 */
public interface iGameObject {
    UUID getUid();
    UUID getCreator(); // UUID создателя данного объекта
    String getName();
    Point2D getLocation();
    Dimension getSize();
    boolean isVisible();
    boolean hasCollision();
    BufferedImage getImage();
    void draw(Graphics2D g2D);
}
