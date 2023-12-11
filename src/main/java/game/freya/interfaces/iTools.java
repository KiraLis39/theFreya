package game.freya.interfaces;

import game.freya.entities.logic.Buff;
import game.freya.enums.other.RarityType;

import java.util.Set;

/**
 * Интерфейс для инструментов
 */
public interface iTools extends iTradeable {
    float getWeight(); // вес

    int getStrength(); // прочность

    short getRequiredLevel(); // необходимый уровень

    RarityType getRarity(); // редкость предмета

    Set<Buff> seeBuffs(); // зачарования, бафы, доп.свойства

    Set<iStorable> getSpareParts(); // детали на которые разбирается предмет
}
