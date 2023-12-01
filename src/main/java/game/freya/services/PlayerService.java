package game.freya.services;

import game.freya.config.Constants;
import game.freya.entities.Player;
import game.freya.entities.dto.PlayerDTO;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.PlayerMapper;
import game.freya.repositories.PlayersRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlayerService {
    private final PlayersRepository playersRepository;
    private final PlayerMapper playerMapper;
    private final UserConfigService userConfigService;

    @Getter
    private PlayerDTO currentPlayer;

    @Transactional
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = playerMapper.toDto(player);
    }

    public Player save(Player player) {
        if (player != null) {
            return playersRepository.save(player);
        }
        throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "player entity in PlayerService:save()");
    }

    @Transactional(readOnly = true)
    public Optional<Player> findByUid(UUID userId) {
        return playersRepository.findByUid(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Player> findByMail(String userMail) {
        return playersRepository.findByEmailIgnoreCase(userMail);
    }

    @Transactional
    public void updateCurrentPlayer() {
        userConfigService.save();

        Optional<Player> playerToUpdate = playersRepository.findByUid(currentPlayer.getUid());
        if (playerToUpdate.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.PLAYER_NOT_FOUND, currentPlayer.getNickName());
        }
        Player pl = playerToUpdate.get();
        BeanUtils.copyProperties(currentPlayer, pl, "id");
        save(pl);
    }

    @Transactional
    public Player createPlayer() {
        PlayerDTO newPlayer = PlayerDTO.builder()
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
        return save(playerMapper.toEntity(newPlayer));
    }

    public void setCurrentPlayerLastPlayedWorldUid(UUID uid) {
        if (this.currentPlayer.getLastPlayedWorldUid().equals(uid)) {
            return;
        }
        this.currentPlayer.setLastPlayedWorldUid(uid);
        this.currentPlayer = playerMapper.toDto(playersRepository.save(playerMapper.toEntity(currentPlayer)));
    }
}
