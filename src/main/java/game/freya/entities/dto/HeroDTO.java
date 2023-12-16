package game.freya.entities.dto;

import game.freya.config.Constants;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.enums.other.MovingVector;
import game.freya.items.PlayedCharacter;
import game.freya.items.containers.Backpack;
import game.freya.items.prototypes.Storage;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class HeroDTO extends PlayedCharacter {
    @Setter
    @Getter
    private HeroCorpusType corpusType = HeroCorpusType.COMPACT;

    @Setter
    @Getter
    private HeroPeriferiaType periferiaType = HeroPeriferiaType.COMPACT;

    @Setter
    @Getter
    private short periferiaSize = 50;

    @Setter
    private UUID worldUid;

    @Getter
    @Setter
    private LocalDateTime lastPlayDate = LocalDateTime.now();

    private transient Storage inventory;

    @Setter
    private long inGameTime = 0;

    public HeroDTO(@NotNull UUID ownerUid, @NotNull UUID heroUid, @NotNull String heroName, short level, int curHealth, int curOil,
                   int maxHealth, int maxOil, float power, byte speed, MovingVector vector, long experience, String imageNameInCache,
                   LocalDateTime createDate, boolean isVisible
    ) {
        super(ownerUid, heroUid, heroName, level, curHealth, curOil, maxHealth, maxOil, power, speed, vector,
                experience, imageNameInCache, createDate, isVisible);
    }

    public BufferedImage getImage() {
        try (InputStream avatarResource = getClass().getResourceAsStream(Constants.DEFAULT_AVATAR_URL)) {
            if (avatarResource != null) {
                return ImageIO.read(avatarResource);
            }
            throw new IOException(Constants.DEFAULT_AVATAR_URL);
        } catch (IOException e) {
            log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }
    }

    public Storage getInventory() {
        if (this.inventory == null) {
            this.inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"),
                    getCharacterUid(), getLocation(), getSize(), "hero_backpack");
        }
        return this.inventory;
    }
}
