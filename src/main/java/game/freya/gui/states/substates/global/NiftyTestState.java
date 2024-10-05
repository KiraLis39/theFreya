package game.freya.gui.states.substates.global;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.screen.DefaultScreenController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NiftyTestState extends BaseAppState {
    private SimpleApplication app;
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;

    public NiftyTestState() {
        super(NiftyTestState.class.getSimpleName());
        setEnabled(false);
    }

    @Override
    public void initialize(Application app) {
        this.app = (SimpleApplication) app;

        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(this.app.getAssetManager(), this.app.getInputManager(),
                this.app.getAudioRenderer(), this.app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");

//            /** Read your XML and initialize your custom ScreenController */
//            nifty.fromXml("Interface/tutorial/step2/screen.xml", "start");
////            nifty.fromXml("Interface/helloworld.xml", "start", new MySettingsScreen());
////            nifty.addXml("Interface/mysecondscreen.xml");
//            nifty.addScreen("start", new ScreenBuilder("start") {{
//                controller(new MyStartScreen());
//            }}.build(nifty));

//            // nifty.fromXml("Interface/helloworld.xml", "start", new MySettingsScreen(data));
//            // attach the Nifty display to the gui view port as a processor
//            guiViewPort.addProcessor(niftyDisplay);

//        nifty.registerScreenController(this);
        app.getGuiViewPort().addProcessor(niftyDisplay);

//        nifty.addScreen("start", new ScreenBuilder("start") {{
//            controller(new DefaultScreenController());
//            layer(new LayerBuilder("Layer_ID") {{
//                childLayoutVertical();
//
//                panel(new PanelBuilder("Panel_ID") {{
////                    childLayoutCenter();
//                    childLayoutHorizontal();
//
//                    control(new ButtonBuilder("Button_ID", "Hello Nifty") {{
//                        alignCenter();
//                        valignCenter();
//                        height("5%");
//                        width("33%");
//                        interactOnClick("restartContext()");
//                    }});
//                    control(new ButtonBuilder("Button_ID2", "Hello Nifty 2") {{
//                        alignCenter();
//                        valignCenter();
//                        height("5%");
//                        width("34%");
//                    }});
//                    control(new ButtonBuilder("Button_ID3", "Hello Nifty 3") {{
//                        alignCenter();
//                        valignCenter();
//                        height("5%");
//                        width("33%");
//                    }});
//                }});
//            }});
//        }}.build(nifty));

        nifty.addScreen("start", new ScreenBuilder("start") {{
            controller(new DefaultScreenController());
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
//                backgroundColor("#000f");

                image(new ImageBuilder() {{
                    filename("Interface/background-new.png");
                }});
            }});

            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
//                backgroundColor("#0000");

                panel(new PanelBuilder("panel_top") {{
                    childLayoutCenter();
                    alignCenter();
//                    backgroundColor("#f008");
                    height("25%");
                    width("75%");

                    text(new TextBuilder() {{
                        text("My Cool Game");
                        font("Interface/Fonts/verdana-48-regular.fnt");
                        height("100%");
                        width("100%");
                    }});
                }});

                panel(new PanelBuilder("panel_mid") {{
                    childLayoutCenter();
                    alignCenter();
//                    backgroundColor("#0f08");
                    height("50%");
                    width("75%");

                    // add text
                    text(new TextBuilder() {{
                        text("Here goes some text describing the game and the rules and stuff. "
                                + "Incidentally, the text is quite long and needs to wrap at the end of lines. ");
                        font("Interface/Fonts/verdana-48-regular.fnt"); // Arial.fnt
                        wrap(true);
                        height("100%");
                        width("100%");
                    }});
                }});

                panel(new PanelBuilder("panel_bottom") {{
                    childLayoutHorizontal();
                    alignCenter();
//                    backgroundColor("#00f8");
                    height("25%");
                    width("75%");

                    panel(new PanelBuilder("panel_bottom_left") {{
                        childLayoutCenter();
                        valignCenter();
//                        backgroundColor("#44f8");
                        height("50%");
                        width("50%");

                        // add control
                        control(new ButtonBuilder("StartButton", "Start") {{
                            alignCenter();
                            valignCenter();
                            height("50%");
                            width("50%");
                        }});
                    }});

                    panel(new PanelBuilder("panel_bottom_right") {{
                        childLayoutCenter();
                        valignCenter();
//                        backgroundColor("#88f8");
                        height("50%");
                        width("50%");

                        // add control
                        control(new ButtonBuilder("QuitButton", "Quit") {{
                            alignCenter();
                            valignCenter();
                            height("50%");
                            width("50%");
                        }});
                    }});
                }});
            }});
        }}.build(nifty));
    }

    @Override
    protected void onEnable() {
        nifty.gotoScreen("start");

//        nifty.addScreen("hud", new ScreenBuilder("hud") {{
//            controller(new DefaultScreenController());
//
//            layer(new LayerBuilder("background") {{
//                childLayoutCenter();
////                backgroundColor("#000f");
//
//                // add image
//                image(new ImageBuilder() {{
//                    filename("Interface/hud-frame.png");
//                }});
//            }});
//
//            layer(new LayerBuilder("foreground") {{
//                childLayoutHorizontal();
////                backgroundColor("#0000");
//
//                // panel added
//                panel(new PanelBuilder("panel_left") {{
//                    childLayoutVertical();
////                    backgroundColor("#0f08");
//                    height("100%");
//                    width("80%");
//                    // <!-- spacer -->
//                }});
//
//                panel(new PanelBuilder("panel_right") {{
//                    childLayoutVertical();
////                    backgroundColor("#00f8");
//                    height("100%");
//                    width("20%");
//
//                    panel(new PanelBuilder("panel_top_right1") {{
//                        childLayoutCenter();
////                        backgroundColor("#00f8");
//                        height("15%");
//                        width("100%");
//
//                        control(new LabelBuilder(){{
//                            color("#000");
//                            text("123");
//                            width("100%");
//                            height("100%");
//                        }});
//                    }});
//
//                    panel(new PanelBuilder("panel_top_right2") {{
//                        childLayoutCenter();
////                        backgroundColor("#44f8");
//                        height("15%");
//                        width("100%");
//
//                        // add image
//                        image(new ImageBuilder() {{
//                            filename("Interface/face1.png");
//                            valignCenter();
//                            alignCenter();
//                            height("50%");
//                            width("30%");
//                        }});
//                    }});
//
//                    panel(new PanelBuilder("panel_bot_right") {{
//                        childLayoutCenter();
//                        valignCenter();
////                        backgroundColor("#88f8");
//                        height("70%");
//                        width("100%");
//                    }});
//                }}); // panel added
//            }});
//        }}.build(nifty));

//        nifty.gotoScreen("hud"); // start the screen

        // NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        //                getApplication().getAssetManager(),
        //                getApplication().getInputManager(),
        //                getApplication().getAudioRenderer(),
        //                getApplication().getGuiViewPort());
        //
        //        Nifty nifty = niftyDisplay.getNifty();
        //        getApplication().getGuiViewPort().addProcessor(niftyDisplay);
        //        ((SimpleApplication) getApplication()).getFlyByCamera().setDragToRotate(true);
        //
        //        nifty.loadStyleFile("nifty-default-styles.xml");
        //        nifty.loadControlFile("nifty-default-controls.xml");
        //
        //        nifty.registerMouseCursor("hand", "Interface/mouse-cursor-hand.png", 5, 4);
        ////        registerSound("mysound", "Interface/abc.wav");
        ////        registerMusic("mymusic", "Интерфейс/xyz.ogg");
        //
        //        // <!-- ... -->
        //
        //        nifty.gotoScreen("start"); // start the screen
        //
        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            controller(new DefaultScreenController());
        //            // <!-- ... -->
        //        }}.build(nifty));
        //
        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            controller(new DefaultScreenController());
        //            // <!-- ... -->
        //        }}.build(nifty));
        //
        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            controller(new DefaultScreenController());
        //            // layer added
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //
        //                // <!-- ... -->
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //
        //                // <!-- ... -->
        //            }});
        //            // layer added
        //
        //        }}.build(nifty));
        //
        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            controller(new DefaultScreenController());
        //            // layer added
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //
        //                // <!-- ... -->
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //
        //                // <!-- ... -->
        //            }});
        //            // layer added
        //
        //        }}.build(nifty));
        //
        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            controller(new DefaultScreenController());
        //            // layer added
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //
        //                // <!-- ... -->
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutVertical();
        //                backgroundColor("#0000");
        //
        //                // panel added
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
        //                }}); // panel added
        //            }});
        //            // layer added
        //
        //        }}.build(nifty));
        //
        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            controller(new DefaultScreenController());
        //
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //                // <!-- ... -->
        //            }});
        //
        //            layer(new LayerBuilder("foreground") {{
        //                childLayoutHorizontal();
        //                backgroundColor("#0000");
        //
        //                // panel added
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
        //
        //                    panel(new PanelBuilder("panel_top_right1") {{
        //                        childLayoutCenter();
        //                        backgroundColor("#00f8");
        //                        height("15%");
        //                        width("100%");
        //                    }});
        //
        //                    panel(new PanelBuilder("panel_top_right2") {{
        //                        childLayoutCenter();
        //                        backgroundColor("#44f8");
        //                        height("15%");
        //                        width("100%");
        //                    }});
        //
        //                    panel(new PanelBuilder("panel_bot_right") {{
        //                        childLayoutCenter();
        //                        valignCenter();
        //                        backgroundColor("#88f8");
        //                        height("70%");
        //                        width("100%");
        //                    }});
        //                }}); // panel added
        //            }});
        //        }}.build(nifty));
        //
        //        nifty.addScreen("start", new ScreenBuilder("start") {{
        //            controller(new DefaultScreenController());
        //            // layer added
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //
        //                // add image
        //                image(new ImageBuilder() {{
        //                    filename("Interface/start-background.png");
        //                }});
        //            }});
        //            // <!-- ... -->
        //        }}.build(nifty));
        //
        //        nifty.addScreen("hud", new ScreenBuilder("hud") {{
        //            controller(new DefaultScreenController());
        //
        //            layer(new LayerBuilder("background") {{
        //                childLayoutCenter();
        //                backgroundColor("#000f");
        //
        //                // add image
        //                image(new ImageBuilder() {{
        //                    filename("Interface/hud-frame.png");
        //                }});
        //            }});
        //            // <!-- ... -->
        //        }}.build(nifty));
        //
        //        panel(new PanelBuilder("panel_top_right2") {{
        //            childLayoutCenter();
        //            backgroundColor("#44f8");
        //            height("15%");
        //            width("100%");
        //
        //            // add image
        //            image(new ImageBuilder() {{
        //                filename("Interface/face1.png");
        //                valignCenter();
        //                alignCenter();
        //                height("50%");
        //                width("30%");
        //            }});
        //        }});
        //
        //        // panel added
        //        panel(new PanelBuilder("panel_top") {{
        //            childLayoutCenter();
        //            alignCenter();
        //            backgroundColor("#f008");
        //            height("25%");
        //            width("75%");
        //
        //            text(new TextBuilder() {{
        //                text("My Cool Game");
        //                font("Interface/Fonts/Default.fnt");
        //                height("100%");
        //                width("100%");
        //            }});
        //        }});
        //
        //        panel(new PanelBuilder("panel_mid") {{
        //            childLayoutCenter();
        //            alignCenter();
        //            backgroundColor("#0f08");
        //            height("50%");
        //            width("75%");
        //
        //            // add text
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
        //        nifty.loadStyleFile("nifty-default-styles.xml");
        //        nifty.loadControlFile("nifty-default-controls.xml");
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
        //
        //            // add control
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
        //
        //            // add control
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
        nifty.exit();
    }

    @Override
    protected void cleanup(Application app) {
        nifty.removeScreen("Screen_ID");
        app.getGuiViewPort().removeProcessor(niftyDisplay);
//        nifty.unregisterScreenController(this);
        niftyDisplay.cleanup();
    }
}
