package game.freya.gl;

import lombok.Builder;

@Builder
public class Collider3D extends Collider2D {

    private double x, y, z, xw, yw, h;

    @Override
    public double getHeight() {
        return h;
    }
}
