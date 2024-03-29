package game.freya.entities.dto;

import game.freya.config.Constants;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    @NotNull
    private final String email;

    @NotNull
    @Setter
    private String nickName;

    @NotNull
    @Setter
    private UUID uid;

    @Builder.Default
    private String avatarUrl = Constants.DEFAULT_AVATAR_URL;

    @Setter
    private UUID lastPlayedWorldUid;

    // custom fields:
    @Setter
    private BufferedImage avatar;

    @Getter
    @Setter
    private HeroDTO currentActiveHero;

    public BufferedImage getAvatar() {
        try (InputStream avatarResource = getClass().getResourceAsStream(Constants.DEFAULT_AVATAR_URL)) {
            if (avatarResource != null) {
                try {
                    return ImageIO.read(avatarResource);
                } catch (IOException ioe) {
                    log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(ioe));
                }
            }
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        } catch (IOException e) {
            throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, Constants.DEFAULT_AVATAR_URL);
        }
    }

    @Override
    public String toString() {
        return "Player{"
                + "uuid='" + uid + '\''
                + "nickName='" + nickName + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
