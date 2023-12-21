package game.freya.config;

import game.freya.enums.gl.TexturesFilterMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.UUID;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_BACK_QUOTE;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F10;
import static java.awt.event.KeyEvent.VK_F11;
import static java.awt.event.KeyEvent.VK_F9;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_APOSTROPHE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F10;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class UserConfig {

    @Getter
    @Builder.Default
    private TexturesFilterMode texturesFilteringLevel = TexturesFilterMode.NEAREST;

    @Getter
    @Setter
    @Builder.Default
    private float miniMapOpacity = 0.65f;

    // player:
    @Getter
    @Setter
    @Builder.Default
    private UUID userId = UUID.randomUUID();

    @Getter
    @Setter
    @Builder.Default
    private String userName = SystemUtils.getUserName();

    @Getter
    @Setter
    @Builder.Default
    private String userMail = "demo@test.ru";

    // audio:
    @Getter
    @Setter
    @Builder.Default
    private boolean isSoundEnabled = true;

    @Getter
    @Setter
    @Builder.Default
    private int soundVolumePercent = 75;

    @Getter
    @Setter
    @Builder.Default
    private boolean isMusicEnabled = true;

    @Getter
    @Setter
    @Builder.Default
    private int musicVolumePercent = 75;

    // custom hotkeys:
    @Setter
    @Getter
    private int keyLookUp;

    @Setter
    @Getter
    private int keyLookLeft;

    @Setter
    @Getter
    private int keyLookRight;

    @Setter
    @Getter
    private int keyLookDown;

    @Setter
    @Getter
    private int keyMoveUp;

    @Setter
    @Getter
    private int keyMoveLeft;

    @Setter
    @Getter
    private int keyMoveRight;

    @Setter
    @Getter
    private int keyMoveDown;

    @Setter
    @Getter
    private int keyRotateClockwise;

    @Setter
    @Getter
    private int keyRotateCounter;

    @Setter
    @Getter
    @Builder.Default
    private int keyPause = GLFW_KEY_ESCAPE; // KeyEvent.VK_ESCAPE;

    @Setter
    @Getter
    @Builder.Default
    private int keyAccelerate = GLFW_KEY_LEFT_SHIFT; // KeyEvent.VK_SHIFT;

    @Setter
    @Getter
    @Builder.Default
    private int keySneak = GLFW_KEY_LEFT_CONTROL; // KeyEvent.VK_CONTROL;

    @Setter
    @Getter
    @Builder.Default
    private int keyConsole = KeyEvent.VK_BACK_QUOTE;

    @Setter
    @Getter
    @Builder.Default
    private int keyConsoleMod = InputEvent.SHIFT_DOWN_MASK;

    @Setter
    @Getter
    private int keyDebug;

    @Setter
    @Getter
    private int keyFps;

    @Setter
    @Getter
    private int keyFullscreen;

    // gameplay:
    @Getter
    @Setter
    @Builder.Default
    private boolean dragGameFieldOnFrameEdgeReached = true;

    @Getter
    @Setter
    @Builder.Default
    private FullscreenType fullscreenType = FullscreenType.MAXIMIZE_WINDOW; // .EXCLUSIVE

    @Getter
    @Setter
    @Builder.Default
    private boolean isFullscreen = false;

    @Getter
    @Setter
    @Builder.Default
    private boolean isPauseOnHidden = true;

    @Getter
    @Setter
    @Builder.Default
    private boolean isShowStartLogo = false;

    // graphics:
    @Getter
    @Setter
    @Builder.Default
    private boolean isUseSmoothing = true;

    @Getter
    @Setter
    @Builder.Default
    private boolean isUseBicubic = false;

    @Getter
    @Setter
    @Builder.Default
    private int fpsLimit = 60; // fps limit

    @Getter
    @Setter
    @Builder.Default
    private boolean isMultiBufferEnabled = true;

    @Setter
    @Builder.Default
    private int bufferedDeep = 2;

    @Setter
    @Builder.Default
    private int maxBufferedDeep = 2;

    @Setter
    @Builder.Default
    private double fov = 45;

    @Setter
    @Builder.Default
    private double zNear = 0.001;

    @Setter
    @Builder.Default
    private double zFar = 100;

    // other:
    public int getBufferedDeep() {
        return isMultiBufferEnabled ? Math.max(bufferedDeep, maxBufferedDeep) : 1;
    }

    public void resetControlKeys() {
        log.info("Выполняется сброс горячих клавиш на умолчания...");

        setKeyLookUp(KeyEvent.VK_UP);
        setKeyLookLeft(KeyEvent.VK_LEFT);
        setKeyLookRight(KeyEvent.VK_RIGHT);
        setKeyLookDown(KeyEvent.VK_DOWN);
        setKeyMoveUp(KeyEvent.VK_W);
        setKeyMoveLeft(KeyEvent.VK_A);
        setKeyMoveRight(KeyEvent.VK_D);
        setKeyMoveDown(KeyEvent.VK_S);
        setKeyConsole(KeyEvent.VK_BACK_QUOTE);
        setKeyConsoleMod(InputEvent.SHIFT_DOWN_MASK);
        setKeyFullscreen(VK_F11);
        setKeyPause(GLFW_KEY_ESCAPE); // KeyEvent.VK_ESCAPE);
        setKeyRotateClockwise(KeyEvent.VK_E);
        setKeyRotateCounter(VK_Q);
        setKeyAccelerate(GLFW_KEY_LEFT_SHIFT); // KeyEvent.VK_SHIFT;
        setKeySneak(GLFW_KEY_LEFT_CONTROL);
    }

    public enum DefaultHotKeys {
        CAM_FORWARD("Камера вперёд", VK_UP, GLFW_KEY_UP, 0),
        CAM_LEFT("Камера влево", VK_LEFT, GLFW_KEY_LEFT, 0),
        CAM_RIGHT("Камера вправо", VK_RIGHT, GLFW_KEY_RIGHT, 0),
        CAM_BACK("Камера назад", VK_DOWN, GLFW_KEY_DOWN, 0),
        MOVE_FORWARD("Движение вперед", VK_W, GLFW_KEY_W, 0),
        MOVE_LEFT("Движение влево", VK_A, GLFW_KEY_A, 0),
        MOVE_RIGHT("Движение вправо", VK_D, GLFW_KEY_D, 0),
        MOVE_BACK("Движение назад", VK_S, GLFW_KEY_S, 0),
        ROTATE_CLOCK("Поворот по часовой", VK_E, GLFW_KEY_E, 0),
        ROTATE_COUNTER("Поворот против часовой", VK_Q, GLFW_KEY_Q, 0),
        PAUSE("Меню/Пауза", VK_ESCAPE, GLFW_KEY_ESCAPE, 0),
        CONSOLE("Консоль", VK_BACK_QUOTE, GLFW_KEY_APOSTROPHE, VK_CONTROL),
        DEBUG("Отладка", VK_F10, GLFW_KEY_F10, 0),
        FPS("FPS", VK_F9, GLFW_KEY_F9, 0),
        FULLSCREEN("Переключение режима экрана", VK_F11, GLFW_KEY_F11, 0),
        ACCELERATION("Ускорение", VK_SHIFT, GLFW_KEY_LEFT_SHIFT, 0),
        SNEAK("Приседание", VK_CONTROL, GLFW_KEY_LEFT_CONTROL, 0);

        @Getter
        private final String description;

        @Getter
        private final int event;

        @Getter
        private final int glEvent;

        @Getter
        private final int mask;

        DefaultHotKeys(String description, int event, int glEvent, int mask) {
            this.description = description;
            this.event = event;
            this.glEvent = glEvent;
            this.mask = mask;
        }
    }

    public enum FullscreenType {
        MAXIMIZE_WINDOW,
        EXCLUSIVE
    }
}
