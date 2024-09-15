package game.freya.entities.roots.prototypes;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "buff_type")
@Table(name = "buffs")
public abstract class Buff {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @Getter
    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
