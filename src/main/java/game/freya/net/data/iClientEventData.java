package game.freya.net.data;

import java.io.Serializable;
import java.util.UUID;

public interface iClientEventData extends Serializable {

    UUID ownerUid();

    UUID heroUid();
}
