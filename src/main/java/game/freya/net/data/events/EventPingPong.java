package game.freya.net.data.events;

import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
public record EventPingPong(
        UUID dataUid,
        UUID heroUid,
        String heroName,
        UUID ownerUid,
        String playerName,
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
        EventPingPong that = (EventPingPong) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
