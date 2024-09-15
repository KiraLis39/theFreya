package game.freya.interfaces;

import game.freya.interfaces.root.iUsable;

/**
 * Интерфейс для съедобных айтемов
 */
public interface iEdible extends iTradeable, iUsable {
    void onRotting(); // при протухании

    boolean isPoisoned();

    int getHealthCompensation();

    int getOilCompensation();
}
