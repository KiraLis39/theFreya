package game.freya.services;

import fox.FoxLogo;
import game.freya.WorldEngine;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.entities.PlayCharacter;
import game.freya.entities.roots.prototypes.Character;
import game.freya.enums.net.NetDataEvent;
import game.freya.enums.net.NetDataType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.GameWindowController;
import game.freya.net.PingService;
import game.freya.net.data.ClientDataDto;
import game.freya.net.data.events.EventHeroMoving;
import game.freya.net.data.events.EventHeroOffline;
import game.freya.net.data.events.EventHeroRegister;
import game.freya.utils.BcryptUtil;
import game.freya.utils.ExceptionUtils;
import jakarta.validation.constraints.NotNull;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class GameControllerService extends GameControllerBase {
    private final ApplicationProperties applicationProperties;
    private final BcryptUtil bcryptUtil;
    private final GameConfigService gameConfigService;
    private final WorldEngine worldEngine;
    private final EventService eventService;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final WorldService worldService;
    private final PingService pingService;

    private GameWindowController gameFrameController;

    /**
     * Отсюда начинается выполнение основного кода игры.
     * В этом методе же вызывается метод, отображающий первое игровое окно - меню игры.
     */
    @Autowired
    public void startGameAndFirstUiShow(@Lazy GameWindowController gameFrameController) {
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

        gameFrameController.showMainMenu(this, characterService);
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

    public List<PlayCharacterDto> getMyCurrentWorldHeroes() {
        return characterService.findAllByWorldUidAndOwnerUid(worldService.getCurrentWorld().getUid(), playerService.getCurrentPlayer().getUid());
    }

    public List<WorldDto> findAllWorldsByNetworkAvailable(boolean isNetworkAvailable) {
        return worldService.findAllByNetAvailable(isNetworkAvailable);
    }

    public void setCurrentWorld(UUID selectedWorldUuid) {
        Optional<WorldDto> selected = worldService.findByUid(selectedWorldUuid);
        if (selected.isPresent()) {
            playerService.getCurrentPlayer().setLastPlayedWorldUid(selectedWorldUuid);
            worldService.setCurrentWorld(selected.get());
            return;
        }
        throw new GlobalServiceException(ErrorMessages.WORLD_NOT_FOUND, selectedWorldUuid.toString());
    }

    /**
     * В этот метод приходят данные обновлений сетевого мира (Сервера).
     * Здесь собираются все изменения, движения игроков, атаки, лечения, взаимодействия и т.п. для
     * мержа с мирами других сетевых участников.
     *
     * @param data модель обновлений для сетевого мира от другого участника игры.
     */
    public void syncServerDataWithCurrentWorld(@NotNull ClientDataDto data) {
        log.debug("Получены данные для синхронизации {} игрока {} (герой {})",
                data.dataEvent(), data.content().ownerUid(), data.content().heroUid());

        Optional<PlayCharacter> aimOpt = characterService.findByUid(data.content().heroUid());
        if (aimOpt.isEmpty()) {
            log.warn("Герой {} не существует в БД. Отправляется запрос на его модель к Серверу, ожидается...", data.content().heroUid());
            requestHeroFromServer(data.content().heroUid());
            return;
        }
        Character aim = aimOpt.get();

        if (data.dataEvent() == NetDataEvent.HERO_OFFLINE) {
            EventHeroOffline event = (EventHeroOffline) data.content();
            UUID offlinePlayerUid = event.ownerUid();
            log.info("Игрок {} отключился от Сервера. Удаляем его из карты активных Героев...", offlinePlayerUid);
            offlineSaveAndRemoveOtherHeroByPlayerUid(offlinePlayerUid);
        }

        if (data.dataEvent() == NetDataEvent.HERO_MOVING) {
            EventHeroMoving event = (EventHeroMoving) data.content();
            aim.setLocation(event.location());
            aim.setVector(event.vector());
        }

        // Обновляем здоровье, максимальное здоровье, силу, бафы-дебафы, текущий инструмент в руках и т.п. другого игрока:
        // ...

        // Обновляем окружение, выросшие-срубленные деревья, снесенные, построенные постройки, их характеристики и т.п.:
        // ...

        // Обновляем данные квестов, задач, групп, союзов, обменов и т.п.:
        // ...

        // Обновляем статусы он-лайн, ветхость, таймауты и прочее...
        // ...
    }

    public void offlineSaveAndRemoveOtherHeroByPlayerUid(UUID clientUid) {
        Optional<CharacterDto> charDtoOpt = characterService.getByUid(clientUid);
        if (charDtoOpt.isPresent()) {
            PlayCharacterDto charDto = (PlayCharacterDto) charDtoOpt.get();
            charDto.setOnline(false);
            characterService.justSaveAnyHero(charDto);
        }
    }

    public void requestHeroFromServer(UUID uid) {
        Constants.getLocalSocketConnection().toServer(ClientDataDto.builder()
                .dataType(NetDataType.HERO_REMOTE_NEED)
                .content(EventHeroRegister.builder().heroUid(uid).build())
                .build());
    }
}
