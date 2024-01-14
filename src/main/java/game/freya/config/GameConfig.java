package game.freya.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameConfig {
    @Builder.Default
    private int defaultServerPort = 13958;

    @Builder.Default
    private int socketBufferSize = 4096; // 65536 | 16384 | 8192 | 4096

    @Builder.Default
    private long serverBroadcastDelay = 50L; // миллисекунд ждать между отправками данных

    @Builder.Default
    private int socketPingAwaitTimeout = 9_000; // сколько миллисекунд клиент ждёт данные от Сервера

    @Builder.Default
    private int socketConnectionAwaitTimeout = 180_000; // сколько миллисекунд клиент ждёт данные от Сервера

    @Builder.Default
    private String databaseRootDirUrl = FileSystems.getDefault().getPath("./db/freya.db").toAbsolutePath().toString();

    // GL:
    @Builder.Default
    private boolean isGlDebugMode = true;

    @Builder.Default
    private boolean useVSync = true;

    @Builder.Default
    private boolean isCullFaceGlEnabled = true;

    @Builder.Default
    private boolean isLightsEnabled = true;

    @Builder.Default
    private boolean isMaterialEnabled = true;

    @Builder.Default
    private boolean isSmoothEnabled = true;

    @Builder.Default
    private boolean isDepthEnabled = true;

    @Builder.Default
    private boolean isUseAlphaTest = false;

    @Builder.Default
    private boolean isBlendEnabled = false;

    @Builder.Default
    private boolean useTextures = true;

    @Builder.Default
    private boolean useFog = false;
}
