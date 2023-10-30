package game.freya.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Slf4j
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Component
public class UserConfig {
    private String userName;
    private String gameThemeName;

    private boolean isShowStartLogo;
    private boolean isNextFigureShow;
    private boolean isSpecialBlocksEnabled;
    private boolean isAutoChangeMelody;
    private boolean isHardcoreMode;
    private boolean useBackImage;
    private boolean isFullscreen;

    private boolean isSoundEnabled;
    private int soundVolumePercent;
    private boolean isMusicEnabled;
    private int musicVolumePercent;

    private int keyLeft;
    private int keyLeftMod;
    private int keyRight;
    private int keyRightMod;
    private int keyDown;
    private int keyDownMod;
    private int keyStuck;
    private int keyStuckMod;
    private int keyRotate;
    private int keyRotateMod;
    private int keyPause;
    private int keyPauseMod;
    private int keyConsole;
    private int keyConsoleMod;
    private int keyFullscreen;
    private int keyFullscreenMod;
}
