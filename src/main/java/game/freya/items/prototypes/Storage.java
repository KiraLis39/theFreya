package game.freya.items.prototypes;

import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iStorable;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
public abstract class Storage implements iGameObject, iStorable {
    @Getter
    private final String name;

    @Getter
    private final UUID uid;

    protected Storage(String name, UUID uid) {
        this.name = name;
        this.uid = uid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Storage storage = (Storage) o;
        return Objects.equals(getName(), storage.getName()) && Objects.equals(getUid(), storage.getUid());
    }
}
