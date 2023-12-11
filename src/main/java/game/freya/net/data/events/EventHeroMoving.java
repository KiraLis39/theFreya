package game.freya.net.data.events;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.enums.other.MovingVector;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.util.Objects;
import java.util.UUID;

@Builder
@HeroDataBuilder
public record EventHeroMoving(
        UUID dataUid,
        UUID playerUid,
        String playerName,
        UUID heroUid,
        String heroName,
        double positionX,
        double positionY,
        MovingVector vector,
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
