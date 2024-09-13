package game.freya.net.data;

import lombok.Builder;

import java.util.UUID;

@Builder
public record NetConnectTemplate(String address, String password, UUID worldUid) {
}
