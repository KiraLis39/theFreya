package game.freya.interfaces;

/**
 * Интерфейс складируемых айтемов
 */
public interface iStorable extends iTradeable {
    void drop(); // drop the item

    void store(iStorage storage); // put item to storage
}
