package game.freya.controls.combo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class ComboMove extends ComboAction {
    private float priority = 1;
    private float castTime = 0.8f;
    private boolean isFinalState = true;

    public ComboMove(String name) {
        super(name);
    }
}
