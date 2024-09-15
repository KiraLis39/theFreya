package game.freya.entities;

import game.freya.entities.roots.prototypes.Item;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorValue("food")
public class Food extends Item {
}
