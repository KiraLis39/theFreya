package game.freya.services;

import com.jme3.system.AppSettings;
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
import java.awt.image.BufferedImage;
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
    private final ApplicationProperties props;
    private final BcryptUtil bcryptUtil;
    private final GameConfigService gameConfigService;
    private final WorldEngine worldEngine;
    private final EventService eventService;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final WorldService worldService;
    private final PingService pingService;
    private final ClientDataMapper clientDataMapper;

    private SceneController sceneController;

    /**
     * Отсюда начинается выполнение основного кода игры.
     * В этом методе же вызывается метод, отображающий первое игровое окно - меню игры.
     */
    @Autowired
    public void startGameAndFirstUiShow(@Lazy SceneController sceneController) {
        this.sceneController = sceneController;

        setLookAndFeel();

        // показываем лого:
        showLogoIfEnabled();

        // продолжаем подготовку к запуску игры пока лого отображается...
        gameConfigService.load();

        // создаём если не было, и обновляем если ему сменили никнейм через конфиг:
        playerService.getCurrentPlayer()
                .setNickName(Constants.getUserConfig().getUserName());

        loadNecessaryResources();

        // настраиваем JME:
        applyJmeSettings();

        sceneController.showGameWindow(applyJmeSettings());
    }

    private AppSettings applyJmeSettings() {
        AppSettings settings = new AppSettings(true);

        settings.setTitle(props.getAppName().concat(" v.").concat(props.getAppVersion()));

        settings.setVSync(Constants.getUserConfig().isUseVSync());
        settings.setFrequency(Constants.getUserConfig().getFpsLimit());
        settings.setFrameRate(Constants.getUserConfig().getFpsLimit());

//        settings.setMinResolution(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight());
//        settings.setResolution(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight());

        settings.setMinWidth(Constants.getUserConfig().getWindowWidth());
        settings.setMinHeight(Constants.getUserConfig().getWindowHeight());
        settings.setWindowSize(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight());

        settings.setResizable(Constants.getGameConfig().isGameWindowResizable());
        settings.setCenterWindow(true);
//        settings.setWindowXPosition();
//        settings.setWindowYPosition();

//        settings.setFullscreen(Constants.getUserConfig().isFullscreen());

//        settings.setBitsPerPixel(32); // 1 bpp = черно-белый, 2 bpp = серый, 4 bpp = 16 цветов, 8 bpp = 256 цветов, 24 или 32 bpp = «truecolor».
//        settings.setAlphaBits();

        /*
            Точность буфера глубины.
            Увеличить точность - 32 бита
            Уменьшить точность - 16 бит
            На некоторых платформах 24 бита могут не поддерживаться, в этом случае - 16 бит
         */
//        settings.setDepthBits();

        /*
            требует аппаратной поддержки со стороны драйвера графического процессора.
            См. {@link Quad Buffering [http://en.wikipedia.org/wiki/Quad_buffering]}
            Видеокарты AMD Radeon HD 6000 Series и более новые поддерживают.
            Стандарты 3D, такие как OpenGL и Direct3D, поддерживают.
         */
        settings.setStereo3D(Constants.getUserConfig().isUseStereo3D());
        settings.setGammaCorrection(Constants.getUserConfig().isUseGammaCorrection());
        settings.setSwapBuffers(Constants.getGameConfig().isUseSwapBuffers());

        settings.setEmulateKeyboard(false);
        settings.setEmulateMouse(false); // для устройств с сенсорным экраном
//        settings.setEmulateMouseFlipAxis(Constants.getUserConfig().isXFlipped(), Constants.getUserConfig().isYFlipped()); // для эмулируемой мыши

//        settings.setGraphicsDebug();
//        settings.setGraphicsDebug();
//        settings.setGraphicsTrace();
//        settings.setGraphicsTiming();

//        settings.setOpenCLSupport();
//        settings.setOpenCLPlatformChooser();

//        settings.setCustomRenderer();
        settings.setAudioRenderer(AppSettings.LWJGL_OPENAL);
        settings.setRenderer(AppSettings.LWJGL_OPENGL45);

        settings.setIcons(new BufferedImage[]{
                Constants.CACHE.getBufferedImage("icon128"),
                Constants.CACHE.getBufferedImage("icon64"),
                Constants.CACHE.getBufferedImage("icon32"),
                Constants.CACHE.getBufferedImage("icon16"),
        });

        /*
            мультисэмплинг 0 - отключить сглаживание (резкие края, более быстрая обработка).
            мультисэмплинг 2 или 4 - активировать сглаживание (более мягкие края, медленнее).
            мультисэмплинг 8, 16, 32...
         */
        settings.setSamples(Constants.getUserConfig().getMultiSamplingLevel());

//        settings.setUseRetinaFrameBuffer();


        /*
            Укажите 8, чтобы указать 8-битный буфер трафарета,
            укажите 0, чтобы отключить буфер трафарета.
         */
//        settings.setStencilBits();

        settings.setUseInput(true); // реагировать ли на клавиатуру и мышь
        settings.setUseJoysticks(false); // Активировать дополнительную поддержку джойстика

        settings.setSettingsDialogImage("images/necessary/menu.png");

        return settings;
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
            // UIManager.put("FileChooser.saveButtonText", "Сохранить");
            // UIManager.put("FileChooser.cancelButtonText", "Отмена");
            // UIManager.put("FileChooser.openButtonText", "Выбрать");
            // UIManager.put("FileChooser.fileNameLabelText", "Наименование файла");
            // UIManager.put("FileChooser.filesOfTypeLabelText", "Типы файлов");
            // UIManager.put("FileChooser.lookInLabelText", "Директория");
            // UIManager.put("FileChooser.saveInLabelText", "Сохранить в директории");
            // UIManager.put("FileChooser.folderNameLabelText", "Путь директории");
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
            try (InputStream is = Constants.class.getResourceAsStream(Constants.getLogoImageUrl());
                 InputStream is2 = Constants.class.getResourceAsStream(Constants.getLogoImageUrl2())
            ) {
                if (is != null && is2 != null) {
                    Constants.setLogo(new FoxLogo());
                    Constants.getLogo().start(props.getAppVersion(),
                            Constants.getUserConfig().isFullscreen() ? FoxLogo.IMAGE_STYLE.FILL : FoxLogo.IMAGE_STYLE.DEFAULT,
                            FoxLogo.BACK_STYLE.PICK, KeyEvent.VK_ESCAPE, ImageIO.read(is), ImageIO.read(is2));
                }
            } catch (IOException e) {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/logo.png or /images/logo2.png");
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

//        try {
//            ModsLoaderEngine.stopMods();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (Constants.getGameWindow() != null) {
            Constants.getGameWindow().stop();
        }

        Constants.getMusicPlayer().stop();
        Constants.getSoundPlayer().stop();
        Constants.getBackgPlayer().stop();
        Constants.getVoicePlayer().stop();

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
