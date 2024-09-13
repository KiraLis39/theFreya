package game.freya.services;

import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.roots.Character;
import game.freya.mappers.CharMapper;
import game.freya.repositories.CharacterRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Transactional
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final CharMapper characterMapper;

    @Getter
    @Setter
    private PlayCharacterDto currentHero;

    @Modifying
    public void deleteByUuid(UUID characterUid) {
        characterRepository.deleteByUid(characterUid);
    }

    @Transactional(readOnly = true)
    public List<PlayCharacterDto> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUid) {
        return characterMapper.toPlayDto(characterRepository.findAllByWorldUidAndOwnerUid(uid, ownerUid));
    }

    @Transactional(readOnly = true)
    public List<CharacterDto> findAllByWorldUuid(UUID uid) {
        return characterMapper.toDto(characterRepository.findAllByWorldUid(uid));
    }

    public PlayCharacterDto justSaveAnyHero(PlayCharacterDto heroDto) {
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

    public PlayCharacterDto saveCurrent() {
        return (PlayCharacterDto) characterMapper.toDto(characterRepository.save(characterMapper.toEntity(currentHero)));
    }

    @Transactional(readOnly = true)
    public CharacterDto findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return characterRepository.findByNameAndWorldUid(heroName, worldUid).map(characterMapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isHeroExist(UUID uid) {
        return characterRepository.existsById(uid);
    }

    @Transactional(readOnly = true)
    public CharacterDto getByUid(UUID uid) {
//        Character found = characterRepository.getReferenceById(uid);
        Optional<Character> found2 = characterRepository.findByUid(uid);
        return found2.map(characterMapper::toDto).orElse(null);
    }

    public Optional<Character> findByUid(UUID uuid) {
        return characterRepository.findByUid(uuid);
    }
}
