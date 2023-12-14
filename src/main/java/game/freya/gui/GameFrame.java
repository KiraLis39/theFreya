package game.freya.gui;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUSED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameFrame {
    private final Dimension monitorSize = Constants.MON.getConfiguration().getBounds().getSize();

    private final UIHandler uIHandler;

    private Dimension windowSize;

    private GameController gameController;

    private long window = -1, menu, game;

    private volatile boolean isGlWindowBreaked = false;

    private ScreenType currentScreen;

    public void appStart(GameController gameController) {
        this.gameController = gameController;

        double newWidth = monitorSize.getWidth() * 0.75d;
        double newHeight = newWidth / (monitorSize.getWidth() / monitorSize.getHeight());
        windowSize = new Dimension((int) newWidth, (int) newHeight);

        // ждём пока кончится показ лого:
        if (Constants.getLogo() != null && Constants.getLogo().getEngine().isAlive()) {
            try {
                log.info("Logo finished await...");
                Constants.getLogo().getEngine().join(3_000);
            } catch (InterruptedException ie) {
                log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
                Constants.getLogo().getEngine().interrupt();
            } finally {
                Constants.getLogo().finalLogo();
            }
        }

        init();
        loadScreen(ScreenType.MENU_SCREEN);
    }

    private void init() {
        log.info("LWJGL v." + Version.getVersion() + "!");

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }

    public void loadScreen(ScreenType screen) {
        this.currentScreen = screen;

        if (window != -1) {
            glfwSetWindowShouldClose(window, true);
            glfwFreeCallbacks(window);
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Setup an error callback. The default implementation will print the error message in System.err.
        try (GLFWErrorCallback callback = GLFWErrorCallback.createPrint(System.err)) {
            callback.set();
            resize(currentScreen);
        }

        loop(); // блокируется до закрытия окна.
    }

    private void resize(ScreenType screen) {
        if (window != -1) {
            glfwDestroyWindow(window);
        }

        if (Constants.getUserConfig().isFullscreen()) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        } else {
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
        }

        log.info("Try to load Menu screen...");
        // Create the window
        byte monitorIndex = 0;
        byte share = 0;
        window = glfwCreateWindow(800, 600,
                gameController.getGameConfig().getAppName().concat(" v.").concat(gameController.getGameConfig().getAppVersion()),
                monitorIndex, share);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW menu");
        } else if (screen.equals(ScreenType.MENU_SCREEN)) {
            menu = window;
        } else if (screen.equals(ScreenType.GAME_SCREEN)) {
            game = window;
        }

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null) {
                throw new RuntimeException("Failed to create the vidmode");
            }

            // Pos the window
            log.info("Fullscreen mode: {}", Constants.getUserConfig().isFullscreen());
            if (Constants.getUserConfig().isFullscreen()) {
                glfwSetWindowSize(window, videoMode.width(), videoMode.height());
                glfwSetWindowPos(window, 0, 32); // для отловли шапки окна при сломанном андекорейте.
            } else {
                glfwSetWindowSize(window, windowSize.width, windowSize.height);
                glfwSetWindowPos(window, 16, 32);
//                glfwSetWindowPos(window,
//                        (videoMode.width() - pWidth.get(0)) / 2,
//                        (videoMode.height() - pHeight.get(0)) / 2);
                log.info("videoMode: {}, mallocInt: {}", videoMode, stack.mallocInt(1));
                log.info("vm: {}x{}, pwh: {}x{}", videoMode.width(), videoMode.height(),
                        (videoMode.width() - pWidth.get(0)) / 2, (videoMode.height() - pHeight.get(0)) / 2);
            }

            // Make the OpenGL context current
            glfwMakeContextCurrent(window);

            // Enable v-sync
            glfwSwapInterval(1);

            // Make the window visible
            glfwShowWindow(window);
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        }

        setInAc();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        if (currentScreen.equals(ScreenType.MENU_SCREEN)) {
            glClearColor(0.0f, 0.0f, 1.0f, 0.5f);
        } else if (currentScreen.equals(ScreenType.GAME_SCREEN)) {
            glClearColor(0.0f, 1.0f, 0.0f, 0.5f);
        } else {
            glClearColor(1.0f, 0.0f, 0.0f, 0.5f);
        }

        // Run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key.
        while (!isGlWindowBreaked && !glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            if (window == menu) {
                MenuCanvas.render(window);
            } else if (window == game) {
                GameCanvas.render(window);
            } else {
                log.error("Нет требуемого окна для рендеринга {}", window);
            }

            glfwSwapBuffers(window); // swap the color buffers

            if (!isGlWindowBreaked) {
                // Poll for window events. The key callback above will only be invoked during this call.
                glfwPollEvents();
            }
        }
        if (isGlWindowBreaked) {
            exit();
        }
    }

    private void exit() {
        // Terminate GLFW and free the error callback
        glfwTerminate();

        SwingUtilities.invokeLater(() -> {
            gameController.saveCurrentWorld();
            gameController.exitTheGame(null);
        });
    }

    private void setInAc() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (windowVar, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                ) {
                    isGlWindowBreaked = true;
                    if (!glfwWindowShouldClose(window)) {
                        glfwSetWindowShouldClose(window, true); // Закрывает окно

                        // Free the window callbacks and destroy the window
                        glfwFreeCallbacks(window);
                        glfwDestroyWindow(window);
                    }
                }
            }

            if (key == GLFW_KEY_F1 && action == GLFW_RELEASE) {
                if (currentScreen.equals(ScreenType.MENU_SCREEN)) {
                    loadScreen(ScreenType.GAME_SCREEN);
                } else {
                    loadScreen(ScreenType.MENU_SCREEN);
                }
            }

            if (key == GLFW_KEY_F11 && action == GLFW_RELEASE) {
                Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
                loadScreen(currentScreen);
            }
        });

