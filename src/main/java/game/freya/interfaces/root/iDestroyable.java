package game.freya.interfaces.root;

import game.freya.annotations.RootInterface;

@RootInterface
public interface iDestroyable extends iGameObject {
    boolean isDestroyed();

    void onDestroy();
}
