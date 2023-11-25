package game.freya.net;

import game.freya.entities.World;
import game.freya.enums.HeroType;
import game.freya.enums.HurtLevel;
import game.freya.enums.MovingVector;
import game.freya.enums.NetDataType;
import lombok.Builder;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
public record ClientDataDTO(
        UUID id,
        NetDataType type,
        UUID playerUid,
        String playerName,
        UUID heroUuid,
        String heroName,
        HeroType heroType,
        short level,
        float experience,
        short hp,
        HurtLevel hurtLevel,
        short maxHp,
        Point2D.Double position,
        MovingVector vector,
        byte speed,
        float power,
        boolean isOnline,
        String explanation,
        World world,
        int passwordHash,
        LocalDateTime createDate,
        LocalDateTime lastPlayDate
        // BufferedImage gameMap,
        // BufferedImage icon
) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientDataDTO that = (ClientDataDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