//        final String frameName = "mainFrame";
//
//        Constants.INPUT_ACTION.add(frameName, frame.getRootPane());
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFullscreen",
//                Constants.getUserConfig().getKeyFullscreen(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fullscreen mode...");
//                        Constants.getUserConfig().setFullscreen(!Constants.getUserConfig().isFullscreen());
//                        Constants.checkFullscreenMode(frame, windowSize);
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchPause",
//                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the pause mode...");
//                        Constants.setPaused(!Constants.isPaused());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchDebug",
//                Constants.getUserConfig().getKeyDebug(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the debug mode...");
//                        Constants.setDebugInfoVisible(!Constants.isDebugInfoVisible());
//                    }
//                });
//
//        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "switchFps",
//                Constants.getUserConfig().getKeyFps(), 0, new AbstractAction() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        log.debug("Try to switch the fps mode...");
//                        Constants.setFpsInfoVisible(!Constants.isFpsInfoVisible());
//                    }
//                });
    }

    public void windowIconified(WindowEvent e) {
        onGameHide();
    }

    public void windowDeiconified(WindowEvent e) {
        onGameRestore();
    }

    public void windowActivated(WindowEvent e) {
        onGameRestore();
    }

    public void windowDeactivated(WindowEvent e) {
        onGameHide();
    }

    public void windowStateChanged(WindowEvent e) {
        int oldState = e.getOldState();
        int newState = e.getNewState();

        switch (newState) {
            case 6 -> {
                log.info("Restored to fullscreen");
                if ((oldState == 1 || oldState == 7)) {
                    onGameRestore();
                }
            }
            case 0 -> {
                log.info("Switch to windowed");
                if ((oldState == 1 || oldState == 7)) {
                    onGameRestore();
                }
            }
            case 1, 7 -> onGameHide();
            default -> log.warn("MainMenu: Unhandled windows state: " + e.getNewState());
        }
    }

    private void onGameRestore() {
        if (Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
//            log.info("Auto resume the game on frame restore is temporary off.");
            Constants.setPaused(false);
            log.debug("Resume game...");
        }
    }

    private void onGameHide() {
        log.debug("Hide or minimized");
        if (!Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.debug("Paused...");
        }
    }
}
