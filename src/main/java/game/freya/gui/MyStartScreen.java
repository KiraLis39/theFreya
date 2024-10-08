package game.freya.gui;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class MyStartScreen extends BaseAppState { // implements ScreenController {

    @Override
    protected void initialize(Application app) {
        //It is technically safe to do all initialization and cleanup in the
        //onEnable()/onDisable() methods. Choosing to use initialize() and
        //cleanup() for this is a matter of performance specifics for the
        //implementor.
        // todo: initialize your AppState, e.g. attach spatials to rootNode

//        control(new ButtonBuilder("StartButton", "Start") {{
//            alignCenter();
//            valignCenter();
//            height("50%");
//            width("50%");
//            visibleToMouse(true);
//            interactOnClick("startGame(hud)");
//        }});
//
//        control(new ButtonBuilder("QuitButton", "Quit") {{
//            alignCenter();
//            valignCenter();
//            height("50%");
//            width("50%");
//            visibleToMouse(true);
//            interactOnClick("quitGame()");
//        }});
//
//        text(new TextBuilder() {{
//            text("${CALL.getPlayerName()}'s Cool Game");
//            font("Interface/Fonts/Default.fnt");
//            height("100%");
//            width("100%");
//        }});
    }

    /**
     * custom methods
     */
    public void startGame(String nextScreen) {
//        nifty.gotoScreen(nextScreen);  // switch to another screen
        // start the game and do some more stuff...
    }

    public void quitGame() {
        getApplication().stop();
    }

    @Override
    protected void cleanup(Application app) {
        // todo: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
    }

    //onEnable()/onDisable() can be used for managing things that should
    //only exist while the state is enabled. Prime examples would be scene
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        //Called when the state is fully enabled, ie: is attached and
        //isEnabled() is true or when the setEnabled() status changes after the
        //state is attached.
    }

    @Override
    protected void onDisable() {
        //Called when the state was previously enabled but is now disabled
        //either because setEnabled(false) was called or the state is being
        //cleaned up.
    }

    @Override
    public void update(float tpf) {
        // todo: implement behavior during runtime
    }

    /*
     * Bind this ScreenController to a screen. This happens right before the
     * onStartScreen STARTED and only exactly once for a screen!
     * @param nifty nifty
     * @param screen screen
     */
//    @Override
//    public void bind(Nifty nifty, Screen screen) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    /*
     * called right after the onStartScreen event ENDED.
     */
//    @Override
//    public void onStartScreen() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    /*
     * called right after the onEndScreen event ENDED.
     */
//    @Override
//    public void onEndScreen() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
}
