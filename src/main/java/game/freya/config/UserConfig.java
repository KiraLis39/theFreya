package game.freya.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfig {
    @Builder.Default
    private UUID userId = UUID.randomUUID();

    @Builder.Default
    private String userName = "User-Hyuser";

    @Builder.Default
    private String userMail = "demo@test.ru";

    // audio:
    @Builder.Default
    private boolean isSoundEnabled = true;

    @Builder.Default
    private int soundVolumePercent = 75;

    @Builder.Default
    private boolean isMusicEnabled = true;

    @Builder.Default
    private int musicVolumePercent = 75;


    // hotkeys:
    @Builder.Default
    private int keyUp = KeyEvent.VK_UP;

    @Builder.Default
    private int keyLeft = KeyEvent.VK_LEFT;

    @Builder.Default
    private int keyRight = KeyEvent.VK_RIGHT;

    @Builder.Default
    private int keyDown = KeyEvent.VK_DOWN;

    @Builder.Default
    private int keyRotateClockwise = KeyEvent.VK_E;

    @Builder.Default
    private int keyRotateCounter = KeyEvent.VK_Q;

    @Builder.Default
    private int keyPause = KeyEvent.VK_ESCAPE;

    @Builder.Default
    private int keyConsole = KeyEvent.VK_BACK_QUOTE;

    @Builder.Default
    private int keyConsoleMod = InputEvent.SHIFT_DOWN_MASK;

    @Builder.Default
    private int keyFullscreen = KeyEvent.VK_F11;


    // booleans:
    @Builder.Default
    private boolean dragGameFieldOnFrameEdgeReached = true;

    @Builder.Default
    private boolean isFullscreen = false;

    @Builder.Default
    private boolean isUseSmoothing = true;

    @Builder.Default
    private boolean isUseBicubic = false;

    @Builder.Default
    private boolean isPauseOnHidden = true;

    @Builder.Default
    private long screenDiscreteLimit = 60L; // fps limit

    // other:
    @Builder.Default
    private boolean isMultiBufferEnabled = true;

    @Builder.Default
    private int bufferedDeep = 2;

    public int getBufferedDeep() {
        return isMultiBufferEnabled ? bufferedDeep : 1;
    }

    public void resetControlKeys() {
        setKeyUp(KeyEvent.VK_UP);
        setKeyLeft(KeyEvent.VK_LEFT);
        setKeyRight(KeyEvent.VK_RIGHT);
        setKeyDown(KeyEvent.VK_DOWN);
        setKeyConsole(KeyEvent.VK_BACK_QUOTE);
        setKeyConsoleMod(InputEvent.SHIFT_DOWN_MASK);
        setKeyFullscreen(KeyEvent.VK_F11);
        setKeyPause(KeyEvent.VK_ESCAPE);
        setKeyRotateClockwise(KeyEvent.VK_E);
        setKeyRotateCounter(KeyEvent.VK_Q);
    }
}
