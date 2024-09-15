package game.freya.entities;

import game.freya.entities.roots.World;
import game.freya.entities.roots.prototypes.Character;
import game.freya.enums.player.HeroType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorValue("played")
public class PlayCharacter extends Character {
    @Enumerated(EnumType.STRING)
    private HeroType type;

    @ManyToMany(mappedBy = "heroes", fetch = FetchType.LAZY)
    private Set<World> worlds;
}
