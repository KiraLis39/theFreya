package game.freya.net.data;

import game.freya.dto.PlayCharacterDto;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Builder
public record ClientDataDto(
        @NotNull UUID dataUid,
        @NotNull NetDataType dataType,
        @NotNull NetDataEvent dataEvent,
        @NotNull UUID heroUid,
        @NotNull String heroName,
        @NotNull String playerName,
        @NotNull UUID ownerUid,
        @NotNull UUID createdBy,
        iClientEventData content,
        long experience,
        float power,
//        Color baseColor,
//        Color secondColor,
//        HeroCorpusType corpusType,
//        HeroPeripheryType peripheryType,
//        short peripherySize,
//        HeroType heroType,
//        short level,
//        int hp,
//        int oil,
//        int maxHp,
//        int maxOil,
//        double positionX,
//        double positionY,
//        MovingVector vector,
//        byte speed,
//        String explanation,
//        UUID worldUid,
//        WorldDto world,
//        String password,
//        LocalDateTime createdDate,
//        LocalDateTime lastPlayDate,
//        String chatMessage,
//        List<BuffDto> buffs,
//        StorageDto inventory,
        Set<PlayCharacterDto> heroes
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
