package game.freya.net.data;

import game.freya.config.annotations.HeroDataBuilder;
import game.freya.entities.World;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.HeroCorpusType;
import game.freya.enums.HeroPeriferiaType;
import game.freya.enums.HeroType;
import game.freya.enums.MovingVector;
import game.freya.enums.NetDataEvent;
import game.freya.enums.NetDataType;
import lombok.Builder;

import java.awt.Color;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Builder
@HeroDataBuilder
public record ClientDataDTO(
        UUID id,
        NetDataType type,
        NetDataEvent event,
        UUID playerUid,
        String playerName,
        Color baseColor,
        Color secondColor,
        HeroCorpusType corpusType,
        HeroPeriferiaType periferiaType,
        short periferiaSize,
        UUID heroUuid,
        String heroName,
        HeroType heroType,
        short level,
        long experience,
        int hp,
        int oil,
        int maxHp,
        int maxOil,
        double positionX,
        double positionY,
        MovingVector vector,
        byte speed,
        float power,
        String explanation,
        UUID worldUid,
        World world,
        int passwordHash,
        LocalDateTime createDate,
        LocalDateTime lastPlayDate,
        String chatMessage,
        String buffsJson,
        String inventoryJson,
        Set<HeroDTO> heroes
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

    @Override
    public String toString() {
        return "ClientDataDTO{"
                + "type=" + type
                + ", playerName='" + playerName + '\''
                + ", heroName='" + heroName + '\''
                + '}';
    }
}
