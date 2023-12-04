package game.freya.services;

import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.PlayerDTO;
import game.freya.enums.NetDataType;
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
        Optional<Hero> old = heroRepository.findByUid(heroDto.getHeroUid());
        if (old.isPresent()) {
            aim = heroMapper.toDto(old.get());
            BeanUtils.copyProperties(heroDto, aim);
        } else {
            aim = heroDto;
        }
        log.info("Сохранение в БД героя {} ({})", heroDto.getHeroUid(), heroDto.getHeroName());
        return heroMapper.toDto(heroRepository.save(heroMapper.toEntity(aim)));
    }

    @Transactional(readOnly = true)
    public HeroDTO findHeroByNameAndWorld(String heroName, UUID worldUid) {
        Optional<Hero> found = heroRepository.findByHeroNameAndWorldUid(heroName, worldUid);
        return found.map(heroMapper::toDto).orElse(null);
    }

    public boolean isHeroExist(UUID uuid) {
        return heroRepository.existsById(uuid);
    }

    @Transactional(readOnly = true)
    public HeroDTO getByUid(UUID uuid) {
        return heroMapper.toDto(heroRepository.getReferenceById(uuid));
    }

    public ClientDataDTO heroToCli(HeroDTO hero, PlayerDTO currentPlayer, NetDataType dataType) {
        return heroMapper.heroToCli(hero, currentPlayer, dataType);
    }

    public HeroDTO cliToHero(ClientDataDTO cli) {
        return heroMapper.cliToHero(cli);
    }
}
