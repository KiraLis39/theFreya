package game.freya.interfaces;

/**
 * Интерфейс складируемых айтемов
 */
public interface iStorable extends iTradeable {
    void drop(); // drop this

    void store(iStorage inStorage); // put this to storage
}
