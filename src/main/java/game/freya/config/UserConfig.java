package game.freya.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.input.KeyInput;
import game.freya.enums.gui.FullscreenType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserConfig implements Serializable {
    // player:
    @NotNull
    @Builder.Default
    private UUID userId = UUID.randomUUID();

    @NotNull
    @Builder.Default
    private String userName = SystemUtils.USER_NAME;

    @NotNull
    @Builder.Default
    private String userMail = "demo@test.ru";

    private String userAvatar;

    // audio:
    @Builder.Default
    private boolean isSoundEnabled = true;

    @Builder.Default
    private int soundVolumePercent = 75;

    @Builder.Default
    private boolean isMusicEnabled = true;

    @Builder.Default
    private int musicVolumePercent = 75;

    @Builder.Default
    private boolean isBackgEnabled = true;

    @Builder.Default
    private int backgVolumePercent = 75;

    // gameplay:
    @Builder.Default
    private float miniMapOpacity = 0.65f;

    @Builder.Default
    private boolean dragGameFieldOnFrameEdgeReached = true;

    @NotNull
    @Builder.Default
    private FullscreenType fullscreenType = FullscreenType.MAXIMIZE_WINDOW; // .EXCLUSIVE

    @Builder.Default
    private boolean isFullscreen = false;

    @Builder.Default
    private boolean isPauseOnHidden = true;

    private int windowWidth;

    private int windowHeight;

    @Builder.Default
    private float fov = 45.f; // default 45

    // graphics:
    @Builder.Default
    private boolean useSmoothing = true;

    @Builder.Default
    private boolean useBicubic = false;

    @Builder.Default
    private boolean useVSync = true;

    @Builder.Default
    private int fpsLimit = 60; // fps limit

    @Builder.Default
    private int multiSamplingLevel = 4;

    @Builder.Default
    private boolean isMultiBufferEnabled = true;

    @Builder.Default
    private int bufferedDeep = 2;

    @Builder.Default
    private int maxBufferedDeep = 2;

    @Builder.Default
    private boolean useGammaCorrection = true;

    @Builder.Default
    private boolean useStereo3D = false;

    @Builder.Default
    private Hotkeys hotkeys = new Hotkeys();

    @Builder.Default
    private boolean showSettingsOnLaunch = true;

    @Builder.Default
    private boolean isMuteOnLostFocus = true;

    @JsonIgnore
    public int getBufferedDeep() {
        return isMultiBufferEnabled ? Math.max(bufferedDeep, maxBufferedDeep) : 1;
    }

    @Override
    public String toString() {
        return "UserConfig{"
                + "userId=" + userId
                + ", userName='" + userName + '\''
                + ", userMail='" + userMail + '\''
                + '}';
    }

    @Getter
    @AllArgsConstructor
    private enum DefaultHotKeys {
        // cam look:
        CAM_UP("Камера вверх", UniKey.builder()
                .swingKey(KeyEvent.VK_UP)
                .jmeKey(KeyInput.KEY_UP)
                .build()),
        CAM_LEFT("Камера влево", UniKey.builder()
                .swingKey(KeyEvent.VK_LEFT)
                .jmeKey(KeyInput.KEY_LEFT)
                .build()),
        CAM_RIGHT("Камера вправо", UniKey.builder()
                .swingKey(KeyEvent.VK_RIGHT)
                .jmeKey(KeyInput.KEY_RIGHT)
                .build()),
        CAM_DOWN("Камера вниз", UniKey.builder()
                .swingKey(KeyEvent.VK_DOWN)
                .jmeKey(KeyInput.KEY_DOWN)
                .build()),

        // moving:
        MOVE_FORWARD("Движение вперед", UniKey.builder()
                .swingKey(KeyEvent.VK_W)
                .jmeKey(KeyInput.KEY_W)
                .build()),
        MOVE_LEFT("Движение влево", UniKey.builder()
                .swingKey(KeyEvent.VK_A)
                .jmeKey(KeyInput.KEY_A)
                .build()),
        MOVE_BACK("Движение назад", UniKey.builder()
                .swingKey(KeyEvent.VK_S)
                .jmeKey(KeyInput.KEY_S)
                .build()),
        MOVE_RIGHT("Движение вправо", UniKey.builder()
                .swingKey(KeyEvent.VK_D)
                .jmeKey(KeyInput.KEY_D)
                .build()),

        // rotation:
        ROTATE_CLOCK("Поворот по часовой", UniKey.builder()
                .swingKey(KeyEvent.VK_R)
                .jmeKey(KeyInput.KEY_R)
                .build()),
        ROTATE_COUNTER("Поворот против часовой", UniKey.builder()
                .swingKey(KeyEvent.VK_R)
                .swingMask(InputEvent.SHIFT_DOWN_MASK)
                .jmeKey(KeyInput.KEY_R) // , KeyInput.KEY_RSHIFT
                .build()),

        // gameplay:
        PAUSE("Меню/Пауза/Выход", UniKey.builder()
                .swingKey(KeyEvent.VK_ESCAPE)
                .jmeKey(KeyInput.KEY_ESCAPE)
                .build()),
        CONSOLE("Консоль", UniKey.builder()
                .swingKey(KeyEvent.VK_BACK_QUOTE)
                .swingMask(InputEvent.CTRL_DOWN_MASK)
                .jmeKey(KeyInput.KEY_PERIOD) // , KeyInput.KEY_LCONTROL
                .build()),
        DEBUG("Отладка", UniKey.builder()
                .swingKey(KeyEvent.VK_F7)
                .jmeKey(KeyInput.KEY_F7)
                .build()),
        FULLSCREEN("Переключение экрана", UniKey.builder()
                .swingKey(KeyEvent.VK_F11)
                .jmeKey(KeyInput.KEY_F11)
                .build()),
        ALT_CUR_MODE("Переключение режима курсора в игре", UniKey.builder()
                .swingKey(KeyEvent.VK_CONTROL)
                .jmeKey(KeyInput.KEY_LCONTROL)
                .build());

        private final String description;
        private final UniKey key;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Hotkeys {
        private UniKey keyLookUp = DefaultHotKeys.CAM_UP.key;
        private UniKey keyLookLeft = DefaultHotKeys.CAM_LEFT.key;
        private UniKey keyLookRight = DefaultHotKeys.CAM_RIGHT.key;
        private UniKey keyLookDown = DefaultHotKeys.CAM_DOWN.key;

        private UniKey keyMoveForward = DefaultHotKeys.MOVE_FORWARD.key;
        private UniKey keyMoveLeft = DefaultHotKeys.MOVE_LEFT.key;
        private UniKey keyMoveRight = DefaultHotKeys.MOVE_RIGHT.key;
        private UniKey keyMoveBack = DefaultHotKeys.MOVE_BACK.key;

        private UniKey keyRotateClockwise = DefaultHotKeys.ROTATE_CLOCK.key;
        private UniKey keyRotateCounter = DefaultHotKeys.ROTATE_COUNTER.key;

        private UniKey keyPause = DefaultHotKeys.PAUSE.key;
        private UniKey keyConsole = DefaultHotKeys.CONSOLE.key;
        private UniKey keyDebugInfo = DefaultHotKeys.DEBUG.key;
        private UniKey keyFullscreen = DefaultHotKeys.FULLSCREEN.key;
        private UniKey keyAltCursorMode = DefaultHotKeys.ALT_CUR_MODE.key;

        public void resetControlKeys() {
            setKeyLookUp(DefaultHotKeys.CAM_UP.key);
            setKeyLookLeft(DefaultHotKeys.CAM_LEFT.key);
            setKeyLookRight(DefaultHotKeys.CAM_RIGHT.key);
            setKeyLookDown(DefaultHotKeys.CAM_DOWN.key);

            setKeyMoveForward(DefaultHotKeys.MOVE_FORWARD.key);
            setKeyMoveLeft(DefaultHotKeys.MOVE_LEFT.key);
            setKeyMoveRight(DefaultHotKeys.MOVE_RIGHT.key);
            setKeyMoveBack(DefaultHotKeys.MOVE_BACK.key);

            setKeyRotateClockwise(DefaultHotKeys.ROTATE_CLOCK.key);
            setKeyRotateCounter(DefaultHotKeys.ROTATE_COUNTER.key);

            setKeyPause(DefaultHotKeys.PAUSE.key);
            setKeyConsole(DefaultHotKeys.CONSOLE.key);
            setKeyDebugInfo(DefaultHotKeys.DEBUG.key);
            setKeyFullscreen(DefaultHotKeys.FULLSCREEN.key);
            setKeyAltCursorMode(DefaultHotKeys.ALT_CUR_MODE.key);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class UniKey {
        private int swingKey; // KeyEvent

        @Builder.Default
        private int swingMask = 0; // InputEvent

        private int jmeKey; // KeyInput

        @Builder.Default
        private boolean jmeMask = true; // KeyInput
    }
}
