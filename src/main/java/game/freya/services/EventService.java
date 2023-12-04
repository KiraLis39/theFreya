package game.freya.services;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.NetDataEvent;
import game.freya.enums.NetDataType;
import game.freya.net.data.ClientDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@HeroDataBuilder
@RequiredArgsConstructor
public class EventService {
    public ClientDataDTO buildMove(HeroDTO dto) {
        return ClientDataDTO.builder()
                .type(NetDataType.EVENT)
                .event(NetDataEvent.HERO_MOVING)

                .heroUuid(dto.getHeroUid())
                .heroName(dto.getHeroName())
                .positionX(dto.getPosition().x)
                .positionY(dto.getPosition().y)
                .vector(dto.getVector())

                .build();
    }
}
