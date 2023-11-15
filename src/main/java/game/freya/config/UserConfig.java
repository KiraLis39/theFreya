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


    // hotkeys:
    @Setter
    @Getter
    @Builder.Default
    private int keyLookUp = KeyEvent.VK_UP;

    @Setter
    @Getter
    @Builder.Default
    private int keyLookLeft = KeyEvent.VK_LEFT;

    @Setter
    @Getter
    @Builder.Default
    private int keyLookRight = KeyEvent.VK_RIGHT;

    @Setter
    @Getter
    @Builder.Default
    private int keyLookDown = KeyEvent.VK_DOWN;


    @Setter
    @Getter
    @Builder.Default
    private int keyMoveUp = KeyEvent.VK_W;

    @Setter
    @Getter
    @Builder.Default
    private int keyMoveLeft = KeyEvent.VK_A;

    @Setter
    @Getter
    @Builder.Default
    private int keyMoveRight = KeyEvent.VK_D;

    @Setter
    @Getter
    @Builder.Default
    private int keyMoveDown = KeyEvent.VK_S;


    @Setter
    @Getter
    @Builder.Default
    private int keyRotateClockwise = KeyEvent.VK_E;

    @Setter
    @Getter
    @Builder.Default
    private int keyRotateCounter = KeyEvent.VK_Q;

    @Setter
    @Getter
    @Builder.Default
    private int keyPause = KeyEvent.VK_ESCAPE;

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
    @Builder.Default
    private int keyFullscreen = KeyEvent.VK_F11;


    // booleans:

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
    private boolean isUseSmoothing = true;

    @Getter
    @Setter
    @Builder.Default
    private boolean isUseBicubic = false;

    @Getter
    @Setter
    @Builder.Default
    private boolean isPauseOnHidden = true;

    @Getter
    @Setter
    @Builder.Default
    private long screenDiscreteLimit = 60L; // fps limit

    // other:

    @Getter
    @Setter
    @Builder.Default
    private boolean isMultiBufferEnabled = true;

    @Setter
    @Builder.Default
    private int bufferedDeep = 2;

    public int getBufferedDeep() {
        return isMultiBufferEnabled ? bufferedDeep : 1;
    }

    public void resetControlKeys() {
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

    public enum HotKeys {
        VK_UP("Камера вверх", Constants.getUserConfig().getKeyLookUp(), 0),
        VK_LEFT("Камера влево", Constants.getUserConfig().getKeyLookLeft(), 0),
        VK_RIGHT("Камера вправо", Constants.getUserConfig().getKeyLookRight(), 0),
        VK_DOWN("Камера вниз", Constants.getUserConfig().getKeyLookDown(), 0),
        VK_W("Движение вперед", Constants.getUserConfig().getKeyMoveUp(), 0),
        VK_A("Движение влево", Constants.getUserConfig().getKeyMoveLeft(), 0),
        VK_D("Движение вправо", Constants.getUserConfig().getKeyMoveRight(), 0),
        VK_S("Движение назад", Constants.getUserConfig().getKeyMoveDown(), 0),
        VK_E("Поворот по часовой", Constants.getUserConfig().getKeyRotateClockwise(), 0),
        VK_Q("Поворот против часовой", Constants.getUserConfig().getKeyRotateCounter(), 0),
        VK_ESCAPE("Меню/Пауза", Constants.getUserConfig().getKeyPause(), 0),
        VK_BACK_QUOTE("Консоль", Constants.getUserConfig().getKeyConsole(), Constants.getUserConfig().getKeyConsoleMod()),
        VK_F11("Переключение режима экрана", Constants.getUserConfig().getKeyFullscreen(), 0);

        @Getter
        private final String description;

        @Getter
        private final int event;

        @Getter
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
