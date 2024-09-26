package game.freya.controls;

public interface ManualControl extends MyControlInterface {
    void steerX(float value);

    void steerY(float value);

    void moveX(float value);

    void moveY(float value);

    void moveZ(float value);
}
