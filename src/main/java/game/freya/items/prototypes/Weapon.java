package game.freya.items.prototypes;

import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iWeapon;
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

import java.util.UUID;


@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "weapons")
public abstract class Weapon implements iGameObject, iWeapon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;
}
