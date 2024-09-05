package game.freya.net.data.events;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeripheralType;
import game.freya.enums.other.HeroType;
import game.freya.enums.other.MovingVector;
import game.freya.net.data.iClientEventData;
import lombok.Builder;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

@Builder
@HeroDataBuilder
public record EventHeroRegister(
        UUID dataUid,
        UUID playerUid,
        String playerName,
        Color baseColor,
        Color secondColor,
        HeroCorpusType corpusType,
        HeroPeripheralType periferiaType,
        short periferiaSize,
        UUID heroUid,
        String heroName,
        HeroType heroType,
        short level,
        int hp,
        int oil,
        int maxHp,
        int maxOil,
        double positionX,
        double positionY,
        MovingVector vector,
        byte speed,
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
        EventHeroRegister that = (EventHeroRegister) o;
        return Objects.equals(dataUid, that.dataUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUid);
    }
}
