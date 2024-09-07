package game.freya.net.data.events;

import game.freya.entities.World;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
public record EventWorldData(
        UUID dataUid,
        UUID heroUid,
        UUID ownerUid,
        String playerName,
        UUID worldUid,
        World world
) implements iClientEventData {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventWorldData that = (EventWorldData) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
