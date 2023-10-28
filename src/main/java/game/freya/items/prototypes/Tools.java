package game.freya.items.prototypes;

import game.freya.items.interfaces.iTools;
import game.freya.items.interfaces.iUsable;
import lombok.Setter;

import java.util.UUID;

public abstract class Tools extends Storable implements iTools {

    private final short packSize = 32;
    @Setter
    private String name;
    private boolean isBroken;

    public Tools(UUID suid, String name) {
        super(suid, name);
    }

    @Override
    public void drop() {

    }

    @Override
    public short packSize() {
        return this.packSize;
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
                + ", name='" + name + '\''
                + "packSize=" + packSize
                + ", isBroken=" + isBroken
                + '}';
    }
}
