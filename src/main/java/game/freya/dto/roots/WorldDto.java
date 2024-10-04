package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import game.freya.config.Constants;
import game.freya.dto.MockEnvironmentWithStorageDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.interfaces.root.iWorld;
import game.freya.services.GameControllerService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.VolatileImage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties({"scobe", "textColor", "linesColor", "backColor", "canvas", "gameController", "gameMap", "icon"})
public class WorldDto implements iWorld {
    @Getter
    @Builder.Default
    @Schema(description = "Список объектов мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final Set<EnvironmentDto> environments = HashSet.newHashSet(32);

    // custom fields:
    @JsonIgnore
    @Builder.Default
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private final Color textColor = new Color(58, 175, 217, 191);

    @JsonIgnore
    @Builder.Default
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private final Color linesColor = new Color(47, 84, 3, 64);

    @JsonIgnore
    @Builder.Default
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private final Color backColor = new Color(31, 31, 31);

    @Getter
    @Setter
    @Schema(description = "UUID мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @Getter
    @Builder.Default
    @Schema(description = "Имя мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name = "world_demo_" + new Random(System.currentTimeMillis()).nextInt(100);

    @Getter
    @Schema(description = "UUID владельца мира (директора, хозяина, игрока)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID ownerUid;

    @Getter
    @Setter
    @Schema(description = "UUID создателя мира", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID createdBy;

    @Getter
    @Builder.Default
    @Schema(description = "Размеры мира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Dimension size = new Dimension(128, 128);

    @Getter
    @Setter
    @Builder.Default
    @Schema(description = "Разрешены ли сетевые подключения?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isNetAvailable = false;

    @Getter
    @Setter
    @Schema(description = "Зашифрованный пароль", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String password; // bcrypt

    @Getter
    @Setter
    @Builder.Default
    @Schema(description = "Уровень сложности", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private HardnessLevel hardnessLevel = HardnessLevel.EASY;

    @Getter
    @Setter
    @Builder.Default
    @Schema(description = "Является ли локальным миром?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isLocal = true;

    @Getter
    @Setter
    @Schema(description = "Сетевой адрес мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String address;

    @Getter
    @Schema(description = "Путь к миниатюре мира (для игрового меню)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cacheKey;

    @Getter
    @Schema(description = "Дата создания мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdDate;

    @Getter
    @Schema(description = "Дата последнего изменения (последнего входа в мир)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime modifyDate;

    @Getter
    @Builder.Default
    @Schema(description = "Список игроков мира", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Set<CharacterDto> heroes = new LinkedHashSet<>(4);

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private GameControllerService gameControllerService;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private VolatileImage gameMap;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private Image icon;

    @Override
    public void init(GameControllerService gameControllerService) {
        this.gameControllerService = gameControllerService;
        log.info("World {} initialized successfully", getName());
    }

    /**
     * Основной метод рисования карты игры.
     * Для оптимизации указывается вьюпорт холста, чтобы игнорировать скрытое "за кадром".
     *
     * @param v2D volatile image холста
     */
    @Override
    public void draw(Graphics2D v2D) throws AWTException {

    }

    /**
     * Подготовка мира к сохранению в БД после его создания, как нового мира
     * например чере меню создания нового мира в игре.
     */
    @Override
    public void generate() {
        for (int i = 0; i < 32; ) {
            MockEnvironmentWithStorageDto nextMock = MockEnvironmentWithStorageDto.builder()
                    .name("mock_" + (i + 1))
                    .cacheKey("mock_0" + Constants.getRandom().nextInt(3))
                    .size(new Dimension(getSize().width * Constants.MAP_CELL_DIM, getSize().height * Constants.MAP_CELL_DIM))
                    .location(new Point2D.Double(Constants.getRandom().nextDouble() * getSize().width, Constants.getRandom().nextDouble() * getSize().height))
                    .createdBy(Constants.getUserConfig().getUserId())
                    .isVisible(true)
                    .build();
            boolean isBusy = false;
            for (EnvironmentDto environment : getEnvironments()) {
                if (environment.getCollider().intersects(nextMock.getCollider())) {
                    isBusy = true;
                    break;
                }
            }
            if (!isBusy) {
                i++;
                addEnvironment(nextMock);
            }
        }
    }

    @Override
    public void addEnvironment(EnvironmentDto env) {
        this.environments.add(env);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorldDto worldDto)) {
            return false;
        }
        return Objects.equals(uid, worldDto.uid) && Objects.equals(createdBy, worldDto.createdBy) && Objects.equals(createdDate, worldDto.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, createdBy);
    }

    @Override
    public String toString() {
        return "WorldDto{"
                + "uid=" + uid
                + ", name='" + name + '\''
                + ", ownerUid=" + ownerUid
                + ", createdBy=" + createdBy
                + ", size=" + size
                + ", isNetAvailable=" + isNetAvailable
                + ", password='" + password.substring(0, 4).concat("*") + '\''
                + ", hardnessLevel=" + hardnessLevel
                + ", isLocal=" + isLocal
                + ", address='" + address + '\''
                + ", cacheKey='" + cacheKey + '\''
                + ", createdDate=" + createdDate
                + ", modifyDate=" + modifyDate
                + ", playHeroes=" + heroes.size()
                + ", environments=" + environments.size()
                + '}';
    }
}
