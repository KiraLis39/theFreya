package game.freya.services;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDTO;
import game.freya.net.data.events.EventHeroMoving;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@HeroDataBuilder
@RequiredArgsConstructor
public class EventService {
    private final PlayerService playerService;

    public ClientDataDTO buildMove(HeroDTO dto) {
        return ClientDataDTO.builder()
                .dataType(NetDataType.EVENT)
//                .heroUid(dto.getHeroUid())
//                .heroName(dto.getHeroName())
                .dataEvent(NetDataEvent.HERO_MOVING)
                .content(EventHeroMoving.builder()
                        .playerUid(dto.getAuthor())
                        .playerName(playerService.getCurrentPlayer().getNickName())

                        .heroUid(dto.getHeroUid())
                        .heroName(dto.getHeroName())
                        .positionX(dto.getLocation().x)
                        .positionY(dto.getLocation().y)
                        .vector(dto.getVector())
                        .build())
                .build();
    }
}
