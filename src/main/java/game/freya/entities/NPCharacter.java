package game.freya.entities;

import game.freya.entities.roots.prototypes.Character;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("npc")
public class NPCharacter extends Character {
}
