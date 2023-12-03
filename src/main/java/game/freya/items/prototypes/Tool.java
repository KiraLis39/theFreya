package game.freya.items.prototypes;

import game.freya.items.interfaces.iTools;
import game.freya.items.interfaces.iUsable;
import lombok.Setter;

import java.util.UUID;

public abstract class Tool extends Storable implements iTools {

    @Setter
    private String name;

    private boolean isBroken;

    protected Tool(UUID suid, String name) {
        super(suid, name);
    }

    @Override
    public void useOn(iUsable iUsable) {

    }

    @Override
    public void onBreak() {
        this.isBroken = true;
    }

    @Override
    public boolean isBroken() {
        return this.isBroken;
    }

    @Override
    public void repair() {
        this.isBroken = false;
    }

    @Override
    public void destroy(Storage storage) {
        storage.get(this);
    }

    @Override
    public String toString() {
        return "Tools{"
                + ", name='" + getName() + '\''
                + "packSize=" + getPackSize()
                + ", isBroken=" + isBroken()
                + '}';
    }
}
