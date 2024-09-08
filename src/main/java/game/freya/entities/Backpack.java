package game.freya.entities;

import game.freya.entities.roots.Storage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("backpack")
public class Backpack extends Storage {
}
