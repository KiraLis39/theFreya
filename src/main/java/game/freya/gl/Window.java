package game.freya.gl;

import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
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
import static org.lwjgl.glfw.GLFW.glfwCreateCursor;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public abstract class Window {
    @Getter
    private final Dimension monitorSize = Constants.MON.getConfiguration().getBounds().getSize();

    @Getter
    private final Dimension windowedSize;

    @Getter
    private long window = -1;

    @Getter
    private BufferedImage wIcon, wCur;

    @Getter
    @Setter
    private float aspect = 1f;

    @Getter
    @Setter
    private int width, height;

    @Getter
    private boolean isVisible = false;

    private GLFWImage iconImage;

    private GLFWImage.Buffer iconBuffer;

    private GLFWImage curImage;

    protected Window() {
        windowedSize = calculateWindowedDimOf(0.75d);
        createWindow();
        setCursor("yellow");
    }

    private Dimension calculateWindowedDimOf(double mod) {
        double w = monitorSize.getWidth() * mod;
        int h = (int) (w / (monitorSize.getWidth() / monitorSize.getHeight()));
        return new Dimension((int) w, h);
    }

    protected void createWindow() {
        recalculateWindowSize();

        byte share = 0;
        window = glfwCreateWindow(width, height, "na", Constants.getUserConfig().isFullscreen() ? glfwGetPrimaryMonitor() : 0, share);
        if (window == NULL) {
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the GLFW menu");
        }

        // Минимальный и максимальный размер:
        // glfwSetWindowSizeLimits(window, width, height, width, height);

        // Соотношение сторон области содержимого окна оконного режима:
        // glfwSetWindowAspectRatio(window, 16, 9);

        // Resize the window, calculate width, height and aspect:
        setFullscreen(Constants.getUserConfig().isFullscreen());

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Enable v-sync
        if (Constants.getGameConfig().isUseVSync()) {
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }

        glViewport(0, 0, width, height);

        // прозрачность окна:
//        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
//        if (glfwGetWindowAttrib(window, GLFW_TRANSPARENT_FRAMEBUFFER) != -1) {
//            log.info("кадровый буфер окна в данный момент прозрачен");
//        } else {
//            log.error("fail...");
//        }
//        glfwSetWindowOpacity(window, 0.7f);
    }

    public void destroy() {
        if (window != -1) {
            log.info("The window closing...");
            glfwSetWindowShouldClose(window, true);
            glfwFreeCallbacks(window);
            // glfwPostEmptyEvent();

            iconBuffer.free();
            iconImage.free();
            curImage.free();

            glfwDestroyWindow(window);
        }
    }

    protected void setWindowIcon() {
        try (InputStream winIconRes = getClass().getResourceAsStream(Constants.gameIconPath)) {
            if (winIconRes == null) {
                log.error("Не удалось проставить иконку окна игры {}", Constants.gameIconPath);
                return;
            }

            this.wIcon = ImageIO.read(winIconRes);
            if (wIcon != null) {
                byte[] iconData = ((DataBufferByte) wIcon.getRaster().getDataBuffer()).getData();

                ByteBuffer ib = createByteBuffer(iconData.length);
                ib.order(ByteOrder.nativeOrder());
                ib.put(iconData, 0, iconData.length);
                ib.flip();

                iconImage = GLFWImage.malloc();
                iconImage.set(wIcon.getWidth(), wIcon.getHeight(), ib);

                iconBuffer = GLFWImage.malloc(1);
                iconBuffer.put(0, iconImage);

                glfwSetWindowIcon(window, iconBuffer);
            }
        } catch (IOException e) {
            log.error("Не удалось загрузить иконку окна: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    protected void setCursor(String curName) {
        this.wCur = (BufferedImage) Constants.CACHE.get(curName);
        if (wCur != null) {
            BufferedImage c = new BufferedImage(wCur.getWidth(), wCur.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics2D g2D = c.createGraphics();
            g2D.drawImage(wCur, 0, 0, null);
            g2D.dispose();

            byte[] curData = ((DataBufferByte) c.getData().getDataBuffer()).getData();

            ByteBuffer ib = createByteBuffer(curData.length);
            ib.put(curData, 0, curData.length);
            ib.flip();

            curImage = GLFWImage.malloc();
            curImage.set(c.getWidth(), c.getHeight(), ib);

            long cur = glfwCreateCursor(curImage, 0, 0);
            glfwSetCursor(window, cur);
        }
    }

    public void render() {
        glfwSetWindowTitle(window, Constants.getAppName()
                .concat(" v.").concat(Constants.getAppVersion())
                .concat(" (fps: ").concat(Constants.getRealFreshRate() + ")"));
    }

    protected void setFullscreen(boolean isFullscreen) {
        log.info("Set the window size...");
        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode == null) {
            glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE); // GLFW_DONT_CARE = игнорировать
            throw new GlobalServiceException(ErrorMessages.GL, "Failed to create the videoMode");
        }
        glfwWindowHint(GLFW_REFRESH_RATE, videoMode.refreshRate()); // определяет частоту обновления для полноэкранных окон

        recalculateWindowSize();

        if (isFullscreen) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
            if (Constants.getUserConfig().getFullscreenType().equals(UserConfig.FullscreenType.MAXIMIZE_WINDOW)) {
                log.info("Работаем в псевдо-полноэкране...");
                glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
                glfwMaximizeWindow(window);
                glfwSetWindowPos(window, 0, 0);
            } else {
                log.info("Работаем в эксклюзивном режиме экрана...");
                glfwSetWindowSize(window, width, height);
            }
        } else {
            log.info("Работаем в оконном режиме экрана...");
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
            glfwSetWindowSize(window, width, height);
            glfwSetWindowPos(window,
                    (videoMode.width() - width) / 2,
                    (videoMode.height() - height) / 2);

            // set the icon to app window:
            setWindowIcon();
        }

        // корректирует масштабы OpenGL [-1, 1] на разрешение окна игры:
        if (width > height) {
            aspect = (float) width / height;
        } else if (width < height) {
            aspect = (float) height / width;
        }
    }

    private void recalculateWindowSize() {
        if (Constants.getUserConfig().isFullscreen()) {
            width = monitorSize.width;
            height = monitorSize.height;
        } else {
            width = windowedSize.width;
            height = windowedSize.height;
        }
    }

    protected void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        if (isVisible) {
            glfwShowWindow(window);
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        } else {
            glfwHideWindow(window);
        }
    }

    protected abstract void onGameRestore();

    protected abstract void onGameHide();
}
