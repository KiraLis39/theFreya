package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;

@RootInterface
public interface iBreakable extends iGameObject {
    int getDurability(); // прочность

    boolean isBroken();

    void onBreak();

    void repair();

    void onRepair();
}
