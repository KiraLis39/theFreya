package game.freya.entities.roots;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "environment_type")
@Table(name = "environments")
public class Environment extends AbstractEntity {
}
