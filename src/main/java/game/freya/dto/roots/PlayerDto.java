package game.freya.dto.roots;

import game.freya.config.Constants;
import game.freya.utils.ExceptionUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    @NotNull
    private final String email;

    @NotNull
    @Setter
    private UUID uid;

    @NotNull
    @Setter
    private String nickName;

    @Builder.Default
    private String avatarUrl = Constants.getUserConfig().getUserAvatar();

    @Setter
    private UUID lastPlayedWorldUid;


    // custom fields:
    @Setter
    private BufferedImage avatar;

    @Getter
    @Setter
    private CharacterDto currentActiveHero;

    public BufferedImage getAvatar() {
        if (avatar == null) {
            if (avatarUrl == null) {
                avatarUrl = Constants.getUserConfig().getUserAvatar();
            }
            try (InputStream avatarResource = getClass().getResourceAsStream(avatarUrl)) {
                if (avatarResource != null) {
                    avatar = ImageIO.read(avatarResource);
                }
            } catch (Exception e) {
                log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(e));
                avatar = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            }
        }
        return avatar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getNickName(), getEmail());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerDto player = (PlayerDto) o;
        return Objects.equals(getUid(), player.getUid())
                && Objects.equals(getNickName(), player.getNickName()) && Objects.equals(getEmail(), player.getEmail());
    }

    @Override
    public String toString() {
        return "PlayerDto{"
                + "uid='" + uid + '\''
                + "nickName='" + nickName + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
