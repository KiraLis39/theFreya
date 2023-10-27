package game.freya.items.interfaces;

/**
 * Интерфейс для предметов взаимодействия
 */
public interface iUsable {
    void use(); // use the object

    boolean isDestructible(); // is object can be destroyed

    void onDamage();
}
