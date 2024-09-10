package game.freya;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.VolatileImage;

@Slf4j
@Component
public class WorldEngine {
    private VolatileImage gameMapVImage;

    public VolatileImage getGameMap() {
        return gameMapVImage;
    }
}
