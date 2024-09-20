package game.freya.enums.net;

import lombok.Getter;

@Getter
public enum NetDataEvent {
    HERO_REGISTER(""),
    HERO_MOVING(""),
    HERO_OFFLINE(""),
    CLIENT_DIE(""),
    PING(""),
    PONG(""),
    WRONG_WORLD_PING(""),
    NONE("");

    private final String description;

    NetDataEvent(String description) {
        this.description = description;
    }
}
