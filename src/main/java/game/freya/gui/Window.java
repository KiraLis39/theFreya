package game.freya.gui;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUSED;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_PERSPECTIVE_CORRECTION_HINT;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public abstract class Window {
    private final GameController gameController;

    @Getter
    private final Dimension monitorSize = Constants.MON.getConfiguration().getBounds().getSize();

    @Getter
    private long window;

    @Getter
    private BufferedImage wIcon;

    @Getter
    @Setter
    private float aspect = 1f;

    @Getter
    @Setter
    private Dimension windowedSize;

    @Getter
    @Setter
    private int width, height;

    @Getter
    private boolean isActiveWindow = false;

    protected Window(GameController gameController) {
        this.gameController = gameController;

        double newWidth = monitorSize.getWidth() * 0.75d;
        double newHeight = newWidth / (monitorSize.getWidth() / monitorSize.getHeight());
        windowedSize = new Dimension((int) newWidth, (int) newHeight);

        if (Constants.getUserConfig().isFullscreen()) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            this.width = monitorSize.width;
            this.height = monitorSize.height;
        } else {
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
            this.width = windowedSize.width;
            this.height = windowedSize.height;
        }

        // корректирует масштабы OpenGL < -1; 1 > на разрешение окна игры:
        if (width > height) {
            aspect = (float) width / height;
        } else if (width < height) {
            aspect = (float) height / width;
        }

        createWindow();
    }

    private void createWindow() {
        byte share = 0;
        window = glfwCreateWindow(width, height,
                Constants.getAppName().concat(" v.").concat(Constants.getAppVersion()),
                Constants.getUserConfig().isFullscreen() ? glfwGetPrimaryMonitor() : 0, share);
        if (window == NULL) {
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the GLFW menu");
        }

        // Минимальный и максимальный размер:
        // glfwSetWindowSizeLimits(window, width, height, width, height);

        // Соотношение сторон области содержимого окна оконного режима:
        // glfwSetWindowAspectRatio(window, 16, 9);

        GLFWVidMode mode = glfwGetVideoMode(window);
        if (mode != null) {
            glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate()); // определяет желаемую частоту обновления для полноэкранных окон
        } else {
            glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE); // GLFW_DONT_CARE = игнорировать
        }

        // прозрачность окна:
//        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
//        if (glfwGetWindowAttrib(window, GLFW_TRANSPARENT_FRAMEBUFFER) != -1) {
//            log.info("кадровый буфер окна в данный момент прозрачен");
//        } else {
//            log.error("fail...");
//        }
//        glfwSetWindowOpacity(window, 0.7f);
    }

    public void setActiveWindow(boolean b, ScreenType screen) {
        isActiveWindow = b;

        setFullscreen(Constants.getUserConfig().isFullscreen(), screen);

        if (b) {
            // RUN:
            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            //gameController.loadMenuTextures(); // подключаем текстуры, если требуется.

            // Enable v-sync
            if (Constants.getGameConfig().isUseVSync()) {
                glfwSwapInterval(1);
            } else {
                glfwSwapInterval(0);
            }

            glfwShowWindow(window);
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);

            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

            glViewport(0, 0, width, height);

            if (screen.equals(ScreenType.MENU_SCREEN)) {
                glOrtho(0, width, height, 0, -1.0f, 1.0f);
            } else {
                double frHeight = Math.tan((Constants.getUserConfig().getFov() / 360) * Math.PI) * Constants.getUserConfig().getZNear();
                double frWidth = frHeight * getAspect();
                glFrustum(-frWidth, frWidth, -frHeight, frHeight, Constants.getUserConfig().getZNear(), Constants.getUserConfig().getZFar());
            }
        } else {
            glfwSetWindowShouldClose(window, true);
            glfwFreeCallbacks(window);
            // glfwPostEmptyEvent();
            glfwDestroyWindow(window);
        }
    }

    protected void setWindowIcon() {
        if (wIcon != null) {
            byte[] iconData = ((DataBufferByte) wIcon.getRaster().getDataBuffer()).getData();
            ByteBuffer ib = createByteBuffer(iconData.length);
            ib.order(ByteOrder.nativeOrder());
            ib.put(iconData, 0, iconData.length);
            ib.flip();

            GLFWImage image = GLFWImage.malloc();
            image.set(wIcon.getWidth(), wIcon.getHeight(), ib);

            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);
            imagebf.put(0, image);

            glfwSetWindowIcon(window, imagebf);
        }
    }

    protected void setWIcon(BufferedImage icon) {
        this.wIcon = icon;
    }

    protected void onGameRestore() {
        if (!gameController.isCurrentWorldIsNetwork() && Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(false);
            log.debug("Resume game...");
        }
    }

    protected void onGameHide() {
        log.debug("Hide or minimized");
        if (!gameController.isCurrentWorldIsNetwork() && !Constants.isPaused() && Constants.getUserConfig().isPauseOnHidden()) {
            Constants.setPaused(true);
            log.debug("Paused...");
        }
    }

    public abstract void render();

    protected void setFullscreen(boolean isFullscreen, ScreenType type) {
        log.info("Set the window size...");
        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode == null) {
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the videoMode");
        }

        //createWindow();

        if (isFullscreen) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

            width = monitorSize.width;
            height = monitorSize.height;

            if (Constants.getUserConfig().getFullscreenType().equals(UserConfig.FullscreenType.MAXIMIZE_WINDOW)) {
                log.info("Работаем в псевдо-полноэкране...");
                glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
                glfwMaximizeWindow(window);
                glfwSetWindowPos(window, 0, 0);
            } else {
                log.info("Работаем в эксклюзивном режиме экрана...");
                glfwSetWindowSize(window, width, height);
                glfwSetWindowPos(window, 30, 60);
            }
        } else {
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);

            width = windowedSize.width;
            height = windowedSize.height;

            log.info("Работаем в оконном режиме экрана...");
            //glfwRestoreWindow(window);
            glfwSetWindowSize(window, width, height);
            glfwSetWindowPos(window,
                    (videoMode.width() - width) / 2,
                    (videoMode.height() - height) / 2);

            // set the icon to app window:
            setWindowIcon();
        }
    }
}
