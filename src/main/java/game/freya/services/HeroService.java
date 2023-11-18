package game.freya.services;

import game.freya.entities.Hero;
import game.freya.entities.Player;
import game.freya.repositories.HeroRepository;
import game.freya.repositories.PlayersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class HeroService {
    private final PlayersRepository playersRepository;
    private final HeroRepository heroRepository;

    @Transactional(readOnly = true)
    public Optional<Hero> findByUid(UUID heroId) {
        return heroRepository.findByUid(heroId);
    }

    public Hero save(Hero hero) {
        Player owner = playersRepository.findByUid(hero.getOwnedPlayer().getUid()).orElseThrow();
        hero.setOwnedPlayer(owner);
        return heroRepository.save(hero);
    }

    @Modifying
    public void deleteHeroByUuid(UUID heroUid) {
        heroRepository.deleteByUid(heroUid);
    }
}
