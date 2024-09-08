package game.freya.services;

import game.freya.dto.PlayCharacterDto;
import game.freya.dto.PlayerDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.roots.Character;
import game.freya.mappers.CharMapper;
import game.freya.net.data.ClientDataDto;
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
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final CharMapper characterMapper;

    @Modifying
    @Transactional
    public void deleteHeroByUuid(UUID heroUid) {
        characterRepository.deleteByUid(heroUid);
    }

    @Transactional(readOnly = true)
    public List<CharacterDto> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUid) {
        return characterMapper.toDto(characterRepository.findAllByWorldUidAndOwnerUid(uid, ownerUid));
    }

    @Transactional(readOnly = true)
    public List<CharacterDto> findAllByWorldUuid(UUID uid) {
        return characterMapper.toDto(characterRepository.findAllByWorldUid(uid));
    }

    @Transactional
    public PlayCharacterDto saveHero(CharacterDto heroDto) {
        CharacterDto aim;
        Optional<Character> old = characterRepository.findByUid(heroDto.getUid());
        if (old.isPresent()) {
            aim = characterMapper.toDto(old.get());
            BeanUtils.copyProperties(heroDto, aim);
        } else {
            aim = heroDto;
        }
        log.info("Сохранение в БД героя {} ({})", aim.getUid(), aim.getName());
        return (PlayCharacterDto) characterMapper.toDto(characterRepository.save(characterMapper.toEntity(aim)));
    }

    @Transactional(readOnly = true)
    public CharacterDto findHeroByNameAndWorld(String heroName, UUID worldUid) {
        Optional<Character> found = characterRepository.findByNameAndWorldUid(heroName, worldUid);
        return found.map(characterMapper::toDto).orElse(null);
    }

    public boolean isHeroExist(UUID uid) {
        return characterRepository.existsById(uid);
    }

    @Transactional(readOnly = true)
    public CharacterDto getByUid(UUID uid) {
        return characterMapper.toDto(characterRepository.getReferenceById(uid));
    }

    public ClientDataDto heroToCli(CharacterDto hero, PlayerDto currentPlayer) {
        return characterMapper.heroToCli(hero, currentPlayer);
    }

    public CharacterDto cliToHero(ClientDataDto cli) {
        return characterMapper.cliToHero(cli);
    }
}
