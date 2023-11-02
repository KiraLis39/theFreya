package game.freya.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@Slf4j
@Builder
public class UserConfig {

    @Getter
    @Setter
    @Builder.Default
    private static String userName = "<no_name_user>";

    // audio:
    @Getter
    @Setter
    @Builder.Default
    private static boolean isSoundEnabled = true;

    @Getter
    @Setter
    @Builder.Default
    private static int soundVolumePercent = 75;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isMusicEnabled = true;

    @Getter
    @Setter
    @Builder.Default
    private static int musicVolumePercent = 75;


    // hotkeys:
    @Getter
    @Setter
    @Builder.Default
    private static int keyLeft = KeyEvent.VK_LEFT;

    @Getter
    @Setter
    @Builder.Default
    private static int keyRight = KeyEvent.VK_RIGHT;

    @Getter
    @Setter
    @Builder.Default
    private static int keyDown = KeyEvent.VK_DOWN;

    @Getter
    @Setter
    @Builder.Default
    private static int keyRotateClockwise = KeyEvent.VK_E;

    @Getter
    @Setter
    @Builder.Default
    private static int keyRotateCounter = KeyEvent.VK_Q;

    @Getter
    @Setter
    @Builder.Default
    private static int keyPause = KeyEvent.VK_ESCAPE;

    @Getter
    @Setter
    @Builder.Default
    private static int keyConsole = KeyEvent.VK_BACK_QUOTE;

    @Getter
    @Setter
    @Builder.Default
    private static int keyConsoleMod = InputEvent.SHIFT_DOWN_MASK;

    @Getter
    @Setter
    @Builder.Default
    private static int keyFullscreen = KeyEvent.VK_F11;


    // booleans:
    @Getter
    @Setter
    @Builder.Default
    private static boolean dragGameFieldOnFrameEdgeReached = true;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isShowStartLogo = true;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isHardcoreMode = false;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isFullscreen = false;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isUseSmoothing = true;

    @Getter
    @Setter
    @Builder.Default
    private static boolean isUseBicubic = false;

    @Getter
    @Setter
    @Builder.Default
    private static boolean pauseOnHidden = true;

    @Getter
    @Setter
    @Builder.Default
    private static long screenDiscreteLimit = 30L; // fps limit

    private UserConfig() {
    }
}
