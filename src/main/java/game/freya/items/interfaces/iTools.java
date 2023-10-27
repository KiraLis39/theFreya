package game.freya.items.interfaces;

import game.freya.items.prototypes.Storage;

/**
 * Интерфейс для инструментов
 */
public interface iTools extends iStorable {
    void useOn(iUsable iUsable); // use the item on other object

    void onBreak();

    boolean isBroken();

    void repair();

    void destroy(Storage storage);
}
