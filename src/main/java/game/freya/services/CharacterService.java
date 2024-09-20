package game.freya.services;

import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.entities.PlayCharacter;
import game.freya.mappers.CharacterMapper;
import game.freya.repositories.CharacterRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final CharacterMapper characterMapper;

    @Getter
    private PlayCharacterDto currentHero;

    public void setCurrentHero(PlayCharacterDto hero) {
        if (currentHero != null) {
            if (currentHero.equals(hero)) {
                currentHero.setOnline(true);
            } else if (currentHero.isOnline()) {
                // если online другой герой - снимаем:
                log.info("Снимаем он-лайн с Героя {} и передаём этот статус Герою {}...", currentHero.getName(), hero.getName());
                currentHero.setOnline(false);
                saveCurrent();
            }
        }
        log.info("Теперь активный Герой - {}", hero.getName());
        currentHero = hero;
    }

    @Modifying
    public void deleteByUuid(UUID characterUid) {
        characterRepository.deleteByUid(characterUid);
    }

    @Transactional(readOnly = true)
    public List<PlayCharacterDto> findAllByWorldUidAndOwnerUid(UUID uid, UUID ownerUid) {
        return characterMapper.toDtos(characterRepository.findAllByWorldUidAndOwnerUid(uid, ownerUid)).stream()
                .map(characterDto -> (PlayCharacterDto) characterDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<PlayCharacterDto> findAllByWorldUuid(UUID uid) {
        return characterMapper.toDto(characterRepository.findAllByWorldUid(uid)).stream()
                .map(characterDto -> (PlayCharacterDto) characterDto).collect(Collectors.toSet());
    }

    public PlayCharacterDto justSaveAnyHero(PlayCharacterDto heroDto) {
        PlayCharacterDto aim;
        Optional<PlayCharacter> old = characterRepository.findByUid(heroDto.getUid());
        if (old.isPresent()) {
            aim = (PlayCharacterDto) characterMapper.toDto(old.get());
            BeanUtils.copyProperties(heroDto, aim);
        } else {
            aim = heroDto;
        }
        log.info("Сохранение в БД героя {} ({})", aim.getUid(), aim.getName());
        return (PlayCharacterDto) characterMapper.toDto(characterRepository.save(characterMapper.toEntity(aim)));
    }

    public void saveCurrent() {
        characterMapper.toDto(characterRepository.save(characterMapper.toEntity(currentHero)));
    }

    @Transactional(readOnly = true)
    public PlayCharacterDto findHeroByNameAndWorld(String heroName, UUID worldUid) {
        return (PlayCharacterDto) characterRepository.findByNameAndWorldUid(heroName, worldUid).map(characterMapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<CharacterDto> getByUid(UUID uid) {
        return characterRepository.findByUid(uid).map(characterMapper::toDto);
    }

    public Optional<PlayCharacter> findByUid(UUID uuid) {
        return characterRepository.findByUid(uuid);
    }
}
