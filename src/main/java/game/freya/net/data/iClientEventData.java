package game.freya.net.data;

import java.io.Serializable;
import java.util.UUID;

public interface iClientEventData extends Serializable {

    UUID playerUid();

    UUID heroUid();
}
