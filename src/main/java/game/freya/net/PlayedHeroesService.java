package game.freya.net;

import game.freya.entities.Hero;
import game.freya.entities.dto.HeroDTO;
import game.freya.mappers.HeroMapper;
import game.freya.net.data.ClientDataDTO;
import game.freya.services.HeroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedHeroesService {

    private static final Map<UUID, HeroDTO> heroes = HashMap.newHashMap(3);
    private final HeroService heroService;
    private final HeroMapper heroMapper;


    public HeroDTO getHero(ClientDataDTO data) {
        if (!heroes.containsKey(data.heroUuid())) {
            Hero otherHero = heroService.save(Hero.builder()
                    .uid(data.heroUuid())
                    .heroName(data.heroName())
                    .type(data.heroType())
                    .power(data.power())
                    .speed(data.speed())
                    .positionX(data.position().x)
                    .positionY(data.position().y)
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
            heroes.put(data.heroUuid(), heroMapper.toDto(otherHero));
        }

        return heroes.get(data.heroUuid());
    }

    public void addHero(HeroDTO heroDTO) {
        heroes.put(heroDTO.getUid(), heroDTO);
    }

    public HeroDTO getHeroByUid(UUID uuid) {
        return heroes.get(uuid);
    }

    public Set<HeroDTO> getHeroes() {
        return new HashSet<>(heroes.values());
    }
}
