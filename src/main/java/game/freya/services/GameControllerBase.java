package game.freya.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Цель - разгрузить дочерний {@link GameControllerService}
 */
@Slf4j
@Getter
@Setter
public abstract class GameControllerBase {
    private Thread netDataTranslator;

    private volatile boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;

    public boolean isPlayerMoving() {
        return isPlayerMovingUp || isPlayerMovingDown || isPlayerMovingRight || isPlayerMovingLeft;
    }
}
