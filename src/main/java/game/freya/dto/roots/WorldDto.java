package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.dto.MockEnvironmentWithStorageDto;
import game.freya.dto.PlayCharacterDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.enums.player.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.GamePaneRunnable;
import game.freya.interfaces.root.iWorld;
import game.freya.services.GameControllerService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
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
    private GameControllerService gameControllerService;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private VolatileImage gameMap;

    @JsonIgnore
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true, nullable = true)
    private Image icon;

    @Override
    public void init(GamePaneRunnable canvas, GameControllerService gameControllerService) {
        this.canvas = canvas;
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
        for (int i = 0; i < 32;) {
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
        drawHeroes(v2D, canvas);

        v2D.dispose();

        // return drown result:
        return this.gameMap;
    }

    /**
     * Метод отрисовки всех героев подключенных к миру и авторизованных игроков.
     *
     * @param v2D    хост для отрисовки.
     * @param canvas класс холста.
     */
    public void drawHeroes(Graphics2D v2D, GamePaneRunnable canvas) {
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) { // если игра по сети:
            for (PlayCharacterDto hero : Constants.getServer().getAcceptedHeroes()) {
                if (gameControllerService.getCharacterService().getCurrentHero().equals(hero)) {
                    // если это текущий герой:
//                    if (!Controls.isPaused()) {
//                        moveHeroIfAvailable(canvas); // узкое место!
//                    }
                    hero.draw(v2D);
                } else if (canvas.getViewPort().getBounds().contains(hero.getLocation())) {
                    // если чужой герой в пределах видимости:
                    hero.draw(v2D);
                }
            }
        } else { // если не-сетевая игра:
//            if (!Controls.isPaused()) {
//                moveHeroIfAvailable(canvas); // узкое место!
//            }

            if (gameControllerService.getCharacterService().getCurrentHero() == null) {
                log.info("Потеряли текущего игрока. Что-то случилось? Выходим...");
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Окно игры не смогло получить текущего игрока для отрисовки");
            }
            gameControllerService.getCharacterService().getCurrentHero().draw(v2D);
        }
    }

    private void moveHeroIfAvailable(GamePaneRunnable canvas) {
        if (Controls.isPlayerMoving()) {
            Rectangle visibleRect = canvas.getViewPort().getBounds();
            MovingVector vector = gameControllerService.getCharacterService().getCurrentHero().getVector();
            Point2D.Double plLocation = gameControllerService.getCharacterService().getCurrentHero().getLocation();

            double hrc = (visibleRect.x + ((visibleRect.getWidth() - visibleRect.x) / 2d));
            boolean isViewMovableX = plLocation.x > hrc - 30 && plLocation.x < hrc + 30;

            double vrc = (visibleRect.y + ((visibleRect.getHeight() - visibleRect.y) / 2d));
            boolean isViewMovableY = plLocation.y > vrc - 30 && plLocation.y < vrc + 30;

            if (Controls.isPlayerMovingUp()) {
                vector = Controls.isPlayerMovingRight() ? MovingVector.UP_RIGHT : Controls.isPlayerMovingLeft() ? MovingVector.LEFT_UP : MovingVector.UP;
            } else if (Controls.isPlayerMovingDown()) {
                vector = Controls.isPlayerMovingRight() ? MovingVector.RIGHT_DOWN : Controls.isPlayerMovingLeft() ? MovingVector.DOWN_LEFT : MovingVector.DOWN;
            }

            if (Controls.isPlayerMovingRight()) {
                vector = Controls.isPlayerMovingUp() ? MovingVector.UP_RIGHT : Controls.isPlayerMovingDown() ? MovingVector.RIGHT_DOWN : MovingVector.RIGHT;
            } else if (Controls.isPlayerMovingLeft()) {
                vector = Controls.isPlayerMovingUp() ? MovingVector.LEFT_UP : Controls.isPlayerMovingDown() ? MovingVector.DOWN_LEFT : MovingVector.LEFT;
            }

            // перемещаем камеру к ГГ:
            if (!visibleRect.contains(gameControllerService.getCharacterService().getCurrentHero().getLocation())) {
                canvas.moveViewToPlayer(0, 0);
            }

            // move hero:
            int[] collisionMarker = hasCollision();
            gameControllerService.getCharacterService().getCurrentHero().setVector(vector.mod(vector, collisionMarker));
            if (!gameControllerService.getCharacterService().getCurrentHero().getVector().equals(MovingVector.NONE)) {
                // двигаемся по направлению вектора (взгляда):
                for (int i = 0; i < gameControllerService.getCharacterService().getCurrentHero().getSpeed(); i++) {
                    gameControllerService.getCharacterService().getCurrentHero().move();
                }
            } else {
                // тогда, стоя на месте, просто указываем направление вектора (взгляда):
                gameControllerService.getCharacterService().getCurrentHero().setVector(vector);
            }

            // send moving data to Server:
            Constants.getLocalSocketConnection()
                    .sendPacket(gameControllerService.getEventService().buildMove(gameControllerService.getCharacterService().getCurrentHero()));

            @Min(0) @Max(18) byte chs = gameControllerService.getCharacterService().getCurrentHero().getSpeed();

            // move map:
            switch (vector) {
                case UP -> {
                    if (isViewMovableY) {
                        canvas.dragDown(chs);
                    }
                }
                case UP_RIGHT -> {
                    if (isViewMovableY) {
                        canvas.dragDown(chs);
                    }
                    if (isViewMovableX) {
                        canvas.dragLeft(chs);
                    }
                }
                case RIGHT -> {
                    if (isViewMovableX) {
                        canvas.dragLeft(chs);
                    }
                }
                case RIGHT_DOWN -> {
                    if (isViewMovableX) {
                        canvas.dragLeft(chs);
                    }
                    if (isViewMovableY) {
                        canvas.dragUp(chs);
                    }
                }
                case DOWN -> {
                    if (isViewMovableY) {
                        canvas.dragUp(chs);
                    }
                }
                case DOWN_LEFT -> {
                    if (isViewMovableY) {
                        canvas.dragUp(chs);
                    }
                    if (isViewMovableX) {
                        canvas.dragRight(chs);
                    }
                }
                case LEFT -> {
                    if (isViewMovableX) {
                        canvas.dragRight(chs);
                    }
                }
                case LEFT_UP -> {
                    if (isViewMovableX) {
                        canvas.dragRight(chs);
                    }
                    if (isViewMovableY) {
                        canvas.dragDown(chs);
                    }
                }
                default -> log.info("Обнаружено несанкционированное направление {}", vector);
            }
        }
    }

    private int[] hasCollision() {
        // если сущность - не призрак:
        if (gameControllerService.getCharacterService().getCurrentHero().hasCollision()) {
            Rectangle2D.Double heroCollider = gameControllerService.getCharacterService().getCurrentHero().getCollider();

            // проверка коллизии с краем мира:
            Area worldMapBorder = new Area(new Rectangle(-12, -12,
                    gameControllerService.getWorldEngine().getGameMap().getWidth() + 24,
                    gameControllerService.getWorldEngine().getGameMap().getHeight() + 24));
            worldMapBorder.subtract(new Area(new Rectangle(0, 0, gameControllerService.getWorldEngine().getGameMap().getWidth(),
                    gameControllerService.getWorldEngine().getGameMap().getHeight())));
            if (worldMapBorder.intersects(heroCollider)) {
                return findVectorCorrection(worldMapBorder, heroCollider);
            }

            // проверка коллизий с объектами:
            for (EnvironmentDto env : gameControllerService.getWorldService().getCurrentWorld().getEnvironments()) {
                if (env.hasCollision() && env.getCollider().intersects(heroCollider)) {
                    return findVectorCorrection(env.getCollider(), heroCollider);
                }
            }
        }

        return new int[]{0, 0};
    }

    private int[] findVectorCorrection(Shape envColl, Rectangle2D.Double heroColl) {
        int[] result; // y, x
        Rectangle heroBounds = heroColl.getBounds();

        final Point upDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroBounds.y);
        final Point upDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroBounds.y);
        final Point downDotY01 = new Point((int) (heroColl.x + heroColl.width * 0.33d), heroBounds.y + heroBounds.height);
        final Point downDotY02 = new Point((int) (heroColl.x + heroColl.width * 0.66d), heroBounds.y + heroBounds.height);

        final Point leftDotX01 = new Point(heroBounds.x, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point leftDotX02 = new Point(heroBounds.x, (int) (heroColl.y + heroColl.height * 0.66d));
        final Point rightDotX01 = new Point(heroBounds.x + heroBounds.width, (int) (heroColl.y + heroColl.height * 0.33d));
        final Point rightDotX02 = new Point(heroBounds.x + heroBounds.width, (int) (heroColl.y + heroColl.height * 0.66d));

        result = new int[]{
                // y
                envColl.contains(upDotY01) || envColl.contains(upDotY02) ? -1
                        : envColl.contains(downDotY01) || envColl.contains(downDotY02) ? 1 : 0,

                // x
                envColl.contains(leftDotX01) || envColl.contains(leftDotX02) ? -1
                        : envColl.contains(rightDotX01) || envColl.contains(rightDotX02) ? 1 : 0
        };
        return result;
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
