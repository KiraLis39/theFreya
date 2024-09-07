package game.freya.entities;

import game.freya.entities.roots.Environment;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@AllArgsConstructor
@Entity
@DiscriminatorValue("mock_with_storage_1")
public class MockEnvironmentWithStorage extends Environment {
}
