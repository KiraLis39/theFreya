package game.freya.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.system.AppSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public final class GameConfig {
    private boolean isShowStartLogo = true;
    private boolean isDebugInfoVisible = false;
    private boolean isFpsInfoVisible = true;
    private boolean isStatsInfoVisible = true;
    private boolean cachePreparedStatements = true;
    private boolean isGameWindowResizable = false;
    private boolean isLinearSrgbImagesEnable = true;
    private boolean useSwapBuffers = true;

    private String userCountry = SystemUtils.USER_COUNTRY;
    private String worldsImagesDir = "./worlds/img/";

    private short prepStmtCacheSize = 250;
    private short prepStmtCacheSqlLimit = 2048;

    private int defaultServerPort = 13958;
    private int socketSendBufferSize = 2048; // 65536 | 16384 | 8192 | 4096
    private int socketReceiveBufferSize = 8192; // 65536 | 16384 | 8192 | 4096

    private int socketAuthTimeout = 6_000; // таймаут авторизации сокета Сервером.
    private int serverOpenTimeAwait = 6_000; // сколько сервер должен открываться в начале работы
    private int serverCloseTimeAwait = 6_000; // сколько сервер должен завершать работу
    private int socketPingAwaitTimeout = 6_000; // сколько миллисекунд Клиент и Сервер пингуются для установки факта успешной связи.
    private int socketConnectionTimeout = 6_000; // таймаут подключения к Серверу.
    private int connectionNoDataTimeout = 30_000; // таймаут без данных до авто-отключения.

    private long serverBroadcastDelay = 100L; // простой между проверками на наличие новых данных для отправки на Сервер.

    private double dragSpeed = 12;
    private double scrollSpeed = 20;

    private float zoomSpeed = 10.0f; // default 1.0f

    @JsonIgnore
    public int getMaxConnectionWasteTime() {
        return connectionNoDataTimeout - socketPingAwaitTimeout;
    }

    private String defaultGlRenderer = AppSettings.LWJGL_OPENGL45;
}
