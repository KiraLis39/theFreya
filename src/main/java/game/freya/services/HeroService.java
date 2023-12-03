package game.freya.services;

import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.mappers.HeroMapper;
import game.freya.net.data.ClientDataDTO;
import game.freya.repositories.HeroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Modifying
    @Transactional
    public void deleteHeroByUuid(UUID heroUid) {
        heroRepository.deleteByUid(heroUid);
    }

    @Transactional(readOnly = true)
    public List<HeroDTO> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUid) {
        return heroMapper.toDtos(heroRepository.findAllByWorldUidAndOwnerUid(uid, ownerUid));
    }

    @Transactional(readOnly = true)
    public List<HeroDTO> findAllByWorldUuid(UUID uid) {
        return heroMapper.toDtos(heroRepository.findAllByWorldUid(uid));
    }

    @Transactional
    public HeroDTO saveHero(HeroDTO heroDto) {
        HeroDTO aim;
        Optional<Hero> old = heroRepository.findByUid(heroDto.getUid());
        if (old.isPresent()) {
            aim = heroMapper.toDto(old.get());
            BeanUtils.copyProperties(heroDto, aim);
        } else {
            aim = heroDto;
        }
        log.info("Сохранение в БД героя {} ({})", heroDto.getUid(), heroDto.getHeroName());
        return heroMapper.toDto(heroRepository.save(heroMapper.toEntity(aim)));
    }

    @Transactional(readOnly = true)
    public HeroDTO findHeroByNameAndWorld(String heroName, UUID worldUid) {
        Optional<Hero> found = heroRepository.findByHeroNameAndWorldUid(heroName, worldUid);
        return found.map(heroMapper::toDto).orElse(null);
    }

    public Hero save(ClientDataDTO data) {
        return heroRepository.save(Hero.builder()
                .uid(data.heroUuid())
                .heroName(data.heroName())
                .baseColor(data.baseColor())
                .secondColor(data.secondColor())
                .corpusType(data.corpusType())
                .periferiaType(data.periferiaType())
                .periferiaSize(data.periferiaSize())
                .type(data.heroType())
                .power(data.power())
                .speed(data.speed())
                .positionX(data.positionX())
                .positionY(data.positionY())
                .level(data.level())
                .experience(data.experience())
                .curHealth(data.hp())
                .maxHealth(data.maxHp())
                .hurtLevel(data.hurtLevel())
                .buffsJson(data.buffsJson())
                .inventoryJson(data.inventoryJson())
                // .inGameTime(data.inGameTime())
                .worldUid(data.worldUid())
                .ownerUid(data.playerUid())
                .lastPlayDate(data.lastPlayDate())
                .createDate(data.createDate())
                .isOnline(data.isOnline())
                .build());
    }

    public boolean isHeroExist(UUID uuid) {
        return heroRepository.existsById(uuid);
    }

    @Transactional(readOnly = true)
    public HeroDTO getByUid(UUID uuid) {
        return heroMapper.toDto(heroRepository.getReferenceById(uuid));
    }
}
