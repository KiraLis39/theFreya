package game.freya.net.data;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import lombok.Builder;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Builder
@HeroDataBuilder
public record ClientDataDTO(
        UUID dataUid,
        NetDataType dataType,
        NetDataEvent dataEvent,
        iClientEventData content,
        UUID playerUid,
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
//        long experience,
//        int hp,
//        int oil,
//        int maxHp,
//        int maxOil,
//        double positionX,
//        double positionY,
//        MovingVector vector,
//        byte speed,
//        float power,
//        String explanation,
//        UUID worldUid,
//        World world,
//        int passwordHash,
//        LocalDateTime createDate,
//        LocalDateTime lastPlayDate,
//        String chatMessage,
//        String buffsJson,
//        String inventoryJson,
        Set<HeroDTO> heroes
) implements iClientEventData {

    public ClientDataDTO {
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
        ClientDataDTO that = (ClientDataDTO) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
