package game.freya.services;

import fox.FoxLogo;
import game.freya.WorldEngine;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.SceneController;
import game.freya.mappers.ClientDataMapper;
import game.freya.net.PingService;
import game.freya.utils.BcryptUtil;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class GameControllerService {
    private final ApplicationProperties applicationProperties;
    private final BcryptUtil bcryptUtil;
    private final GameConfigService gameConfigService;
    private final WorldEngine worldEngine;
    private final EventService eventService;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final WorldService worldService;
    private final PingService pingService;
    private final ClientDataMapper clientDataMapper;

    private SceneController gameFrameController;

    /**
     * Отсюда начинается выполнение основного кода игры.
     * В этом методе же вызывается метод, отображающий первое игровое окно - меню игры.
     */
    @Autowired
    public void startGameAndFirstUiShow(@Lazy SceneController gameFrameController) {
        this.gameFrameController = gameFrameController;

        setLookAndFeel();

        // показываем лого:
        showLogoIfEnabled();

        // продолжаем подготовку к запуску игры пока лого отображается...
        log.info("Check the current user in DB created...");

        // создаём если не было, и обновляем если ему сменили никнейм через конфиг:
        playerService.getCurrentPlayer()
                .setNickName(Constants.getUserConfig().getUserName());

        loadNecessaryResources();

        gameFrameController.showGameWindow();
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
//            UIManager.put("nimbusBase", new Color(...));
//            UIManager.put("nimbusBlueGrey", new Color(...));
//            UIManager.put("control", new Color(...));
//            UIManager.put("Button.font", FONT);
//            UIManager.put("Label.font", FONT);
//            UIManager.put("OptionPane.cancelButtonText", "nope");
//            UIManager.put("OptionPane.okButtonText", "yup");
//            UIManager.put("OptionPane.inputDialogTitle", "Введите свой никнейм:");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.warn("Couldn't get specified look and feel, for reason: {}", ExceptionUtils.getFullExceptionMessage(ex));
            }
        }
    }

    private void showLogoIfEnabled() {
        if (Constants.getGameConfig().isShowStartLogo()) {
            try (InputStream is = Constants.class.getResourceAsStream(Constants.getLogoImageUrl())) {
                if (is != null) {
                    Constants.setLogo(new FoxLogo());
                    Constants.getLogo().start(applicationProperties.getAppVersion(),
                            Constants.getUserConfig().isFullscreen() ? FoxLogo.IMAGE_STYLE.FILL : FoxLogo.IMAGE_STYLE.DEFAULT,
                            FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is));
                }
            } catch (IOException e) {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/logo.png");
            }
        }
    }

    private void loadNecessaryResources() {
        if (Files.notExists(Path.of(Constants.getGameConfig().getWorldsImagesDir()))) {
            try {
                Files.createDirectories(Path.of(Constants.getGameConfig().getWorldsImagesDir()));
            } catch (IOException e) {
                log.error("Не удалось создать директорию для миниатюр миров. Нет прав на папку игры? Так играть не выйдет.");
                throw new RuntimeException(e);
            }
        }

        try {
            URL necUrl = getClass().getResource("/images/necessary/");
            assert necUrl != null;
            Constants.CACHE.addAllFrom(necUrl);
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    public void exitTheGame(Duration duration, int errCode) {
        saveTheGame(duration);
        closeConnections();

        log.info("The game is finished with code {}!", errCode);
        System.exit(errCode);
    }

    public void saveTheGame(Duration duration) {
        log.info("Saving the game...");

        // сохраняем героя:
        if (characterService.getCurrentHero() != null) {
            characterService.getCurrentHero().setOnline(false);
            characterService.getCurrentHero().setInGameTime(duration == null ? 0 : duration.toMillis()); // or .getSeconds()
            characterService.saveCurrent();
        }

        // сохраняем мир:
        worldService.saveCurrent();

        // сохраняем игрока (и его UserConfig конфиг):
        playerService.saveCurrent();

        // сохранение GameConfig программы:
        gameConfigService.createOrSaveGameConfig();

        log.info("The game is saved.");
    }

    /**
     * Закрывает все активные сетевые подключения, соединения и закрывает Сервер и Сокет.
     */
    public void closeConnections() {
        // закрываем соединения:
        if (Constants.getLocalSocketConnection() != null && Constants.getLocalSocketConnection().isOpen()) {
            Constants.getLocalSocketConnection().setHandledExit(true);
            Constants.getLocalSocketConnection().close();
            log.info("Соединение с Сервером завершено.");
        }

        // закрываем Сервер:
        if (Constants.getServer() != null && Constants.getServer().isOpen()) {
            log.warn(closeServer() ? "Сервер успешно остановлен" : "Возникла ошибка при закрытии сервера.");
        }
    }

    private boolean closeServer() {
        Constants.getServer().close();
        Constants.getServer().untilClose(Constants.getGameConfig().getServerCloseTimeAwait());
        return Constants.getServer().isClosed();
    }
}
