package game.freya.entities.roots;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorColumn(name = "buff_type")
@Table(name = "buffs")
public class Buff {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false, unique = true)
    private UUID uid;

    @Getter
    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
