package game.freya.gl;

import game.freya.enums.other.ScreenType;

public abstract class RenderScreen {
    public abstract void render(double w, double h);

    public abstract ScreenType getType();
}
