package game.freya.services;

import game.freya.dto.PlayCharacterDto;
import game.freya.dto.PlayerDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.mappers.CharMapper;
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
    private final CharMapper characterMapper;

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
                        .location(dto.getLocation())
                        .vector(dto.getVector())
                        .build())
                .build();
    }

    public ClientDataDto heroToCli(CharacterDto hero, PlayerDto currentPlayer) {
        return characterMapper.heroToCli(hero, currentPlayer);
    }

    public PlayCharacterDto cliToHero(ClientDataDto cli) {
        return characterMapper.cliToHero(cli);
    }
}
