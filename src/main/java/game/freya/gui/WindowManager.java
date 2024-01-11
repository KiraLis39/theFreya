package game.freya.gui;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.Media;
import game.freya.enums.other.ScreenType;
import game.freya.gui.panes.GameWindow;
import game.freya.gui.panes.MenuWindow;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_AUTO_ICONIFY;
import static org.lwjgl.glfw.GLFW.GLFW_CENTER_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_NO_ERROR;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
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
public class WindowManager {
    private GameController gameController;

    private FoxWindow currentWindow;

    private ScreenType currentScreen;

    public void appStart(GameController gameController) {
        this.gameController = gameController;

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

        GLFWErrorCallback.createPrint(System.err).set();
        loadScreen(ScreenType.MENU_SCREEN);
    }

    private void logoEndAwait() {
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

    private void doHintsPreset() {
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
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

    public void loadScreen(ScreenType screen) {
        close(currentWindow);
        if (screen != null) {
            this.currentScreen = screen;
        }

        if (currentScreen.equals(ScreenType.MENU_SCREEN)) {
            currentWindow = new MenuWindow(this, gameController);
        } else if (currentScreen.equals(ScreenType.GAME_SCREEN)) {
            currentWindow = new GameWindow(this, gameController);
        }

        // ждём пока кончится показ лого:
        logoEndAwait();

        currentWindow.setVisible(true);
        draw(); // блокируется до закрытия окна.
    }

    private void draw() {
        long lastUpdate = System.currentTimeMillis();
        int frames = 0;

        // Начинаем цикл рисований:
        Media.playSound("landing");
        while (!gameController.isGlWindowBreaked() && !glfwWindowShouldClose(currentWindow.getWindow())) {
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                currentWindow.render();

                // swap the color buffers
                glfwSwapBuffers(currentWindow.getWindow());

                // draw fps:
                if (Constants.isFpsInfoVisible() && !gameController.isGlWindowBreaked()) {
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
                if (!gameController.isGlWindowBreaked()) {
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
            }
        }

        // end of work:
        if (gameController.isGlWindowBreaked()) {
            try {
                close(currentWindow);
            } catch (Throwable e) {
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

    private void close(FoxWindow window) {
        if (window != null && window.isWindowActive()) {
            window.destroyWindowContext(window.getType());
        }
    }

    private void exit() {
        Media.playSound("jump");

        GL.setCapabilities(null);

        // Terminate GLFW and free the error callback
        if (!currentWindow.isDestroyed()) {
            glfwFreeCallbacks(currentWindow.getWindow());
            glfwDestroyWindow(currentWindow.getWindow());
        }
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
