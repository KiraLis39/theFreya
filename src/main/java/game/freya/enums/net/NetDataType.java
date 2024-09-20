package game.freya.enums.net;

import lombok.Getter;

@Getter
public enum NetDataType {
    AUTH_DATA(""),
    HERO_DATA(""),
    HERO_ACCEPTED(""),
    HERO_RESTRICTED(""),
    AUTH_SUCCESS(""),
    AUTH_DENIED(""),
    CHAT(""),
    EVENT(""),
    HERO_REMOTE_NEED("???");

    private final String description;

    NetDataType(String description) {
        this.description = description;
    }
}
