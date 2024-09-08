package game.freya.interfaces;

import game.freya.entities.roots.Buff;
import game.freya.enums.amunitions.RarityType;

import java.util.Set;

/**
 * Интерфейс для инструментов
 */
public interface iTools extends iStorable {
    float getWeight(); // вес

    int getStrength(); // прочность

    short getRequiredLevel(); // необходимый уровень

    RarityType getRarity(); // редкость предмета

    Set<Buff> seeBuffs(); // зачарования, бафы, доп.свойства

    Set<iStorable> getSpareParts(); // детали на которые разбирается предмет
}
