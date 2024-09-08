package game.freya.entities.roots;

import game.freya.entities.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "weapon_type")
@Table(name = "weapons")
public class Weapon extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;
}
