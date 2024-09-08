package game.freya.services;

import game.freya.dto.roots.CharacterDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroMoving;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final PlayerService playerService;

    public ClientDataDto buildMove(CharacterDto dto) {
        return ClientDataDto.builder()
                .dataType(NetDataType.EVENT)
                .heroUid(dto.getUid())
                .heroName(dto.getName())
                .dataEvent(NetDataEvent.HERO_MOVING)
                .content(EventHeroMoving.builder()
                        .ownerUid(dto.getOwnerUid())
                        .playerName(playerService.getCurrentPlayer().getNickName())

                        .heroUid(dto.getUid())
                        .heroName(dto.getName())
                        .positionX(dto.getLocation().x)
                        .positionY(dto.getLocation().y)
                        .vector(dto.getVector())
                        .build())
                .build();
    }
}
