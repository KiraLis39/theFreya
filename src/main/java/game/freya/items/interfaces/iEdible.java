package game.freya.items.interfaces;

/**
 * Интерфейс для съедобных айтемов
 */
public interface iEdible extends iStorable {
    void eat(); // есть

    void onRotting(); // при протухании

    boolean isPoisoned(); // отравлено ли
}
