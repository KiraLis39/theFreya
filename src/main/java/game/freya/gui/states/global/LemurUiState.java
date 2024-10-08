package game.freya.gui.states.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;

public class LemurUiState extends BaseAppState {
    private SimpleApplication app;
    private Container panel;
    private Node guiNode;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();

        panel = new Container();
        panel.setLocalTranslation(300, 300, 0);
//        panel.setSize(new Vector3f(400, 100, 0));
        guiNode.attachChild(panel);

        setEnabled(false);
    }

    @Override
    protected void cleanup(Application application) {
        guiNode.detachChild(panel);
    }

    @Override
    protected void onDisable() {
        panel.clearChildren();
        guiNode.detachChild(panel);
    }

    @Override
    protected void onEnable() {
        Label label = panel.addChild(new Label("Hello, World!"));
        label.setInsets(new Insets3f(0, 0, 9, 0));

        Button button = panel.addChild(new Button("Click Me"));
        button.setPreferredSize(new Vector3f(200, 50, 0));
        button.addClickCommands(_ -> System.out.println("Кнопка нажата!"));

        panel.addChild(button);
        guiNode.attachChild(panel);
    }
}
