package game.freya.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean cachePreparedStatements = true;

    private String userCountry = SystemUtils.USER_COUNTRY;
    private String worldsImagesDir = "./worlds/img/";

    private short prepStmtCacheSize = 250;
    private short prepStmtCacheSqlLimit = 2048;

    private int defaultServerPort = 13958;
    private int socketBufferSize = 4096; // 65536 | 16384 | 8192 | 4096

    private int serverOpenTimeAwait = 6_000; // сколько сервер должен открываться в начале работы
    private int socketPingAwaitTimeout = 6_000; // сколько миллисекунд Клиент и Сервер пингуются для установки факта успешной связи.
    private int connectionNoDataTimeout = 30_000; // таймаут без данных до авто-отключения.

    private long serverBroadcastDelay = 50L; // миллисекунд ждать между отправками данных

    private double dragSpeed = 12;
    private double scrollSpeed = 20;

    @JsonIgnore
    public int getMaxConnectionWasteTime() {
        return connectionNoDataTimeout - socketPingAwaitTimeout;
    }
}
