package game.freya.services;

import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.mappers.HeroMapper;
import game.freya.repositories.HeroRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class HeroService {
    private final PlayerService playerService;
    private final HeroRepository heroRepository;
    private final HeroMapper heroMapper;

    private final Set<HeroDTO> heroes = new HashSet<>();

    @Transactional(readOnly = true)
    public Optional<Hero> findByUid(UUID heroId) {
        return heroRepository.findByUid(heroId);
    }

    public Hero save(Hero hero) {
        return heroRepository.save(hero);
    }

    public HeroDTO save(HeroDTO hero) {
        return heroMapper.toDto(heroRepository.save(heroMapper.toEntity(hero)));
    }

    @Modifying
    public void deleteHeroByUuid(UUID heroUid) {
        heroRepository.deleteByUid(heroUid);
    }

    public List<HeroDTO> findAllByWorldUuid(UUID uid) {
        return heroMapper.toDtos(heroRepository.findAllByWorldUid(uid));
    }

    public HeroDTO getCurrentHero() {
        return heroes.stream()
                .filter(h -> h.getOwnerUid().equals(playerService.getCurrentPlayer().getUid()) && h.isOnline())
                .findAny().orElse(null);
    }

    public Set<HeroDTO> getCurrentHeroes() {
        return heroes;
    }

    public boolean isCurrentHero(HeroDTO hero) {
        return getCurrentHero().equals(hero);
    }

    public void saveCurrentHero() {
        heroRepository.save(heroMapper.toEntity(getCurrentHero()));
    }

    public void offlineHero() {
        HeroDTO cHero = getCurrentHero();
        cHero.setOnline(false);
        save(cHero);
    }
}
