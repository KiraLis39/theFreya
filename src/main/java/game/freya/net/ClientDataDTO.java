package game.freya.net;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record ClientDataDTO(UUID puid) implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientDataDTO that = (ClientDataDTO) o;
        return Objects.equals(puid, that.puid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puid);
    }

    @Override
    public String toString() {
        return "ClientDataDTO{"
                + "puid=" + puid
                + '}';
    }
}
