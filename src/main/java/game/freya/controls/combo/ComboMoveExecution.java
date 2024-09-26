package game.freya.controls.combo;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;

public class ComboMoveExecution {
    private static final float TIME_LIMIT = 0.3f;
    private int state;
    private float moveTime;
    private boolean finalState = false;

    @Getter
    private final ComboMove combo;

    @Getter
    private String debugString = ""; // for debug only

    public ComboMoveExecution(ComboMove combo) {
        this.combo = combo;
    }

    private boolean isStateSatisfied(HashSet<String> pressedMappings, float time, ComboAction.ComboState state) {
        if (state.timeElapsed() != -1f) {
            // check if an appropriate amount of time has passed if the state requires it
            if (moveTime + state.timeElapsed() >= time) {
                return false;
            }
        }
        for (String mapping : state.pressedMappings()) {
            if (!pressedMappings.contains(mapping)) {
                return false;
            }
        }
        for (String mapping : state.releasedMappings()) {
            if (pressedMappings.contains(mapping)) {
                return false;
            }
        }
        return true;
    }

    public void updateExpiration(float time) {
        if (!finalState && moveTime > 0 && moveTime + TIME_LIMIT < time) {
            state = 0;
            moveTime = 0;
            finalState = false;

            // reset debug string.
            debugString = "";
        }
    }

    /**
     * Check if move needs to be executed.
     *
     * @param pressedMappings Which mappings are currently pressed
     * @param time            Current time since start of app
     * @return True if move needs to be executed.
     */
    public boolean updateState(HashSet<String> pressedMappings, float time) {
        ComboAction.ComboState currentState = combo.getState(state);
        if (isStateSatisfied(pressedMappings, time, currentState)) {
            state++;
            moveTime = time;

            if (state >= combo.statesCount()) {
                finalState = false;
                state = 0;

                moveTime = time + 0.5f; // this is for the reset of the debug string only.
                debugString += ", -CASTING " + combo.getName().toUpperCase() + "-";
                return true;
            }

            // the following for debug only.
            if (currentState.pressedMappings().length > 0) {
                if (!debugString.isEmpty()) {
                    debugString += ", ";
                }
                debugString += Arrays.toString(currentState.pressedMappings()).replace(", ", "+");
            }

            if (combo.isFinalState() && state == combo.statesCount() - 1) {
                finalState = true;
            }
        }
        return false;
    }
}
