package game.freya.entities;

import game.freya.entities.roots.Character;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("played")
public class PlayCharacter extends Character {
}
