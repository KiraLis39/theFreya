package game.freya.gui;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.Media;
import game.freya.config.UserConfig;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.GameCanvas;
import game.freya.gui.panes.MenuCanvas;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.awt.Dimension;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_AUTO_ICONIFY;
import static org.lwjgl.glfw.GLFW.GLFW_CENTER_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_NO_ERROR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUSED;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwPostEmptyEvent;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIconifyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMaximizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEventsTimeout;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameFrame {
    private final Dimension monitorSize = Constants.MON.getConfiguration().getBounds().getSize();

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

        log.info("LWJGL v." + Version.getVersion() + "!");
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        } else {
            final boolean DEBUG = Constants.isGLDebugMode();
            if (DEBUG) {
                // When we are in debug mode, enable all LWJGL debug flags
                Configuration.DEBUG.set(true);
                Configuration.DEBUG_FUNCTIONS.set(true);
                Configuration.DEBUG_LOADER.set(true);
                Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
                // Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.set(true);
                Configuration.DEBUG_STACK.set(true);
            } else {
                Configuration.DISABLE_CHECKS.set(true);
            }

            loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    public void loadScreen(ScreenType screen) {
        this.currentScreen = screen;

        if (window != -1) {
            glfwSetWindowShouldClose(window, true);
            glfwFreeCallbacks(window);
        }

        // Configure GLFW Hints:
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CENTER_CURSOR, GLFW_TRUE); // курсор по центру вновь созданных полноэкранных окон
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_TRUE); // будет ли полноэкранное окно автоматически иконизироваться и восстанавливать предыдущий видеорежим при потере фокуса ввода
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE); // будет ли окну предоставлен фокус ввода при вызове glfwShowWindow
        glfwWindowHint(GLFW_CONTEXT_NO_ERROR, GLFW_FALSE); // Если включен, ситуации, которые могли бы вызвать ошибки, вызывают неопределенное поведение
        glfwWindowHint(GLFW_SAMPLES, 0); // количество выборок, которые будут использоваться для мультисэмплинга
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE); // должен ли кадровый буфер иметь двойную буферизацию

//        GLFWImage.Buffer buff = new GLFWImage.Buffer(ByteBuffer.wrap(ImageIO.read().getData().))[2];
//        GLFWImage images = new GLFWImage(window)[2];
//        images[0] = load_icon("my_icon.png");
//        images[1] = load_icon("my_icon_small.png");
//        glfwSetWindowIcon(window, 2, images);

        // Setup an error callback. The default implementation will print the error message in System.err.
        try (GLFWErrorCallback callback = GLFWErrorCallback.createPrint(System.err)) {
            callback.set();
            resize(currentScreen);
        }

        Media.playSound("landing", 6d);
        loop(); // блокируется до закрытия окна.
    }

    private void resize(ScreenType screen) {
        if (window != -1) {
            glfwDestroyWindow(window);
        }

        if (Constants.getUserConfig().isFullscreen()) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
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
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the GLFW menu");
        } else if (screen.equals(ScreenType.MENU_SCREEN)) {
            menu = window;
        } else if (screen.equals(ScreenType.GAME_SCREEN)) {
            game = window;
        }

        // Минимальный и максимальный размер:
        // glfwSetWindowSizeLimits(window, windowSize.width, windowSize.height, windowSize.width, windowSize.height);

        // Соотношение сторон области содержимого окна оконного режима:
        // glfwSetWindowAspectRatio(window, 16, 10);

        GLFWVidMode mode = glfwGetVideoMode(window);
        if (mode != null) {
            glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate()); // определяет желаемую частоту обновления для полноэкранных окон
        } else {
            glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE); // GLFW_DONT_CARE = игнорировать
        }

        // Get the thread stack and push a new frame
