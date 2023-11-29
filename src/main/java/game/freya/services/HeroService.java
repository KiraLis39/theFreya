package game.freya.services;

import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.mappers.HeroMapper;
import game.freya.repositories.HeroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class HeroService {
    private final HeroRepository heroRepository;
    private final HeroMapper heroMapper;

    private HeroDTO currentHero;

    @Transactional
    public Hero save(Hero hero) {
        return heroRepository.save(hero);
    }

    @Modifying
    @Transactional
    public void deleteHeroByUuid(UUID heroUid) {
        heroRepository.deleteByUid(heroUid);
    }

    @Transactional(readOnly = true)
    public List<HeroDTO> findAllByWorldUuid(UUID uid) {
        return heroMapper.toDtos(heroRepository.findAllByWorldUid(uid));
    }

    public HeroDTO getCurrentHero() {
        return this.currentHero;
    }

    @Transactional
    public void saveCurrentHero(HeroDTO currentHero) {
        this.currentHero = heroMapper.toDto(heroRepository.save(heroMapper.toEntity(currentHero)));
    }

    public boolean isCurrentHero(HeroDTO hero) {
        return getCurrentHero().equals(hero);
    }

    @Transactional
    public void offlineHero() {
        this.currentHero.setOnline(false);
        this.currentHero = heroMapper.toDto(heroRepository.save(heroMapper.toEntity(this.currentHero)));
    }

    @Transactional(readOnly = true)
    public Optional<Hero> findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return heroRepository.findByHeroNameAndWorldUid(heroName, worldUid);
    }

    public Optional<Hero> findHeroByUuid(UUID uuid) {
        return heroRepository.findByUid(uuid);
    }
}
