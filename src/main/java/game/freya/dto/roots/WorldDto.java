package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.dto.MockEnvironmentWithStorageDto;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.GameCanvas;
import game.freya.interfaces.iWorld;
import game.freya.services.GameControllerService;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@Slf4j
@SuperBuilder
public non-sealed class WorldDto extends AbstractEntityDto implements iWorld { //  extends ComponentAdapter
    private static final Random r = new Random(100);

    private final char scobe = ')';

    private final Color textColor = new Color(58, 175, 217, 191);

    private final Color linesColor = new Color(47, 84, 3, 64);

    private final Color backColor = new Color(31, 31, 31);

    @Getter
    @Builder.Default
    private final Set<EnvironmentDto> environments = HashSet.newHashSet(128);

    @Getter
    @Setter
    @Builder.Default
    private String name = "world_demo_" + r.nextInt(1000);

    @Getter
    @Setter
    @Builder.Default
    private boolean isNetAvailable = false;

    @Getter
    @Setter
    @Builder.Default
    private int passwordHash = 0;

    @Getter
    @Setter
    @Builder.Default
    private HardnessLevel level = HardnessLevel.EASY;

    @Getter
    @Setter
    @Builder.Default
    private boolean isLocalWorld = true;

    @Getter
    @Setter
    private String networkAddress;

    // custom fields:
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Transient
    @JsonIgnore
    private GameCanvas canvas;

    @Transient
    @JsonIgnore
    private GameControllerService gameController;

    @Transient
    @JsonIgnore
    private VolatileImage gameMap;

    @Transient
    @JsonIgnore
    private Image icon;

    @Override
    public void init(GameCanvas canvas, GameControllerService controller) {
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

    @Override
    public void addEnvironment(EnvironmentDto env) {
        this.environments.add(env);
    }

    @Override
    public HardnessLevel getHardnesslevel() {
        return this.level;
    }

    @Override
    public void generate() {
        for (int i = 0; i < 32; ) {
            MockEnvironmentWithStorageDto nextMock = new MockEnvironmentWithStorageDto("mock_" + (i + 1),
                    getSize().width * Constants.MAP_CELL_DIM, getSize().height * Constants.MAP_CELL_DIM);
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
            if (Constants.isDebugInfoVisible()) {
                String ns = String.valueOf(n + scobe);
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
        if (Constants.isDebugInfoVisible()) {
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
                if (Constants.isDebugInfoVisible()) {
                    g2D.setColor(Color.ORANGE);
                    g2D.draw(env.getCollider());
                }
            }
        }
    }

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
    public int hashCode() {
        return Objects.hash(getUid(), getCreatedDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorldDto world = (WorldDto) o;
        return Objects.equals(getUid(), world.getUid()) && Objects.equals(getCreatedDate(), world.getCreatedDate());
    }
}
