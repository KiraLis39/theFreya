package game.freya.items.interfaces;

/**
 * Интерфейс для складируемых айтемов
 */
public interface iStorable {
    void drop(); // drop the item

    short packSize(); // size of the pack of items
}
