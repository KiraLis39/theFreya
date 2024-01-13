package game.freya.gui;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.Media;
import game.freya.enums.other.ScreenType;
import game.freya.gl.RenderScreen;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.Rectangle;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_AUTO_ICONIFY;
import static org.lwjgl.glfw.GLFW.GLFW_CENTER_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_NO_ERROR;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW;
import static org.lwjgl.glfw.GLFW.GLFW_RAW_MOUSE_MOTION;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEventsTimeout;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

@Slf4j
@Component
@RequiredArgsConstructor
public class WindowManager implements Runnable {
    private GameController gameController;

    private FoxWindow window;

    private RenderScreen currentScreen;

    private volatile boolean isGlWindowBreaked = false;

    public void appStart(GameController gameController) {
        this.gameController = gameController;

        Thread glThread = new Thread(this);
        glThread.setName("GLThread");
        glThread.start();
    }

    @Override
    public void run() {
        log.info("LWJGL v." + Version.getVersion() + "!");
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        if (Constants.getGameConfig().isGlDebugMode()) {
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

        // Configure GLFW Hints:
        doHintsPreset();

        // Set errors callback:
        GLFWErrorCallback.createPrint(System.err).set();

        // Create a Window:
        window = new FoxWindow(this, gameController);

//        if (Constants.getGlut() == null) {
//            log.info("Создание GLUT в потоке {}...", Thread.currentThread().getName());
//            Constants.setGlut(new GLUT());
//        }

        // Load the screen into window:
        loadScreen(ScreenType.MENU_LOADING_SCREEN);
    }

    private void doHintsPreset() {
        glfwDefaultWindowHints(); // optional, the current window hints are already the default

//        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        // будет ли полноэкранное окно автоматически иконизироваться и восстанавливать предыдущий видеорежим при потере фокуса ввода
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_TRUE);
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE); // будет ли окну предоставлен фокус ввода при вызове glfwShowWindow
        glfwWindowHint(GLFW_CENTER_CURSOR, GLFW_TRUE); // курсор по центру вновь созданных полноэкранных окон
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE); // должен ли кадровый буфер иметь двойную буферизацию
        glfwWindowHint(GLFW_SAMPLES, 4); // количество выборок, которые будут использоваться для мультисэмплинга
        glfwWindowHint(GLFW_CONTEXT_NO_ERROR, GLFW_FALSE); // Если включен, ситуации, которые могли бы вызвать ошибки, вызывают неопределенное поведение
    }

    public void loadScreen(ScreenType type) {
        if (type != null) {
            switch (type) {
                case MENU_LOADING_SCREEN -> {
                    // load textures:
                    if (Constants.getGameConfig().isUseTextures()) {
                        gameController.loadMenuTextures();
                    }

                    // ждём пока кончится показ лого:
                    logoEndsAwait();
                }
                case MENU_SCREEN -> {
                    // перевод курсора в режим меню:
                    Constants.setAltControlMode(true, window.getWindow());
                }
                case GAME_LOADING_SCREEN -> {
                    // load textures:
                    if (Constants.getGameConfig().isUseTextures()) {
                        gameController.loadGameTextures();
                    }
                }
                case GAME_SCREEN -> {
                    // перевод по-умолчанию в режим мыши:
                    Constants.setAltControlMode(false, window.getWindow());
                    glfwSetInputMode(window.getWindow(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
                    glfwSetCursor(window.getWindow(), glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));

                    gameController.setGameActive(true);
                    Constants.setGameStartedIn(System.currentTimeMillis());
                    Constants.setPaused(false);
                }
            }

            this.currentScreen = type.getScreen(this, gameController);
            window.setVisible(true);
            draw(); // блокируется до закрытия окна.
        }
    }

    private void draw() {
        long lastUpdate = System.currentTimeMillis();
        int frames = 0;

        // Начинаем цикл рисований:
        Media.playSound("landing");
        while (!isGlWindowBreaked && !glfwWindowShouldClose(window.getWindow())) {
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                window.configureThis();
                window.render(this.currentScreen);

                // swap the color buffers
                glfwSwapBuffers(window.getWindow());

                // draw fps:
                if (Constants.isFpsInfoVisible() && !isGlWindowBreaked) {
                    frames++;
                    drawFps();

                    long time = System.currentTimeMillis();
                    if (Constants.getFpsDelaySeconds() * 1000L <= time - lastUpdate) {
                        lastUpdate = time;
                        Constants.setRealFreshRate(frames / Constants.getFpsDelaySeconds());
//                        log.info("{} frames in {} seconds = {} fps", frames, Constants.getFpsDelaySeconds(), Constants.getRealFreshRate());
                        frames = 0;
                    }
                }

                // poll & wait:
                if (!isGlWindowBreaked) {
                    glfwPollEvents();

                    //  переводит вызывающий поток в спящий режим до тех пор, пока не будет получено хотя бы одно событие:
                    // glfwWaitEvents();
                    glfwWaitEventsTimeout(0.0078125d);
                    // Если основной поток спит в glfwWaitEvents, можете разбудить его из другого потока, отправив событие glfwPostEmptyEvent();
                }
            } catch (NullPointerException npe) {
                log.error("NPE here: {}", ExceptionUtils.getFullExceptionMessage(npe));
            } catch (IndexOutOfBoundsException ioub) {
                log.error("Index Out Of Bounds: {}", ExceptionUtils.getFullExceptionMessage(ioub));
                throw ioub;
            } catch (Exception e) {
                log.error("Some strange happened: {}", ExceptionUtils.getFullExceptionMessage(e));
                isGlWindowBreaked = true;
            }
        }

        // end of work:
        if (isGlWindowBreaked) {
            try {
                window.destroy();
            } catch (Exception e) {
                log.warn("Non-critical exception: {}", e.getMessage());
            } finally {
                exit();
            }
        }
    }

    private void drawFps() {
        // glEnable(GL_BLEND);
        // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // textRenderer.beginRendering(900, 50);
        // textRenderer.beginRendering((int) canvasWidth, (int) canvasHeight);

        // glBegin(GL_QUADS);

        // glTexCoord2f(-0.9f, 0.75f);
        // glTexCoord2f(-0.8f, 0.75f);
        // glTexCoord2f(-0.8f, 0.9f);
        // glTexCoord2f(-0.9f, 0.9f);

        // glEnd();
        // glDisable(GL_BLEND);

        // textRenderer.setColor(Color.RED);
        // textRenderer.setColor(0.8f, 1f, 0.2f, 1.0f);
        // textRenderer.draw(String.valueOf(fps), 5, 20);
        // textRenderer.endRendering();
        // textRenderer.flush();
    }

    public void showConfirmExitRequest() {
        if (isMenuScreen()) {
            if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                    "Выйти на рабочий стол без сохранения?", FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
            ) {
                isGlWindowBreaked = true;
                if (!glfwWindowShouldClose(window.getWindow())) {
                    destroy();
                }
            } else {
                glfwSetWindowShouldClose(window.getWindow(), false); // Не закрывает окно :)
            }
        } else if (isGameScreen()) {
//            game.onExitBack();
        } else {
            log.error("WTF: {}", window.getWindow());
        }
    }

    public void stopGame() {
        if (gameController.isGameActive()) {
            if (gameController.getCurrentWorld() != null) {
                boolean paused = Constants.isPaused();
                boolean debug = Constants.isDebugInfoVisible();

                Constants.setPaused(false);
                Constants.setDebugInfoVisible(false);
                gameController.doScreenShot(new Point(0, 0), new Rectangle(0, 0, 400, 400));
                Constants.setPaused(paused);
                Constants.setDebugInfoVisible(debug);
            }

            // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
            if (gameController.isGameActive()) {
                gameController.setGameActive(false);

                gameController.saveCurrentWorld();

                if (Constants.getDuration() != null && gameController.getPlayedHeroesService().isCurrentHeroNotNull()) {
                    gameController.setCurrentHeroOfflineAndSave(Constants.getDuration());
                }

                // если игра сетевая и локальная - останавливаем сервер при выходе из игры:
                if (gameController.isCurrentWorldIsNetwork()) {
                    if (gameController.isCurrentWorldIsLocal()) {
                        if (gameController.closeServer()) {
                            log.info("Сервер успешно остановлен");
                        } else {
                            log.warn("Возникла ошибка при закрытии сервера.");
                        }
                    }
                    if (gameController.isSocketIsOpen()) {
                        gameController.closeSocket();
                    }
                }

                loadScreen(ScreenType.MENU_SCREEN);
            }
        }
    }

    public void destroy() {
        window.destroy();
        exit();
    }

    private void exit() {
        Media.playSound("jump");

        GL.setCapabilities(null);

        // Terminate GLFW and free the error callback
        glfwFreeCallbacks(window.getWindow());
        //glfwDestroyWindow(window.getWindow());
        glfwTerminate();

        SwingUtilities.invokeLater(() -> {
            gameController.saveCurrentWorld();
            gameController.exitTheGame(null);
        });

        GLFWErrorCallback res = glfwSetErrorCallback(null);
        if (res != null) {
            res.free();
        }
    }

    private void logoEndsAwait() {
        if (Constants.getLogo() != null && Constants.getLogo().isAlive()) {
            try {
                log.info("Logo finished await...");
                Constants.getLogo().join(15_000);
            } catch (InterruptedException ie) {
                log.warn("Logo thread joining was interrupted: {}", ExceptionUtils.getFullExceptionMessage(ie));
                Constants.getLogo().getEngine().interrupt();
            } finally {
                Constants.getLogo().finalLogo();
            }
        }
    }

    public boolean isMenuLoadingScreen() {
        return currentScreen.getType().equals(ScreenType.MENU_LOADING_SCREEN);
    }

    public boolean isMenuScreen() {
        return currentScreen.getType().equals(ScreenType.MENU_SCREEN);
    }

    public boolean isGameLoadingScreen() {
        return currentScreen.getType().equals(ScreenType.GAME_LOADING_SCREEN);
    }

    public boolean isGameScreen() {
        return currentScreen.getType().equals(ScreenType.GAME_SCREEN);
    }

    public double getWindowAspect() {
        return window.getAspect();
    }

    public boolean isCameraMovingForward() {
        return window.isCameraMovingForward();
    }

    public boolean isCameraMovingBack() {
        return window.isCameraMovingBack();
    }

    public boolean isCameraMovingLeft() {
        return window.isCameraMovingLeft();
    }

    public boolean isCameraMovingRight() {
        return window.isCameraMovingRight();
    }

    public Rectangle getViewPortBounds() {
        return new Rectangle();
    }

    public void dragDown(float speed) {
        window.dragDown(speed);
    }

    public void dragRight(float speed) {
        window.dragRight(speed);
    }

    public void dragUp(float speed) {
        window.dragUp(speed);
    }

    public void dragLeft(float speed) {
        window.dragLeft(speed);
    }

    public void setConnectionAwait(boolean b) {
        window.setConnectionAwait(b);
    }

    public void setPingAwait(boolean b) {
        window.setPingAwait(b);
    }

    public void setGlWindowBreaked(boolean b) {
        isGlWindowBreaked = b;
    }

    public FoxWindow getWindow() {
        return window;
    }

    /*
        * При уничтожении полноэкранного окна исходный видеорежим его монитора восстанавливается, но гамма-рампа остается нетронутой.

        * Если вы будете использовать Vulkan для рендеринга в окне, отключите создание контекста, установив для подсказки GLFW_CLIENT_API значение GLFW_NO_API
            https://www.glfw.org/docs/3.3/vulkan_guide.html

        * Каждый раз, когда вы опрашиваете состояние, вы рискуете пропустить желаемое изменение состояния. Если нажатая клавиша снова будет отпущена до того,
            как вы опросите ее состояние, вы пропустите нажатие клавиши. Рекомендуемое решение — использовать обратный вызов клавиши, но существует также режим
            ввода GLFW_STICKY_KEYS.

            glfwSetInputMode(окно, GLFW_STICKY_KEYS, GLFW_TRUE);
            Когда включен режим липких ключей, опрашиваемое состояние ключа будет оставаться GLFW_PRESS до тех пор, пока состояние этого ключа не будет опрошено с
            помощью glfwGetKeyGLFW_RELEASE, в противном случае оно останется GLFW_PRESS.

            Если вы хотите знать, в каком состоянии находились клавиши Caps Lock и Num Lock при создании событий ввода, установите GLFW_LOCK_KEY_MODS режим ввода.

            glfwSetInputMode(окно, GLFW_LOCK_KEY_MODS, GLFW_TRUE);
            Когда этот режим ввода включен, любой обратный вызов, который получает биты-модификаторы, будет иметь GLFW_MOD_CAPS_LOCK< Бит /span> установлен. а>, если
            был включен Num Lock.GLFW_MOD_NUM_LOCK установлен, если в момент возникновения события был включен Caps Lock, и бит

            Константа GLFW_KEY_LAST содержит наибольшее значение любого токена ключа.

        * glfwIconifyWindow(окно); / glfwRestoreWindow(окно); = > иконизировать (т.е. свернуть) / восстанавливает окна после развертывания

        * Если хотите уведомить пользователя о событии, не прерывая его, вы можете запросить внимание с помощью glfwRequestWindowAttention(окно);
            Система подсветит указанное окно, а на платформах, где это не поддерживается, приложение в целом. Как только пользователь обратит на это
            внимание, система автоматически завершит запрос.

        * Узнать, находится ли курсор внутри области содержимого if (glfwGetWindowAttrib(окно, GLFW_HOVERED))

        * Ввод времени с высоким разрешением в секундах с помощью double секунд = glfwGetTime(); количество секунд с момента инициализации библиотеки с помощью glfwInit

        * Если системный буфер обмена содержит строку в кодировке UTF-8 или ее можно преобразовать в такую строку, вы
            можете получить ее с помощью glfwGetClipboardString text = glfwGetClipboardString(NULL);
    */
}