//        try (MemoryStack stack = stackPush()) {
//            IntBuffer pWidth = stack.mallocInt(1); // int*
//            IntBuffer pHeight = stack.mallocInt(1); // int*

        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode == null) {
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the videoMode");
        }

        // Pos the window
        log.info("Fullscreen mode: {}", Constants.getUserConfig().isFullscreen());
        if (!Constants.getUserConfig().isFullscreen()) {
            log.info("Работаем в оконном режиме экрана...");
            glfwSetWindowSize(window, windowSize.width, windowSize.height);
            glfwSetWindowPos(window,
                    (videoMode.width() - windowSize.width) / 2,
                    (videoMode.height() - windowSize.height) / 2);
            // glfwRestoreWindow(window);
        } else if (Constants.getUserConfig().getFullscreenType().equals(UserConfig.FullscreenType.MAXIMIZE_WINDOW)) {
            log.info("Работаем в псевдо-полноэкране...");
            glfwMaximizeWindow(window);
        } else {
            log.info("Работаем в эксклюзивном режиме экрана...");
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
//        }

        // прозрачность окна:
//        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
//        if (glfwGetWindowAttrib(window, GLFW_TRANSPARENT_FRAMEBUFFER) != -1) {
//            log.info("кадровый буфер окна в данный момент прозрачен");
//        } else {
//            log.error("fail...");
//        }
//        glfwSetWindowOpacity(window, 0.7f);

        setInAc();
    }

    private void loop() {
        long lastUpdate = System.currentTimeMillis();
        int frames = 0;

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        if (currentScreen.equals(ScreenType.MENU_SCREEN)) {
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        } else if (currentScreen.equals(ScreenType.GAME_SCREEN)) {
            glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        } else {
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
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

            // fps:
            frames++;
            long time = System.currentTimeMillis();
            int UPDATE_EVERY = 3; // update FPS every seconds
            if (UPDATE_EVERY * 1000L <= time - lastUpdate) {
                lastUpdate = time;
                log.info("{} frames in {} seconds = {} fps", frames, UPDATE_EVERY, (frames / (float) UPDATE_EVERY));
                frames = 0;
            }

            if (!isGlWindowBreaked) {
                // Poll for window events. The key callback above will only be invoked during this call.
                glfwPollEvents();

                //  переводит вызывающий поток в спящий режим до тех пор, пока не будет получено хотя бы одно событие:
                // glfwWaitEvents();
                glfwWaitEventsTimeout(0.015625d);
                // Если основной поток спит в glfwWaitEvents, можете разбудить его из другого потока, отправив событие glfwPostEmptyEvent();
            }
        }
        if (isGlWindowBreaked) {
            try {
                glfwFreeCallbacks(window);
            } finally {
                exit();
            }
        }
    }

    private void exit() {
        Media.playSound("landing");

        // Terminate GLFW and free the error callback
        glfwTerminate();

        SwingUtilities.invokeLater(() -> {
            gameController.saveCurrentWorld();
            gameController.exitTheGame(null);
        });
    }

    private void setInAc() {
        // когда физическая клавиша нажата или отпущена или когда она повторяется:
        glfwSetKeyCallback(window, (windowVar, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                ) {
                    isGlWindowBreaked = true;
                    if (!glfwWindowShouldClose(window)) {
                        glfwPostEmptyEvent();
                        glfwSetWindowShouldClose(window, true); // Закрывает окно

                        // Free the window callbacks and destroy the window
                        glfwFreeCallbacks(window);
                        glfwDestroyWindow(window);
                    }
                } else {
                    glfwSetWindowShouldClose(window, false); // Не закрывает окно :)
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

            // установка курсора:
            // Если вы хотите реализовать управление камерой на основе движения мыши или другие схемы ввода, требующие неограниченного
            //  движения мыши, установите режим курсора на GLFW_CURSOR_DISABLED. Это скроет курсор и зафиксирует его в указанном окне:
            // glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            if (key == GLFW_KEY_F2 && action == GLFW_PRESS) {
                glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                glfwSetCursor(window, glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
            }
            if (key == GLFW_KEY_LEFT_ALT && action == GLFW_PRESS) {
                glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                glfwSetCursor(window, glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR));
            }
            if (key == GLFW_KEY_LEFT_ALT && action == GLFW_RELEASE) {
                glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                glfwSetCursor(window, NULL);
            }
        });

        // уведомление, когда курсор перемещается по окну:
        // glfwSetCursorPosCallback(window, курсор_позиция_callback);

        // уведомление, когда курсор входит или покидает область содержимого окна:
        // glfwSetCursorEnterCallback(window, курсор_enter_callback);

        // уведомления, когда пользователь прокручивает страницу, используя колесо мыши:
        // glfwSetScrollCallback(window, Scroll_callback);

        // получать пути к файлам и/или каталогам, помещенным в окно (Функция обратного вызова получает массив путей в кодировке UTF-8):
        // glfwSetDropCallback(окно, drop_callback);

        // при закрытии окна игры:
        glfwSetWindowCloseCallback(window, new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long l) {
                if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                        "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                ) {
                    isGlWindowBreaked = true;
                    if (!glfwWindowShouldClose(window)) {
                        glfwPostEmptyEvent();
                        glfwSetWindowShouldClose(window, true); // Закрывает окно

                        // Free the window callbacks and destroy the window
                        glfwFreeCallbacks(window);
                        glfwDestroyWindow(window);
                    }
                } else {
                    glfwSetWindowShouldClose(window, false); // Не закрывает окно :)
                }
            }
        });

        // при сворачивании:
        glfwSetWindowIconifyCallback(window, new GLFWWindowIconifyCallback() {
            @Override
            public void invoke(long l, boolean isIconify) {
                if (isIconify) {
                    onGameHide();
                } else {
                    onGameRestore();
                }
            }
        });

        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long l, int w, int h) {
                Media.playSound("landing");
                log.info("Размер окна был изменен на {}x{}", w, h);
            }
        });

        glfwSetWindowMaximizeCallback(window, new GLFWWindowMaximizeCallback() {
            @Override
            public void invoke(long l, boolean isMaximized) {
                Media.playSound("landing");
                log.info("Размер окна был {}", isMaximized ? "максимизирован." : "восстановлен.");
            }
        });

        // если игру надо приостанавливать во время обучения при всплывающих подсказках:
        glfwSetWindowFocusCallback(window, new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long win, boolean focusState) {
                Media.playSound("landing");
                log.info("Фокус был {} на окно {}", focusState ? "(1)." : "(2).", win);
            }
        });
    }

    private void onGameRestore() {
        if (!gameController.isCurrentWorldIsNetwork() && Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(false);
            log.debug("Resume game...");
        }
    }

    private void onGameHide() {
        log.debug("Hide or minimized");
        if (!gameController.isCurrentWorldIsNetwork() && !Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.debug("Paused...");
        }
    }

    /*
        * При уничтожении полноэкранного окна исходный видеорежим его монитора восстанавливается, но гамма-рампа остается нетронутой.

        * Если вы будете использовать Vulkan для рендеринга в окне, отключите создание контекста, установив для подсказки GLFW_CLIENT_API значение GLFW_NO_API
            https://www.glfw.org/docs/3.3/vulkan_guide.html

        * Каждый раз, когда вы опрашиваете состояние, вы рискуете пропустить желаемое изменение состояния. Если нажатая клавиша снова будет отпущена до того, как вы опросите ее состояние, вы пропустите нажатие клавиши. Рекомендуемое решение — использовать обратный вызов клавиши, но существует также режим ввода GLFW_STICKY_KEYS.

            glfwSetInputMode(окно, GLFW_STICKY_KEYS, GLFW_TRUE);
            Когда включен режим липких ключей, опрашиваемое состояние ключа будет оставаться GLFW_PRESS до тех пор, пока состояние этого ключа не будет опросено с помощью glfwGetKeyGLFW_RELEASE, в противном случае оно останется GLFW_PRESS.

            Если вы хотите знать, в каком состоянии находились клавиши Caps Lock и Num Lock при создании событий ввода, установите GLFW_LOCK_KEY_MODS режим ввода.

            glfwSetInputMode(окно, GLFW_LOCK_KEY_MODS, GLFW_TRUE);
            Когда этот режим ввода включен, любой обратный вызов, который получает биты-модификаторы, будет иметь GLFW_MOD_CAPS_LOCK< Бит /span> установлен. а>, если был включен Num Lock.GLFW_MOD_NUM_LOCK установлен, если в момент возникновения события был включен Caps Lock, и бит

            Константа GLFW_KEY_LAST содержит наибольшее значение любого токена ключа.

        * glfwIconifyWindow(окно); / glfwRestoreWindow(окно); = > иконизировать (т.е. свернуть) / восстанавливает окна после развертывания

        * Если хотите уведомить пользователя о событии, не прерывая его, вы можете запросить внимание с помощью glfwRequestWindowAttention(окно);
            Система подсветит указанное окно, а на платформах, где это не поддерживается, приложение в целом. Как только пользователь обратит на это внимание, система автоматически завершит запрос.

        * Узнать, находится ли курсор внутри области содержимого if (glfwGetWindowAttrib(окно, GLFW_HOVERED))

        * Ввод времени с высоким разрешением в секундах с помощью double секунд = glfwGetTime(); количество секунд с момента инициализации библиотеки с помощью glfwInit

        * Если системный буфер обмена содержит строку в кодировке UTF-8 или ее можно преобразовать в такую строку, вы
            можете получить ее с помощью glfwGetClipboardString text = glfwGetClipboardString(NULL);

        *
    */
}
