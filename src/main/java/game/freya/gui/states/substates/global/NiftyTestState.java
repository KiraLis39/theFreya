package game.freya.gui.states.substates.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import game.freya.config.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.swing.*;

@Slf4j
public class NiftyTestState extends BaseAppState implements ScreenController {
    private final String gameVersion;
    private SimpleApplication app;
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;

    public NiftyTestState(String gameVersion) {
        super(NiftyTestState.class.getSimpleName());
        setEnabled(false);
        this.gameVersion = gameVersion;
    }

    @Override
    public void initialize(Application app) {
        this.app = (SimpleApplication) app;

        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(this.app.getAssetManager(), this.app.getInputManager(),
                this.app.getAudioRenderer(), this.app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");

        /* Read your XML and initialize your custom ScreenController */
//        nifty.fromXml("Interface/tutorial/step2/screen.xml", "start");
//        nifty.fromXml("Interface/helloworld.xml", "start", new MySettingsScreen(data));
//        nifty.addXml("Interface/mysecondscreen.xml");

//        nifty.addScreen("start", new ScreenBuilder("start") {{
//            layer(new LayerBuilder("Layer_ID") {{
//                image(new ImageBuilder() {{
//                    filename("Interface/background-new.png");
//                }});
//            }});
//        }}.build(nifty));

        nifty.addScreen("start", new ScreenBuilder("start") {{
            controller(NiftyTestState.this);
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder("panel_top") {{
                    childLayoutCenter();
                    alignCenter();
                    // valignCenter();
                    height("25%");
                    width("75%");
                    text(new TextBuilder() {{
                        text("Freya The game v." + gameVersion);
                        font("Interface/Fonts/verdana-48-regular.fnt");
                        height("100%");
                        width("100%");
                    }});
                }});
                panel(new PanelBuilder("panel_mid") {{
                    childLayoutCenter();
                    alignCenter();
                    height("50%");
                    width("75%");
                    text(new TextBuilder() {{
                        text("""
                                Here goes some text describing the game and the rules and stuff. Incidentally,
                                the text is quite long and needs to wrap at the end of lines.""");
                        font("Interface/Fonts/verdana-48-regular.fnt"); // Arial.fnt
                        wrap(true);
                        height("100%");
                        width("100%");
                    }});
                }});
                panel(new PanelBuilder("panel_bottom") {{
                    childLayoutHorizontal();
                    alignCenter();
                    height("25%");
                    width("75%");
                    panel(new PanelBuilder("panel_bottom_left") {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        control(new ButtonBuilder("StartButton", "Start") {{
                            alignCenter();
                            valignCenter();
                            height("50%");
                            width("50%");
                            interactOnClick("gotoScreen(hud)");
                        }});
                    }});
                    panel(new PanelBuilder("panel_bottom_right") {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        control(new ButtonBuilder("QuitButton", "Quit") {{
                            alignCenter();
                            valignCenter();
                            height("50%");
                            width("50%");
                            interactOnClick("exitGame()");
                        }});
                    }});
                }});
            }});
        }}.build(nifty));

        nifty.addScreen("hud", new ScreenBuilder("hud") {{
            controller(NiftyTestState.this);
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
                backgroundColor("#000f");
//                image(new ImageBuilder() {{
//                    filename("Interface/overlay-credits-bottom.png");
//                }});
            }});

            layer(new LayerBuilder("foreground") {{
                childLayoutHorizontal();
                backgroundColor("#0000");
                panel(new PanelBuilder("panel_left") {{
                    childLayoutVertical();
                    backgroundColor("#0f08");
                    height("100%");
                    width("80%");
                    // <!-- spacer -->
                }});

                panel(new PanelBuilder("panel_right") {{
                    childLayoutVertical();
                    backgroundColor("#00f8");
                    height("100%");
                    width("20%");
                    panel(new PanelBuilder("panel_top_right1") {{
                        childLayoutCenter();
                        backgroundColor("#00f8");
                        height("15%");
                        width("100%");
                        control(new LabelBuilder() {{
                            color("#000");
                            text("123");
                            width("100%");
                            height("100%");
                        }});
                    }});

                    panel(new PanelBuilder("panel_top_right2") {{
                        childLayoutCenter();
                        backgroundColor("#44f8");
                        height("15%");
                        width("100%");
//                        image(new ImageBuilder() {{
//                            filename("Interface/nifty-logo.png");
//                            valignCenter();
//                            alignCenter();
//                            height("50%");
//                            width("30%");
//                        }});
                    }});

                    panel(new PanelBuilder("panel_bot_right") {{
                        childLayoutCenter();
                        valignCenter();
                        height("70%");
                        width("100%");
                    }});
                }});
            }});
        }}.build(nifty));
    }

    @Override
    protected void onEnable() {
        app.getGuiViewPort().addProcessor(niftyDisplay);
        nifty.gotoScreen("start");

//        nifty.gotoScreen("hud"); // start the screen
        //        nifty.registerMouseCursor("hand", "Interface/mouse-cursor-hand.png", 5, 4);
        //        registerSound("mysound", "Interface/abc.wav");
        //        registerMusic("mymusic", "Интерфейс/xyz.ogg");
        //
        //        nifty.gotoScreen("start"); // start the screen

        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //            }});
        //        }}.build(nifty));

        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //            }});
        //        }}.build(nifty));

        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //                panel(new PanelBuilder("panel_top") {{
        //                    childLayoutCenter();
        //                    alignCenter();
        //                    backgroundColor("#f008");
        //                    height("25%");
        //                    width("75%");
        //                }});
        //
        //                panel(new PanelBuilder("panel_mid") {{
        //                    childLayoutCenter();
        //                    alignCenter();
        //                    backgroundColor("#0f08");
        //                    height("50%");
        //                    width("75%");
        //                }});
        //
        //                panel(new PanelBuilder("panel_bottom") {{
        //                    childLayoutHorizontal();
        //                    alignCenter();
        //                    backgroundColor("#00f8");
        //                    height("25%");
        //                    width("75%");
        //
        //                    panel(new PanelBuilder("panel_bottom_left") {{
        //                        childLayoutCenter();
        //                        valignCenter();
        //                        backgroundColor("#44f8");
        //                        height("50%");
        //                        width("50%");
        //                    }});
        //
        //                    panel(new PanelBuilder("panel_bottom_right") {{
        //                        childLayoutCenter();
        //                        valignCenter();
        //                        backgroundColor("#88f8");
        //                        height("50%");
        //                        width("50%");
        //                    }});
        //                }});
        //            }});
        //        }}.build(nifty));

        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutHorizontal();
        //                backgroundColor("#0000");
        //                panel(new PanelBuilder("panel_left") {{
        //                    childLayoutVertical();
        //                    backgroundColor("#0f08");
        //                    height("100%");
        //                    width("80%");
        //                    // <!-- spacer -->
        //                }});
        //
        //                panel(new PanelBuilder("panel_right") {{
        //                    childLayoutVertical();
        //                    backgroundColor("#00f8");
        //                    height("100%");
        //                    width("20%");
        //                    panel(new PanelBuilder("panel_top_right1") {{
        //                        childLayoutCenter();
        //                        backgroundColor("#00f8");
        //                        height("15%");
        //                        width("100%");
        //                    }});
        //                    panel(new PanelBuilder("panel_top_right2") {{
        //                        childLayoutCenter();
        //                        backgroundColor("#44f8");
        //                        height("15%");
        //                        width("100%");
        //                    }});
        //                    panel(new PanelBuilder("panel_bot_right") {{
        //                        childLayoutCenter();
        //                        valignCenter();
        //                        backgroundColor("#88f8");
        //                        height("70%");
        //                        width("100%");
        //                    }});
        //                }});
        //            }});
        //        }}.build(nifty));

        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //                image(new ImageBuilder() {{
        //                    filename("Interface/start-background.png");
        //                }});
        //            }});
        //        }}.build(nifty));
        //
        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //                image(new ImageBuilder() {{
        //                    filename("Interface/hud-frame.png");
        //                }});
        //            }});
        //        }}.build(nifty));

        //        panel(new PanelBuilder("panel_top_right2") {{
        //            childLayoutCenter();
        //            backgroundColor("#44f8");
        //            height("15%");
        //            width("100%");
        //            image(new ImageBuilder() {{
        //                filename("Interface/face1.png");
        //                valignCenter();
        //                alignCenter();
        //                height("50%");
        //                width("30%");
        //            }});
        //        }});
        //        panel(new PanelBuilder("panel_top") {{
        //            childLayoutCenter();
        //            alignCenter();
        //            backgroundColor("#f008");
        //            height("25%");
        //            width("75%");
        //            text(new TextBuilder() {{
        //                text("My Cool Game");
        //                font("Interface/Fonts/Default.fnt");
        //                height("100%");
        //                width("100%");
        //            }});
        //        }});
        //        panel(new PanelBuilder("panel_mid") {{
        //            childLayoutCenter();
        //            alignCenter();
        //            backgroundColor("#0f08");
        //            height("50%");
        //            width("75%");
        //            text(new TextBuilder() {{
        //                text("Here goes some text describing the game and the rules and stuff. "
        //                        + "Incidentally, the text is quite long and needs to wrap at the end of lines.");
        //                font("Interface/Fonts/Default.fnt");
        //                wrap(true);
        //                height("100%");
        //                width("100%");
        //            }});
        //        }});
        //
        //        panel(new PanelBuilder("panel_top_right1") {{
        //            childLayoutCenter();
        //            backgroundColor("#00f8");
        //            height("15%");
        //            width("100%");
        //
        //            control(new LabelBuilder(){{
        //                color("#000");
        //                text("123");
        //                width("100%");
        //                height("100%");
        //            }});
        //        }});
        //
        //        panel(new PanelBuilder("panel_bottom_left") {{
        //            childLayoutCenter();
        //            valignCenter();
        //            backgroundColor("#44f8");
        //            height("50%");
        //            width("50%");
        //            control(new ButtonBuilder("StartButton", "Start") {{
        //                alignCenter();
        //                valignCenter();
        //                height("50%");
        //                width("50%");
        //            }});
        //        }});
        //
        //        panel(new PanelBuilder("panel_bottom_right") {{
        //            childLayoutCenter();
        //            valignCenter();
        //            backgroundColor("#88f8");
        //            height("50%");
        //            width("50%");
        //            control(new ButtonBuilder("QuitButton", "Quit") {{
        //                alignCenter();
        //                valignCenter();
        //                height("50%");
        //                width("50%");
        //            }});
        //        }});
    }

    @Override
    protected void onDisable() {
        app.getGuiViewPort().removeProcessor(niftyDisplay);
        nifty.exit();
    }

    @Override
    protected void cleanup(Application app) {
        nifty.removeScreen("Screen_ID");
        app.getGuiViewPort().removeProcessor(niftyDisplay);
        nifty.unregisterScreenController(this);
        niftyDisplay.cleanup();
    }

    @Override
    public void bind(@Nonnull Nifty nifty, @Nonnull Screen screen) {
        this.nifty = nifty;
    }

    public void gotoScreen(@Nonnull String screenId) {
        this.nifty.gotoScreen(screenId);
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }

    public void exitGame() {
        SwingUtilities.invokeLater(() -> Constants.getGameCanvas().requestClose(false));
    }
}
