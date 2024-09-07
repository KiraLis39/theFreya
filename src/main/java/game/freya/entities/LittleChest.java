package game.freya.entities;

import game.freya.entities.roots.Storage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@Entity
@DiscriminatorValue("chest_little")
public class LittleChest extends Storage {
    @Override
    public short capacity() {
        return 0;
    }
}
