package game.freya.services;

import game.freya.config.Constants;
import game.freya.dto.roots.PlayerDto;
import game.freya.entities.roots.Player;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.PlayerMapper;
import game.freya.repositories.PlayersRepository;
import game.freya.utils.ExceptionUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PlayerService {
    private final PlayersRepository playersRepository;
    private final PlayerMapper playerMapper;
    private final UserConfigService userConfigService;

    private PlayerDto currentPlayer; // Текущий игрок (сидящий за клавиатурой).

    public Player save(@NotNull Player player) {
        return playersRepository.saveAndFlush(player);
    }

    @Transactional(readOnly = true)
    public Optional<Player> findByUid(UUID userId) {
        return playersRepository.findByUid(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Player> findByMail(String userMail) {
        return playersRepository.findByEmailIgnoreCase(userMail);
    }

    public void saveCurrent() {
        try {
            userConfigService.createOrSaveUserConfig();
        } catch (Exception e) {
            log.error("Ошибка при сохранении конфига пользователя: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        Optional<Player> playerOpt = playersRepository.findByUid(currentPlayer.getUid());
        if (playerOpt.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.PLAYER_NOT_FOUND, currentPlayer.getNickName());
        }
        Player toUpdate = playerOpt.get();
        BeanUtils.copyProperties(currentPlayer, toUpdate, "uid");
        save(toUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlayerDto createPlayer() {
        UUID newPlayerUid = Objects.requireNonNullElse(Constants.getUserConfig().getUserId(), UUID.randomUUID());
        String newPlayerName = Objects.requireNonNullElse(Constants.getUserConfig().getUserName(), "NO_NAME_PLAYER");
        String newPlayerMail = Objects.requireNonNullElse(Constants.getUserConfig().getUserMail(), "no@mail.com");
        String newPlayerAvatar = Objects.requireNonNullElse(Constants.getUserConfig().getUserAvatar(), Constants.DEFAULT_AVATAR_URL);
        log.info("Создание нового пользователя '{} ({})' [{}]...", newPlayerName, newPlayerMail, newPlayerUid);
        PlayerDto newPlayer = PlayerDto.builder()
                .uid(newPlayerUid)
                .nickName(newPlayerName)
                .email(newPlayerMail)
                .build();
        try (InputStream ris = getClass().getResourceAsStream(newPlayerAvatar)) {
            assert ris != null;
            newPlayer.setAvatar(ImageIO.read(ris));
        } catch (Exception e) {
            log.error("Can`t set the avatar to player {} by url '{}'", newPlayer.getNickName(), newPlayerAvatar);
            newPlayer.setAvatar(null);
        }
        log.info("Новый пользователь {} успешно создан.", newPlayer.getNickName());
        return playerMapper.toDto(save(playerMapper.toEntity(newPlayer)));
    }

    public void setCurrentPlayerLastPlayedWorldUid(UUID uid) {
        if (this.currentPlayer.getLastPlayedWorldUid().equals(uid)) {
            return;
        }
        this.currentPlayer.setLastPlayedWorldUid(uid);
        this.currentPlayer = playerMapper.toDto(playersRepository.save(playerMapper.toEntity(currentPlayer)));
    }

    @Transactional
    public PlayerDto getCurrentPlayer() {
        if (currentPlayer == null) {
            Optional<Player> currentPlayerOpt = playersRepository.findByUid(Constants.getUserConfig().getUserId());
            if (currentPlayerOpt.isPresent()) {
                currentPlayer = playerMapper.toDto(currentPlayerOpt.get());
            } else {
                // если кто-то надумал сменить свой uid в конфиг-файле - мешаем, т.к. у настоящего другого игрока почта тоже будет иной:
                currentPlayerOpt = findByMail(Constants.getUserConfig().getUserMail());
                if (currentPlayerOpt.isPresent()) {
                    log.warn("Не найден в бд игрок по uid {}, но найден по почте {}.",
                            Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                    currentPlayer = playerMapper.toDto(currentPlayerOpt.get());
                    // возвращаем верный uid игрока в конфиг-файл:
                    Constants.getUserConfig().setUserId(currentPlayer.getUid());
                } else {
                    currentPlayer = createPlayer();
                    if (currentPlayer != null) {
                        log.warn("Не был найден в бд игрок с uid {}, или почтой {}. Создана новая запись.",
                                Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                    } else {
                        log.error("Игрок {} ({}) не был найден в БД ни по имени, ни по почте. Создание нового так же провалилось по неизвестной причине.",
                                Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                        throw new GlobalServiceException(ErrorMessages.PLAYER_NOT_FOUND, Constants.getUserConfig().getUserId().toString());
                    }
                }
            }
        }
        return currentPlayer;
    }
}
