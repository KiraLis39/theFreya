package game.freya.net.data.events;

import game.freya.enums.player.MovingVector;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.UUID;

@Builder
public record EventHeroMoving(
        UUID dataUid,
        UUID ownerUid,
        String playerName,
        UUID heroUid,
        String heroName,
        Point2D.Double location,
        MovingVector vector,
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
        EventHeroMoving that = (EventHeroMoving) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
