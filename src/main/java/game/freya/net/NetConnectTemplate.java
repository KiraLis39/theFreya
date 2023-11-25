package game.freya.net;

import lombok.Builder;

import java.util.UUID;

@Builder
public record NetConnectTemplate(String address, int passwordHash, UUID worldUid) {

}
