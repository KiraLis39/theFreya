package game.freya.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.HeroType;
import game.freya.enums.HurtLevel;
import game.freya.enums.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.HeroMapper;
import game.freya.net.data.ClientDataDTO;
import game.freya.services.HeroService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedHeroesService {
    private static final Map<UUID, HeroDTO> heroes = new HashMap<>();
    private final HeroService heroService;
    private final HeroMapper heroMapper;
    private final ObjectMapper mapper;

    @Getter
    private volatile UUID currentHeroUid;


    public HeroDTO getHero(ClientDataDTO data) {
        if (!heroes.containsKey(data.heroUuid())) {
            Hero otherHero = heroService.save(data);
            addHero(heroMapper.toDto(otherHero));
        }
        return heroes.get(data.heroUuid());
    }

    public void addHero(final HeroDTO heroDTO) {
        log.info("Добавляется в карту текущих игроков игрок {}...", heroDTO.getHeroName());
        heroes.put(heroDTO.getUid(), heroDTO);
    }

    public Collection<HeroDTO> getHeroes() {
        return heroes.values();
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

    public short getCurrentHeroMaxOil() {
        checkCHE();
        return heroes.get(currentHeroUid).getMaxOil();
    }

    public short getCurrentHeroCurOil() {
        checkCHE();
        return heroes.get(currentHeroUid).getCurOil();
    }

    public void offlineSaveAndRemoveCurrentHero(Duration gameDuration) {
        setCurrentHeroInGameTime(gameDuration == null ? 0 : gameDuration.toMillis());
        setCurrentHeroOnline(false);
        saveCurrentHeroToDB();
        heroes.remove(currentHeroUid);
        log.info("Удалён из карты игровых героев Герой {}", currentHeroUid);
        currentHeroUid = null;
    }

    private void saveCurrentHeroToDB() {
        checkCHE();
        heroService.saveHero(heroes.get(currentHeroUid));
    }

    public boolean isCurrentHero(HeroDTO hero) {
        return hero.getUid().equals(currentHeroUid);
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
        return heroes.get(currentHeroUid).getPosition();
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
        return heroes.get(currentHeroUid).getType();
    }

    public short getCurrentHeroLevel() {
        return heroes.get(currentHeroUid).getLevel();
    }

    public float getCurrentHeroExperience() {
        return heroes.get(currentHeroUid).getExperience();
    }

    public short getCurrentHeroCurHealth() {
        return heroes.get(currentHeroUid).getCurHealth();
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
        currentHeroUid = hero.getUid();
    }

    public float getCurrentHeroPower() {
        checkCHE();
        return heroes.get(currentHeroUid).getPower();
    }

    public short getCurrentHeroMaxHealth() {
        checkCHE();
        return heroes.get(currentHeroUid).getMaxHealth();
    }

    public boolean isCurrentHeroNotNull() {
        log.info("Проверка факта, что currentHero != NULL: currentHero = {}", currentHeroUid);
        return currentHeroUid != null;
    }
}
