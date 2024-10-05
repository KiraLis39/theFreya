package game.freya.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Controls {
    @Getter
    @Setter
    private static volatile boolean isGameActive = false;

    @Getter
    @Setter
    private static volatile boolean isOptionsMenuVisible = false;
}
