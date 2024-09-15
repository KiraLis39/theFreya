package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import game.freya.config.Constants;
import game.freya.dto.MockEnvironmentWithStorageDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.GamePaneRunnable;
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
import java.awt.geom.Rectangle2D;
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

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private GamePaneRunnable canvas;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private GameControllerService gameController;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private VolatileImage gameMap;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private Image icon;

    @Override
    public void init(GamePaneRunnable canvas, GameControllerService controller) {
        this.canvas = canvas;
        this.gameController = controller;
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
        if (canvas == null) {
            log.error("Нельзя рисовать мир, пока canvas = null!");
            return;
        }
        Rectangle2D.Double camera = (Rectangle2D.Double) canvas.getViewPort();

        // рисуем готовый кадр мира:
        Rectangle bounds = camera.getBounds();
        v2D.drawImage(repaintMap(camera),
                0, 0,
                canvas.getWidth(), canvas.getHeight(),

                bounds.x, bounds.y,
                bounds.width, bounds.height,

                canvas);
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
                    .cacheKey("mock_0" + Constants.RANDOM.nextInt(3))
                    .size(new Dimension(getSize().width * Constants.MAP_CELL_DIM, getSize().height * Constants.MAP_CELL_DIM))
                    .createdBy(Constants.getUserConfig().getUserId())
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

    @JsonIgnore
    private VolatileImage repaintMap(Rectangle2D.Double camera) throws AWTException {
        if (this.gameMap == null) {
            this.gameMap = canvas.createVolatileImage(getSize().width * Constants.MAP_CELL_DIM,
                    getSize().height * Constants.MAP_CELL_DIM, new ImageCapabilities(true));
        }

        Graphics2D v2D;
        int valid = this.gameMap.validate(canvas.getGraphicsConfiguration());
        while (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
            this.gameMap = canvas.createVolatileImage(getSize().width * Constants.MAP_CELL_DIM,
                    getSize().height * Constants.MAP_CELL_DIM, new ImageCapabilities(true));
            valid = this.gameMap.validate(canvas.getGraphicsConfiguration());
        }
        if (valid == VolatileImage.IMAGE_RESTORED) {
            v2D = this.gameMap.createGraphics();
        } else {
            v2D = (Graphics2D) this.gameMap.getGraphics();
        }

        v2D.setClip(0, 0, (int) camera.getWidth(), (int) camera.getHeight());

        v2D.setColor(backColor);
        v2D.fillRect(0, 0, (int) camera.getWidth(), (int) camera.getHeight());

        int n = 1;
        v2D.setStroke(new BasicStroke(2f));
        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {

            // draw numbers of rows and columns:
            if (Constants.getGameConfig().isDebugInfoVisible()) {
                String ns = String.valueOf(n + Constants.SCOBE);
                v2D.setColor(textColor);
                v2D.drawString(ns, i - 26, 12);
                v2D.drawString(ns, i - 34, gameMap.getHeight() - 12);

                v2D.drawString(ns, 6, i - 16);
                v2D.drawString(ns, gameMap.getWidth() - 24, i - 26);
            }

            // draw map grid cells:
            v2D.setColor(linesColor);
            v2D.drawLine(i, 0, i, gameMap.getHeight());
            v2D.drawLine(0, i, gameMap.getWidth(), i);

            n++;
        }

        // рисуем центральные оси:
        if (Constants.getGameConfig().isDebugInfoVisible()) {
            v2D.setColor(Color.RED);
            v2D.setStroke(new BasicStroke(2f));
            v2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
            v2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());
        }

        // рисуем окружение на карте:
        drawEnvironments(v2D, camera);

        // рисуем игроков из контроллера на карте:
        gameController.drawHeroes(v2D, canvas);

        v2D.dispose();

        // return drown result:
        return this.gameMap;
    }

    private void drawEnvironments(Graphics2D g2D, Rectangle2D.Double visibleRect) {
        for (EnvironmentDto env : environments) {
            if (env.isInSector(visibleRect)) {
                env.draw(g2D);
                if (Constants.getGameConfig().isDebugInfoVisible()) {
                    g2D.setColor(Color.ORANGE);
                    g2D.draw(env.getCollider());
                }
            }
        }
    }

    @JsonIgnore
    public Image getIcon() {
        if (this.icon == null) {
            if (isNetAvailable) {
                this.icon = Constants.CACHE.getBufferedImage("net");
            } else {
                log.debug(Constants.getNotRealizedString());
            }
        }
        return this.icon;
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
