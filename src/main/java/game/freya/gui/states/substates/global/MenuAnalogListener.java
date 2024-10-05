package game.freya.gui.states.substates.global;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class MenuAnalogListener implements AnalogListener {
    @Getter
    private Point mousePoint;
    private SimpleApplication app;
    private Node parentNode;
    private Camera cam;
    private OptionsState optionsState;

    public MenuAnalogListener(SimpleApplication app, Node parentNode) {
        this.app = app;
        this.cam = app.getCamera();
        this.parentNode = parentNode;
        this.optionsState = this.app.getStateManager().getState(OptionsState.class);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case "MouseMoved" -> optionsState.checkPointer(app.getInputManager().getCursorPosition());

            case "MATTest" -> log.debug("Process [MouseMoveLeftTest]: %.3f".formatted(value));
            case "MoveForward" -> log.debug("Process [MoveForwardTest]: %.3f".formatted(value));
            case "MoveLeft" -> log.debug("Process [MoveLeftTest]: %.3f".formatted(value));
            case "MoveBack" -> log.debug("Process [MoveBackTest]: %.3f".formatted(value));
            case "MoveRight" -> log.debug("Process [MoveRightTest]: %.3f".formatted(value));

            case "Click" -> {
//                log.info("\nProcess [LMBTest]: %.3f".formatted(value));
                optionsState.checkClick();
//                brokenExample(tpf);
            }
            case null, default -> log.warn("Не релизован процесс [{}] ({})", name, value * tpf);
        }
    }

    private void brokenExample(float tpf) {
        // Reset results list.
        CollisionResults results = new CollisionResults();

        // Convert screen click to 3d position
        Vector3f click3d = cam.getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(app.getInputManager().getCursorPosition(), 1f).subtractLocal(click3d).normalizeLocal();

        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(click3d, dir);

        // Collect intersections between ray and all nodes in results list.
        parentNode.collideWith(ray, results);

        // (Print the results so we see what is going on:)
        for (int i = 0; i < results.size(); i++) {
            // (For each "hit", we know distance, impact point, geometry.)
            float dist = results.getCollision(i).getDistance();
            Vector3f pt = results.getCollision(i).getContactPoint();
            String target = results.getCollision(i).getGeometry().getName();
            log.info("Selection #{}: {} at {}, {} WU away.", i, target, pt, dist);
        }

        // Use the results -- we rotate the selected geometry.
        if (results.size() > 0) {
            // The closest result is the target that the player picked:
            Geometry target = results.getClosestCollision().getGeometry();
            // Here comes the action:
            if (target.getName().equals("Red Box")) {
                target.rotate(0, -tpf, 0);
            } else if (target.getName().equals("Blue Box")) {
                target.rotate(0, tpf, 0);
            } else {
                target.rotate(0, tpf * 2, 0);
            }
        }
    }
}
