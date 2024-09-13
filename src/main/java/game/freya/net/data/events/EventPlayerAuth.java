package game.freya.net.data.events;

import game.freya.enums.net.NetDataType;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
public record EventPlayerAuth(
        UUID dataUid,
        NetDataType dataType,
        UUID heroUid,
        UUID ownerUid,
        String playerName,
        String password,
        UUID createdBy,
        UUID worldUid
) implements iClientEventData {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventPlayerAuth that = (EventPlayerAuth) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
