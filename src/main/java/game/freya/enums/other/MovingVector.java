package game.freya.enums.other;

import lombok.Getter;

import java.util.Arrays;

/**
 * X = EAST\WEST; Y = NORTH\SOUTH; Z = INTER\OUTER
 */
@Getter
public enum MovingVector {
    UP(-1, 0, 0),
    UP_RIGHT(-1, 1, 0),
    RIGHT(0, 1, 0),
    RIGHT_DOWN(1, 1, 0),
    DOWN(1, 0, 0),
    DOWN_LEFT(1, -1, 0),
    LEFT(0, -1, 0),
    LEFT_UP(-1, -1, 0),
    NONE(0, 0, 0);

    private final int y, x, z;

    MovingVector(int y, int x, int z) {
        this.y = y;
        this.x = x;
        this.z = z;
    }

    public MovingVector reverse(MovingVector vector) {
        return switch (vector) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT_UP -> RIGHT_DOWN;
            case DOWN_LEFT -> UP_RIGHT;
            case RIGHT_DOWN -> LEFT_UP;
            case UP_RIGHT -> DOWN_LEFT;
            default -> vector;
        };
    }

    public MovingVector mod(MovingVector vector, int[] collisionMarker) {
        int ny = vector.y - collisionMarker[0];
        int nx = vector.x - collisionMarker[1];
        return Arrays.stream(values())
                .filter(v -> v.getY() == ny && v.getX() == nx).findFirst()
                .orElse(vector);
    }
}
