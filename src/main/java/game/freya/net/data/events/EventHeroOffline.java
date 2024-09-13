package game.freya.net.data.events;

import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
public record EventHeroOffline(
        UUID dataUid,
        UUID ownerUid,
        String playerName,
        UUID heroUid,
        String heroName,
        String explanation,
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
        EventHeroOffline that = (EventHeroOffline) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
