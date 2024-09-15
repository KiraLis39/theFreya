package game.freya.entities;

import game.freya.entities.roots.prototypes.Buff;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor
@Entity
@DiscriminatorValue("neg")
public class NegativeBuff extends Buff {
}
