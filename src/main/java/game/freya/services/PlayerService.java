package game.freya.services;

import game.freya.config.Constants;
import game.freya.dto.PlayerDto;
import game.freya.entities.Player;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.PlayerMapper;
import game.freya.repositories.PlayersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
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
    private PlayerDto currentPlayer;

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

    //    @Transactional
    public void updateCurrentPlayer() {
        userConfigService.save();

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
        log.info("Создание нового пользователя {}...", Constants.getUserConfig().getUserName());
        PlayerDto newPlayer = PlayerDto.builder()
                .uid(Constants.getUserConfig().getUserId())
                .nickName(Constants.getUserConfig().getUserName())
                .email(Constants.getUserConfig().getUserMail())
                .build();
        try (InputStream ris = getClass().getResourceAsStream(Constants.DEFAULT_AVATAR_URL)) {
            if (ris != null) {
                newPlayer.setAvatar(ImageIO.read(ris));
            } else {
                throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, Constants.DEFAULT_AVATAR_URL);
            }
        } catch (IOException e) {
            log.error("Can`t set the avatar to player {} by url '{}'", newPlayer.getNickName(), Constants.DEFAULT_AVATAR_URL);
            throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, Constants.DEFAULT_AVATAR_URL);
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

    @Transactional(readOnly = true)
    public PlayerDto getCurrentPlayer() {
        if (currentPlayer == null) {
            Optional<Player> currentPlayerOpt = playersRepository.findByUid(Constants.getUserConfig().getUserId());
            if (currentPlayerOpt.isPresent()) {
                currentPlayer = playerMapper.toDto(currentPlayerOpt.get());
            } else {
                currentPlayerOpt = findByMail(Constants.getUserConfig().getUserMail());
                if (currentPlayerOpt.isPresent()) {
                    log.warn("Не был найден в базе данных игрок по uid {}, но он найден по почте {}.",
                            Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                    currentPlayer = playerMapper.toDto(currentPlayerOpt.get());
                    Constants.getUserConfig().setUserId(currentPlayer.getUid());
                } else {
                    currentPlayer = createPlayer();
                    if (currentPlayer != null) {
                        log.warn("Не был найден в бд игрок с uid {}, или почтой {}. Создана новая запись.",
                                Constants.getUserConfig().getUserId(), Constants.getUserConfig().getUserMail());
                    } else {
                        throw new GlobalServiceException(ErrorMessages.PLAYER_NOT_FOUND, Constants.getUserConfig().getUserId().toString());
                    }
                }
            }
        }
        return currentPlayer;
    }
}
