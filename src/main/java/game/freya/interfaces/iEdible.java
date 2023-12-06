package game.freya.interfaces;

/**
 * Интерфейс для съедобных айтемов
 */
public interface iEdible extends iStorable, iTradeable {

    void eat();

    void onRotting(); // при протухании

    boolean isPoisoned();

    int getHealthCompensation();

    int getOilCompensation();
}
