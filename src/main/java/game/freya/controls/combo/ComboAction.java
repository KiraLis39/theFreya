package game.freya.controls.combo;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class ComboAction {
    @Getter
    private final String name;

    private final List<ComboState> states = new ArrayList<>();

    @Getter
    private transient String[] pressArr, releaseArr;

    @Getter
    private transient float timeElapsed;

    protected ComboAction(String name) {
        this.name = name;
    }

    public ComboAction pressed(String... keys) {
        pressArr = keys;
        return this;
    }

    public ComboAction released(String... keys) {
        releaseArr = keys;
        return this;
    }

    public ComboAction idle(float elapsed) {
        timeElapsed = elapsed;
        return this;
    }

    protected ComboState getState(int i) {
        return states.get(i);
    }

    public int statesCount() {
        return states.size();
    }

    public ComboAction done() {
        if (pressArr == null) {
            pressArr = new String[0];
        }

        if (releaseArr == null) {
            releaseArr = new String[0];
        }

        states.add(new ComboState(pressArr, releaseArr, timeElapsed));
        pressArr = null;
        releaseArr = null;
        timeElapsed = -1;
        return this;
    }

    @Builder
    public record ComboState(String[] pressedMappings, String[] releasedMappings, float timeElapsed) {
    }
}
