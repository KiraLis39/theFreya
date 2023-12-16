package game.freya.utils;


public class GameUtils {
    private final double gravity = 9.800000190734863D;

    public static float getDistance(float x1, float x2, float y1, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

//    public static float getDistance(Vector2 point1, Vector2 point2) {
//        return (float) Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y));
//    }
}
