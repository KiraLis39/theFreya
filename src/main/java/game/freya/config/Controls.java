package game.freya.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Controls {
    @Getter
    @Setter
    private static volatile boolean isControlsMapped = false, isMovingKeyActive = false, isGameActive = false;

//    @Getter
//    private static volatile boolean isPaused = false;

    @Getter
    @Setter
    private static volatile boolean isMouseRightEdgeOver = false, isMouseLeftEdgeOver = false, isMouseUpEdgeOver = false, isMouseDownEdgeOver = false;

    @Getter
    @Setter
    private static volatile boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;

    @Getter
    @Setter
    private static volatile boolean isMinimapShowed = true;

    @Getter
    @Setter
    private static volatile boolean isMenuActive, initialized = false;

    @Getter
    @Setter
    private static volatile boolean firstButtonOver = false, secondButtonOver = false, thirdButtonOver = false, fourthButtonOver = false, exitButtonOver = false;

    @Getter
    @Setter
    private static volatile boolean revolatileNeeds = false, isOptionsMenuVisible = false;

    public static boolean isPlayerMoving() {
        return isPlayerMovingUp || isPlayerMovingDown || isPlayerMovingRight || isPlayerMovingLeft;
    }

//    public static void setPaused(boolean _isPaused) {
//        isPaused = _isPaused;
//        log.info("Paused: {}", isPaused);
//    }
}
