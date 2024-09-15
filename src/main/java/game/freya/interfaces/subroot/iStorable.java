package game.freya.interfaces.subroot;

import game.freya.dto.roots.BuffDto;
import game.freya.dto.roots.StorageDto;
import game.freya.enums.amunitions.RarityType;
import game.freya.interfaces.root.iDestroyable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

/**
 * Интерфейс складируемых айтемов
 */
public interface iStorable extends iDestroyable {
    float getWeight(); // вес

    int getStackCount(); // count in one cell (pack)

    void drop(Point2D.Double location); // drop this (to current location)

    void onStoreTo(StorageDto storageDto); // put this to storage

    RarityType getRarity(); // редкость предмета

    Set<iStorable> getSpareParts(); // детали на которые разбирается предмет

    Set<BuffDto> getBuffs(); // зачарования, бафы, доп.свойства

    Image getIcon(); // иконка в хранилище

    String getDescription(); // всплывающая подсказка
}
