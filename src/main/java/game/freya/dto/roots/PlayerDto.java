package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.interfaces.root.iPlayer;
import game.freya.utils.ExceptionUtils;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDto implements iPlayer {
    @NotNull
    @Schema(description = "Почтовый адрес игрока", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @NotNull
    @Schema(description = "Почтовый адрес игрока", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickName;

    @NotNull
    @Schema(description = "Почтовый адрес игрока", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private final String email;

    @Builder.Default
    @Schema(description = "Почтовый адрес игрока", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String avatarUrl = Constants.getUserConfig().getUserAvatar();

    @Schema(description = "Почтовый адрес игрока", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID lastPlayedWorldUid;

    // custom fields:
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY, hidden = true)
    private BufferedImage avatar;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true)
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
