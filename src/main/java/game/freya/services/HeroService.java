package game.freya.services;

import game.freya.dto.PlayerDTO;
import game.freya.dto.roots.CharacterDTO;
import game.freya.entities.roots.Character;
import game.freya.mappers.CharMapper;
import game.freya.net.data.ClientDataDTO;
import game.freya.repositories.CharacterRepository;
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
    private final CharacterRepository characterRepository;

    private final CharMapper heroMapper;

    @Modifying
    @Transactional
    public void deleteHeroByUuid(UUID heroUid) {
        characterRepository.deleteByUid(heroUid);
    }

    @Transactional(readOnly = true)
    public List<CharacterDTO> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUid) {
        return heroMapper.toDto(characterRepository.findAllByWorldUidAndOwnerUid(uid, ownerUid));
    }

    @Transactional(readOnly = true)
    public List<CharacterDTO> findAllByWorldUuid(UUID uid) {
        return heroMapper.toDto(characterRepository.findAllByWorldUid(uid));
    }

    @Transactional
    public CharacterDTO saveHero(CharacterDTO heroDto) {
        CharacterDTO aim;
        Optional<Character> old = characterRepository.findByUid(heroDto.getUid());
        if (old.isPresent()) {
            aim = heroMapper.toDto(old.get());
            BeanUtils.copyProperties(heroDto, aim);
        } else {
            aim = heroDto;
        }
        log.info("Сохранение в БД героя {} ({})", heroDto.getUid(), heroDto.getName());
        return heroMapper.toDto(characterRepository.save(heroMapper.toEntity(aim)));
    }

    @Transactional(readOnly = true)
    public CharacterDTO findHeroByNameAndWorld(String heroName, UUID worldUid) {
        Optional<Character> found = characterRepository.findByNameAndWorldUid(heroName, worldUid);
        return found.map(heroMapper::toDto).orElse(null);
    }

    public boolean isHeroExist(UUID uuid) {
        return characterRepository.existsById(uuid);
    }

    @Transactional(readOnly = true)
    public CharacterDTO getByUid(UUID uuid) {
        return heroMapper.toDto(characterRepository.getReferenceById(uuid));
    }

    public ClientDataDTO heroToCli(CharacterDTO hero, PlayerDTO currentPlayer) {
        return heroMapper.heroToCli(hero, currentPlayer);
    }

    public CharacterDTO cliToHero(ClientDataDTO cli) {
        return heroMapper.cliToHero(cli);
    }
}
