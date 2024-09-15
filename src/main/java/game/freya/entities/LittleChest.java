package game.freya.entities;

import game.freya.entities.roots.prototypes.Storage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorValue("chest_little")
public class LittleChest extends Storage {
}
