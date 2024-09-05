package game.freya.entities.logic;

import game.freya.entities.dto.HeroDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
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

    public abstract void activate(HeroDTO playerDTO);

    public abstract void deactivate(HeroDTO playerDTO);
}
