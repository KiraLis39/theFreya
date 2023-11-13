package game.freya.entities.dto;

import game.freya.entities.Hero;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    @NotNull
    private final String nickName;
    @NotNull
    private final String email;
    @NotNull
    @Setter
    private UUID uid;
    @Builder.Default
    private String avatarUrl = "/images/defaultAvatar.png";

    @Setter
    @Builder.Default
    private long inGameTime = 0;

    @Setter
    private UUID lastPlayedWorld;

    // custom fields:
    @Builder.Default
    private boolean isOnline = false;

    @Setter
    private BufferedImage avatar;

    @Getter
    private Set<Hero> heroes = HashSet.newHashSet(3);

    public PlayerDTO(UUID uid, String nickName, String email, String avatarUrl) {
        this.uid = uid;
        this.nickName = nickName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        try {
            this.avatar = this.avatarUrl == null ? null : ImageIO.read(new File(avatarUrl));
        } catch (IOException io) {
            log.error("Can`t read the player`s avatar by URL '{}'!", avatarUrl);
            this.avatar = null;
        }

        this.inGameTime = 0;
    }

    public BufferedImage getAvatar() {
        try (InputStream avatarResource = getClass().getResourceAsStream("/images/defaultAvatar.png")) {
            if (avatarResource != null) {
                try {
                    return ImageIO.read(avatarResource);
                } catch (IOException ioe) {
                    log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(ioe));
                }
            }
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        } catch (IOException e) {
            throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/defaultAvatar.png");
        }
    }

    public void setOnline(boolean online) {
        log.info("The Player '{}' is {} now!", getNickName(), online ? "ON-LINE" : "OFF-LINE");
        isOnline = online;
    }

    @Override
    public String toString() {
        return "Player{"
                + "nickName='" + nickName + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
