package game.freya.net.data.types;

import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
public record TypeChat(
        UUID dataUid,
        NetDataType dataType,
        NetDataEvent dataEvent,
        iClientEventData content,
        UUID ownerUid,
        String playerName,
        UUID heroUid,
        String heroName,
        UUID worldUid,
        String chatMessage
) implements iClientEventData {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeChat that = (TypeChat) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
