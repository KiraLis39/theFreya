package game.freya.interfaces;

import game.freya.dto.roots.StorageDto;

import java.awt.geom.Point2D;

/**
 * Интерфейс складируемых айтемов
 */
public interface iStorable {
    int getStackCount(); // count in one cell (pack)

    void drop(Point2D.Double location); // drop this (to current location)

    boolean store(StorageDto storageDto); // put this to storage
}
