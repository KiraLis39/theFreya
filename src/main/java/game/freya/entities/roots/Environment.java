package game.freya.entities.roots;

import game.freya.entities.AbstractEntity;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorColumn(name = "environment_type")
@Table(name = "environments")
public class Environment extends AbstractEntity {
}
