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

    public Storable(UUID suid) {
        this.suid = suid;
    }

    @Override
    public void drop() {

    }

    @Override
    public short packSize() {
        return 0;
    }
}
