package game.freya.net;

import game.freya.dto.roots.CharacterDTO;
import game.freya.enums.player.MovingVector;
import game.freya.services.HeroService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedHeroesService {
    private static final Map<UUID, CharacterDTO> heroes = new HashMap<>();

    private final HeroService heroService;

    @Getter
    private volatile UUID currentHeroUid;


    public CharacterDTO getHero(UUID uid) {
        if (!heroes.containsKey(uid)) {
            CharacterDTO dto;
            if (heroService.isHeroExist(uid)) {
                dto = heroService.getByUid(uid);
            } else {
                return null;
            }
            addHero(dto);
        }
        return heroes.get(uid);
    }

    public void addHero(final CharacterDTO heroDTO) {
        if (heroDTO.getUid() == null) {
            log.warn("Нельзя работать c героем без heroUid. Нужно найти причину и устранить!");
        }

        heroDTO.setOnline(true);

        if (heroes.containsKey(heroDTO.getUid())) {
            log.debug("Обновление данных героя {} игрока {}...", heroDTO.getName(), heroDTO.getOwnerUid());
            heroes.replace(heroDTO.getUid(), heroDTO);
        } else {
            log.info("Добавляется в карту текущих игроков игрок {}...", heroDTO.getName());
            heroes.put(heroDTO.getUid(), heroDTO);
        }
    }

    public Set<CharacterDTO> getHeroes() {
        return new HashSet<>(heroes.values());
    }

    public void clear() {
        log.info("Чистка currentHeroUid и массива heroes...");
        currentHeroUid = null;
        heroes.clear();
        log.warn("Список активных игровых героев обнулён.");
    }

    public void offlineSaveAndRemoveCurrentHero(Duration gameDuration) {
        setCurrentHeroInGameTime(gameDuration == null ? 0 : gameDuration.toMillis());
        setCurrentHeroOnline(false);
        log.info("Локальный герой теперь offline.");
        saveCurrentHeroToDB();
        heroes.remove(currentHeroUid);
        log.info("Локальный герой был сохранён и удалён из карты героев.");
        currentHeroUid = null;
    }

    public void offlineSaveAndRemoveOtherHeroByPlayerUid(UUID playerUid) {
        CharacterDTO otherHero = heroes.values().stream().filter(h -> h.getOwnerUid().equals(playerUid)).findFirst().orElse(null);
        if (otherHero != null) {
            try {
                heroService.saveHero(otherHero);
                heroes.remove(otherHero.getUid());
                log.info("Удалён из карты игровых героев Герой {} ({})", otherHero.getName(), otherHero.getUid());
            } catch (Exception e) {
                log.error("Что случилось? Выяснить: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } else {
            log.warn("Героя {} в карте героев уже нет.", playerUid);
        }
    }

    private void saveCurrentHeroToDB() {
        heroService.saveHero(heroes.get(currentHeroUid));
    }

    public boolean isCurrentHero(CharacterDTO hero) {
        return hero.getUid().equals(currentHeroUid);
    }

    public CharacterDTO getCurrentHero() {
        return heroes.get(currentHeroUid);
    }

    public MovingVector getCurrentHeroVector() {
        return heroes.get(currentHeroUid).getVector();
    }

    public void setCurrentHeroVector(MovingVector vector) {
        heroes.get(currentHeroUid).setVector(vector);
    }

    public Point2D.Double getCurrentHeroPosition() {
        return heroes.get(currentHeroUid).getLocation();
    }

    public long getCurrentHeroInGameTime() {
        return heroes.get(currentHeroUid).getInGameTime();
    }

    public void setCurrentHeroInGameTime(long gameDuration) {
        heroes.get(currentHeroUid).setInGameTime(gameDuration);
    }

    public byte getCurrentHeroSpeed() {
        return heroes.get(currentHeroUid).getSpeed();
    }

    public String getCurrentHeroName() {
        return heroes.get(currentHeroUid).getName();
    }

    public boolean isCurrentHeroOnline() {
        return heroes.get(currentHeroUid).isOnline();
    }

    public void setCurrentHeroOnline(boolean b) {
        heroes.get(currentHeroUid).setOnline(b);
    }

    public void addCurrentHero(CharacterDTO hero) {
        hero.setOnline(true);
        hero.setLastPlayDate(LocalDateTime.now());
        addHero(hero);
        currentHeroUid = hero.getUid();
    }

    public boolean isCurrentHeroNotNull() {
        return currentHeroUid != null && !getHeroes().isEmpty();
    }

    public CharacterDTO getHeroByOwnerUid(UUID ouid) {
        return heroes.values().stream().filter(h -> h.getOwnerUid().equals(ouid)).findAny().orElse(null);
    }

    public Rectangle getCurrentHeroCollider() {
        return heroes.get(currentHeroUid).getCollider();
    }
}
