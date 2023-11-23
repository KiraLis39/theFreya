package game.freya;

import game.freya.config.Constants;
import game.freya.net.ClientDataDTO;
import game.freya.net.SocketService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConnection;

import java.sql.SQLException;

@Slf4j
@Getter
@Setter
public class GameControllerBase {
    private SQLiteConnection conn;
    private Thread netDataTranslator;
    private boolean isPlayerMovingUp = false, isPlayerMovingDown = false, isPlayerMovingLeft = false, isPlayerMovingRight = false;


    public void closeDataBaseConnection() {
        try {
            if (conn != null) {
                log.info("Connection to SQLite is closing...");
                conn.close();
                log.info("Connection to SQLite was closed successfully.");
            } else {
                log.warn("Connection is NULL and can`t be closed now.");
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    public boolean isPlayerMoving() {
        return isPlayerMovingUp || isPlayerMovingDown || isPlayerMovingRight || isPlayerMovingLeft;
    }

    public boolean isDataTranslatorAlive() {
        return netDataTranslator != null && netDataTranslator.isAlive();
    }

    /**
     * Транслятор сервера.
     * Не работает, когда выступаем в роли клиента?
     */
    public void startServerBroadcast(SocketService socketService) {
        if (netDataTranslator == null) {
            netDataTranslator = new Thread(() -> {
                while (!netDataTranslator.isInterrupted()) {
                    socketService.broadcast(buildNewDataPackage());

                    try {
                        Thread.sleep(Constants.NETWORK_DATA_TRANSLATE_DELAY);
                    } catch (InterruptedException e) {
                        log.warn("Прерывание потока бродкаста данных клиентам!");
                        Thread.currentThread().interrupt();
                    }
                }
            });
            netDataTranslator.start();
        } else if (!netDataTranslator.isAlive()) {
            netDataTranslator.start();
        } else {
            log.error("Нельзя повторно запустить ещё живой поток!");
        }
    }

    public void startClientBroadcast(SocketService socketService) {
        if (netDataTranslator == null) {
            netDataTranslator = new Thread(() -> {
                while (!netDataTranslator.isInterrupted()) {
                    socketService.toServer(buildNewDataPackage());

                    try {
                        Thread.sleep(Constants.NETWORK_DATA_TRANSLATE_DELAY);
                    } catch (InterruptedException e) {
                        log.warn("Прерывание потока отправки данных на сервер!");
                        Thread.currentThread().interrupt();
                    }
                }
            });
            netDataTranslator.start();
        } else if (!netDataTranslator.isAlive()) {
            netDataTranslator.start();
        } else {
            log.error("Нельзя повторно запустить ещё живой поток!");
        }
    }

    private ClientDataDTO buildNewDataPackage() {
        // собираем пакет данных для сервера и других игроков:
        return ClientDataDTO.builder()
                // ...
                .build();
    }

    public void stopBroadcast() {
        if (netDataTranslator != null && netDataTranslator.isAlive()) {
            try {
                netDataTranslator.interrupt();
                netDataTranslator.join(1_000);
            } catch (InterruptedException e) {
                netDataTranslator.interrupt();
            }
            log.info("Транслятор данных остановлен: {}", netDataTranslator.isAlive());
        }
    }
}
