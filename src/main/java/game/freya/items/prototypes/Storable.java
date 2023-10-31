package game.freya.items.prototypes;

import game.freya.items.interfaces.iStorable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
public abstract class Storable implements iStorable {

    @Getter
    private final UUID uid;

    @Getter
    private final String name;

    @Getter
    @Setter
    private short packSize = 32;

    public Storable(String name) {
        this(UUID.randomUUID(), name);
    }

    public Storable(UUID uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    @Override
    public void drop() {

    }
}
