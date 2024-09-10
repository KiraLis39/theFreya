package game.freya.net.data;

import game.freya.dto.roots.CharacterDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import lombok.Builder;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Builder
public record ClientDataDto(
        UUID dataUid,
        NetDataType dataType,
        NetDataEvent dataEvent,
        iClientEventData content,
        UUID ownerUid,
        String playerName,
//        Color baseColor,
//        Color secondColor,
//        HeroCorpusType corpusType,
//        HeroPeriferiaType periferiaType,
//        short periferiaSize,
        UUID heroUid,
        String heroName,
//        HeroType heroType,
//        short level,
        long experience,
//        int hp,
//        int oil,
//        int maxHp,
//        int maxOil,
//        double positionX,
//        double positionY,
//        MovingVector vector,
//        byte speed,
        float power,
//        String explanation,
//        UUID worldUid,
//        World world,
//        int passwordHash,
//        LocalDateTime createdDate,
//        LocalDateTime lastPlayDate,
//        String chatMessage,
//        String buffsJson,
//        String inventoryJson,
        Set<CharacterDto> heroes
) implements iClientEventData {
    public ClientDataDto {
        if (dataUid == null) {
            dataUid = UUID.randomUUID();
        }
        if (dataEvent == null) {
            dataEvent = NetDataEvent.NONE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientDataDto that = (ClientDataDto) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
