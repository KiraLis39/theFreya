package game.freya.net;

import game.freya.config.Constants;
import game.freya.exceptions.GlobalServiceException;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

@Slf4j
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
//@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class PingService {
    private final GameControllerService gameControllerService;
    private volatile boolean pingBroke;

    public boolean pingServer(String host, Integer port, UUID worldUid) {
        this.pingBroke = false;

        try (SocketConnection conn = new SocketConnection()) {
            try {
                // подключаемся к серверу:
                conn.openSocket(host, port, gameControllerService, true);

                // ждём пока получим ответ PONG от Сервера:
                long was = System.currentTimeMillis();
                while (conn.isAlive() && !pingBroke && !conn.isPongReceived()
                        && System.currentTimeMillis() - was < Constants.getGameConfig().getSocketPingAwaitTimeout()
                ) {
                    conn.join(333);
                }

                // проверяем получен ли ответ:
                if (conn.isPongReceived()) {
                    log.info("Пинг к Серверу {}:{} прошел успешно", host, port);
                    return true;
                } else if (conn.getLastExplanation() != null) { //  && conn.getLastExplanation().equals(worldUid.toString())
                    log.warn("Пинг к Серверу {}:{} не прошел (1): {}", host, port, conn.getLastExplanation());
                }
            } catch (InterruptedException e) {
                log.warn("Пинг к Серверу {}:{} не прошел (2): {}", host, port, conn.getLastExplanation());
            } catch (GlobalServiceException gse) {
                log.warn("Пинг к Серверу {}:{} не прошел (3): {} ({})", host, port, gse.getMessage(), conn.getLastExplanation());
            } catch (Exception e) {
                log.warn("Пинг к Серверу {}:{} не прошел (4): {}", host, port, ExceptionUtils.getFullExceptionMessage(e));
            }
        }
        return false;
    }

    public void breakPing() {
        this.pingBroke = true;
    }
}
