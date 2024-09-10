package game.freya.entities.roots;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "weapon_type")
@Table(name = "weapons")
public class Weapon extends AbstractEntity {
}
