package game.freya.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.HurtLevel;
import game.freya.enums.other.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.services.HeroService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Rectangle;
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
    private static final Map<UUID, HeroDTO> heroes = new HashMap<>();

    private final HeroService heroService;

    private final ObjectMapper mapper;

    @Getter
    private volatile UUID currentHeroUid;


    public HeroDTO getHero(UUID uid) {
        if (!heroes.containsKey(uid)) {
            HeroDTO dto;
            if (heroService.isHeroExist(uid)) {
                dto = heroService.getByUid(uid);
            } else {
                return null;
            }
            addHero(dto);
        }
        return heroes.get(uid);
    }

    public void addHero(final HeroDTO heroDTO) {
        if (heroDTO.getHeroUid() == null) {
            log.warn("Нельзя работать c героем без heroUid. Нужно найти причину и устранить!");
        }

        heroDTO.setOnline(true);

        if (heroes.containsKey(heroDTO.getHeroUid())) {
            log.debug("Обновление данных героя {} игрока {}...", heroDTO.getHeroName(), heroDTO.getAuthor());
            heroes.replace(heroDTO.getHeroUid(), heroDTO);
        } else {
            log.info("Добавляется в карту текущих игроков игрок {}...", heroDTO.getHeroName());
            heroes.put(heroDTO.getHeroUid(), heroDTO);
        }
    }

    public Set<HeroDTO> getHeroes() {
        return new HashSet<>(heroes.values());
    }

    public void clear() {
        log.info("Чистка currentHeroUid и массива heroes...");
        currentHeroUid = null;
        heroes.clear();
        log.warn("Список активных игровых героев обнулён.");
    }

    public String getCurrentHeroInventoryJson() throws JsonProcessingException {
        return mapper.writeValueAsString(heroes.get(currentHeroUid).getInventory());
    }

    public String getCurrentHeroBuffsJson() throws JsonProcessingException {
        return mapper.writeValueAsString(heroes.get(currentHeroUid).getBuffs());
    }

    public int getCurrentHeroMaxOil() {
        checkCHE();
        return heroes.get(currentHeroUid).getMaxOil();
    }

    public int getCurrentHeroCurOil() {
        checkCHE();
        return heroes.get(currentHeroUid).getOil();
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
        HeroDTO otherHero = heroes.values().stream().filter(h -> h.getAuthor().equals(playerUid)).findFirst().orElse(null);
        if (otherHero != null) {
            try {
                heroService.saveHero(otherHero);
                heroes.remove(otherHero.getHeroUid());
                log.info("Удалён из карты игровых героев Герой {} ({})", otherHero.getHeroName(), otherHero.getHeroUid());
            } catch (Exception e) {
                log.error("Что случилось? Выяснить: {}", ExceptionUtils.getFullExceptionMessage(e));
            }
        } else {
            log.warn("Героя {} в карте героев уже нет.", playerUid);
        }
    }

    private void saveCurrentHeroToDB() {
        checkCHE();
        heroService.saveHero(heroes.get(currentHeroUid));
    }

    public boolean isCurrentHero(HeroDTO hero) {
        return hero.getHeroUid().equals(currentHeroUid);
    }

    public HeroDTO getCurrentHero() {
        checkCHE();
        return heroes.get(currentHeroUid);
    }

    public MovingVector getCurrentHeroVector() {
        checkCHE();
        return heroes.get(currentHeroUid).getVector();
    }

    public void setCurrentHeroVector(MovingVector vector) {
        checkCHE();
        heroes.get(currentHeroUid).setVector(vector);
    }

    public Point2D.Double getCurrentHeroPosition() {
        checkCHE();
        return heroes.get(currentHeroUid).getLocation();
    }

    public long getCurrentHeroInGameTime() {
        checkCHE();
        return heroes.get(currentHeroUid).getInGameTime();
    }

    public void setCurrentHeroInGameTime(long gameDuration) {
        checkCHE();
        heroes.get(currentHeroUid).setInGameTime(gameDuration);
    }

    public byte getCurrentHeroSpeed() {
        return heroes.get(currentHeroUid).getSpeed();
    }

    public LocalDateTime getCurrentHeroCreateDate() {
        return heroes.get(currentHeroUid).getCreateDate();
    }

    public String getCurrentHeroName() {
        return heroes.get(currentHeroUid).getHeroName();
    }

    public HeroType getCurrentHeroType() {
        return heroes.get(currentHeroUid).getHeroType();
    }

    public short getCurrentHeroLevel() {
        return heroes.get(currentHeroUid).getLevel();
    }

    public long getCurrentHeroExperience() {
        return heroes.get(currentHeroUid).getExperience();
    }

    public int getCurrentHeroCurHealth() {
        return heroes.get(currentHeroUid).getHealth();
    }

    public HurtLevel getCurrentHeroHurtLevel() {
        return heroes.get(currentHeroUid).getHurtLevel();
    }

    public boolean isCurrentHeroOnline() {
        checkCHE();
        return heroes.get(currentHeroUid).isOnline();
    }

    public void setCurrentHeroOnline(boolean b) {
        checkCHE();
        heroes.get(currentHeroUid).setOnline(b);
    }

    private void checkCHE() {
        if (heroes.get(currentHeroUid) == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "currentHeroUid: " + currentHeroUid + ". heroes: " + heroes.values());
        }
    }

    public void addCurrentHero(HeroDTO hero) {
        hero.setOnline(true);
        hero.setLastPlayDate(LocalDateTime.now());
        addHero(hero);
        currentHeroUid = hero.getHeroUid();
    }

    public float getCurrentHeroPower() {
        checkCHE();
        return heroes.get(currentHeroUid).getPower();
    }

    public int getCurrentHeroMaxHealth() {
        checkCHE();
        return heroes.get(currentHeroUid).getMaxHealth();
    }

    public boolean isCurrentHeroNotNull() {
        return currentHeroUid != null && !getHeroes().isEmpty();
    }

    public Color getCurrentHeroBaseColor() {
        checkCHE();
        return heroes.get(currentHeroUid).getBaseColor();
    }

    public Color getCurrentHeroSecondColor() {
        checkCHE();
        return heroes.get(currentHeroUid).getSecondColor();
    }

    public HeroCorpusType getCurrentHeroCorpusType() {
        checkCHE();
        return heroes.get(currentHeroUid).getCorpusType();
    }

    public HeroPeriferiaType getCurrentHeroPeriferiaType() {
        checkCHE();
        return heroes.get(currentHeroUid).getPeriferiaType();
    }

    public short getCurrentHeroPeriferiaSize() {
        checkCHE();
        return heroes.get(currentHeroUid).getPeriferiaSize();
    }

    public HeroDTO getHeroByOwnerUid(UUID ouid) {
        return heroes.values().stream().filter(h -> h.getAuthor().equals(ouid)).findAny().orElse(null);
    }

    public Rectangle getCurrentHeroCollider() {
        checkCHE();
        return heroes.get(currentHeroUid).getCollider();
    }

    public Point2D getCurrentHeroCenterPoint() {
        checkCHE();
        return heroes.get(currentHeroUid).getCenterPoint();
    }
}
