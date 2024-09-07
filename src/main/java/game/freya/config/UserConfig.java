package game.freya.config;

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

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class UserConfig {

    @Getter
    @Builder.Default
    private float miniMapOpacity = 0.65f;

    // player:
    @Getter
    @Builder.Default
    private UUID userId = UUID.randomUUID();

    @Getter
    @Builder.Default
    private String userName = SystemUtils.USER_NAME;

    @Getter
    @Builder.Default
    private String userMail = "demo@test.ru";

    // audio:
    @Getter
    @Builder.Default
    private boolean isSoundEnabled = true;

    @Getter
    @Builder.Default
    private int soundVolumePercent = 75;

    @Getter
    @Builder.Default
    private boolean isMusicEnabled = true;

    @Getter
    @Builder.Default
    private int musicVolumePercent = 75;

    // hotkeys:
    @Getter
    @Builder.Default
    private int keyLookUp = KeyEvent.VK_UP;

    @Getter
    @Builder.Default
    private int keyLookLeft = KeyEvent.VK_LEFT;

    @Getter
    @Builder.Default
    private int keyLookRight = KeyEvent.VK_RIGHT;

    @Getter
    @Builder.Default
    private int keyLookDown = KeyEvent.VK_DOWN;

    @Getter
    @Builder.Default
    private int keyMoveUp = KeyEvent.VK_W;

    @Getter
    @Builder.Default
    private int keyMoveLeft = KeyEvent.VK_A;

    @Getter
    @Builder.Default
    private int keyMoveRight = KeyEvent.VK_D;

    @Getter
    @Builder.Default
    private int keyMoveDown = KeyEvent.VK_S;

    @Getter
    @Builder.Default
    private int keyRotateClockwise = KeyEvent.VK_E;

    @Getter
    @Builder.Default
    private int keyRotateCounter = KeyEvent.VK_Q;

    @Getter
    @Builder.Default
    private int keyPause = KeyEvent.VK_ESCAPE;

    @Getter
    @Builder.Default
    private int keyConsole = KeyEvent.VK_BACK_QUOTE;

    @Getter
    @Builder.Default
    private int keyConsoleMod = InputEvent.SHIFT_DOWN_MASK;

    @Getter
    @Builder.Default
    private int keyDebug = KeyEvent.VK_F10;

    @Getter
    @Builder.Default
    private int keyFps = KeyEvent.VK_F9;

    @Getter
    @Builder.Default
    private int keyFullscreen = KeyEvent.VK_F11;

    // gameplay:
    @Getter
    @Builder.Default
    private boolean dragGameFieldOnFrameEdgeReached = true;

    @Getter
    @Builder.Default
    private FullscreenType fullscreenType = FullscreenType.MAXIMIZE_WINDOW; // .EXCLUSIVE

    @Getter
    @Builder.Default
    private boolean isFullscreen = false;

    @Getter
    @Builder.Default
    private boolean isPauseOnHidden = true;

    // graphics:
    @Getter
    @Builder.Default
    private boolean isUseSmoothing = true;

    @Getter
    @Builder.Default
    private boolean isUseBicubic = false;

    @Getter
    @Builder.Default
    private int fpsLimit = 60; // fps limit

    @Getter
    @Builder.Default
    private boolean isMultiBufferEnabled = true;

    @Builder.Default
    private int bufferedDeep = 2;

    @Builder.Default
    private int maxBufferedDeep = 2;


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
        setKeyFullscreen(KeyEvent.VK_F11);
        setKeyPause(KeyEvent.VK_ESCAPE);
        setKeyRotateClockwise(KeyEvent.VK_E);
        setKeyRotateCounter(KeyEvent.VK_Q);
    }

    @Getter
    public enum HotKeys {
        CAM_UP("Камера вверх", Constants.getUserConfig().getKeyLookUp(), 0),
        CAM_LEFT("Камера влево", Constants.getUserConfig().getKeyLookLeft(), 0),
        CAM_RIGHT("Камера вправо", Constants.getUserConfig().getKeyLookRight(), 0),
        CAM_DOWN("Камера вниз", Constants.getUserConfig().getKeyLookDown(), 0),
        MOVE_UP("Движение вперед", Constants.getUserConfig().getKeyMoveUp(), 0),
        MOVE_LEFT("Движение влево", Constants.getUserConfig().getKeyMoveLeft(), 0),
        MOVE_RIGHT("Движение вправо", Constants.getUserConfig().getKeyMoveRight(), 0),
        MOVE_BACK("Движение назад", Constants.getUserConfig().getKeyMoveDown(), 0),
        ROTATE_CLOCK("Поворот по часовой", Constants.getUserConfig().getKeyRotateClockwise(), 0),
        ROTATE_COUNTER("Поворот против часовой", Constants.getUserConfig().getKeyRotateCounter(), 0),
        PAUSE("Меню/Пауза", Constants.getUserConfig().getKeyPause(), 0),
        CONSOLE("Консоль", Constants.getUserConfig().getKeyConsole(), Constants.getUserConfig().getKeyConsoleMod()),
        DEBUG("Отладка", Constants.getUserConfig().getKeyDebug(), 0),
        FPS("FPS", Constants.getUserConfig().getKeyFps(), 0),
        FULLSCREEN("Переключение режима экрана", Constants.getUserConfig().getKeyFullscreen(), 0);

        private final String description;

        private final int event;

        private final int mask;

        HotKeys(String description, int event, int mask) {
            this.description = description;
            this.event = event;
            this.mask = mask;
        }
    }

    public enum FullscreenType {
        MAXIMIZE_WINDOW,
        EXCLUSIVE
    }
}
