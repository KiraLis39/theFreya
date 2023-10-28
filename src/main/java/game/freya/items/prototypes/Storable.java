package game.freya.items.prototypes;

import game.freya.items.interfaces.iStorable;
import lombok.Getter;

import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
public abstract class Storable implements iStorable {

    @Getter
    private final UUID suid;

    @Getter
    private final String name;

    public Storable(String name) {
        this(UUID.randomUUID(), name);
    }

    public Storable(UUID suid, String name) {
        this.suid = suid;
        this.name = name;
    }

    @Override
    public void drop() {

    }

    @Override
    public short packSize() {
        return 0;
    }
}
