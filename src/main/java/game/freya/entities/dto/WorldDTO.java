package game.freya.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.config.Constants;
import game.freya.enums.other.HardnessLevel;
import game.freya.interfaces.iWorld;
import game.freya.items.MockEnvironmentWithStorage;
import game.freya.items.prototypes.Environment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Builder
public class WorldDTO extends ComponentAdapter implements iWorld {
    private static final Random r = new Random(100);

    private static final String scobe = ")";

    private final Color textColor = new Color(58, 175, 217, 191);

    private final Color linesColor = new Color(47, 84, 3, 64);

    private final Color backColor = new Color(31, 31, 31);

    @Getter
    @Builder.Default
    private final Set<Environment> environments = HashSet.newHashSet(100);

    @Getter
    @Builder.Default
    private UUID uid = UUID.randomUUID();

    @Setter
    @Getter
    private UUID author;

    @Setter
    @Getter
    @Builder.Default
    private String title = "world_demo_" + r.nextInt(1000);

    @Setter
    @Getter
    @Builder.Default
    private boolean isNetAvailable = false;

    @Setter
    @Getter
    @Builder.Default
    private int passwordHash = 0;

    @Setter
    @Getter
    @Builder.Default
    private Dimension dimension = new Dimension(32, 32);

    @Setter
    @Getter
    @Builder.Default
    private HardnessLevel level = HardnessLevel.EASY;

    @Getter
    @Builder.Default
    private LocalDateTime createDate = LocalDateTime.now();

    @Getter
    @Builder.Default
    private boolean isLocalWorld = true;

    @Getter
    @Setter
    private String networkAddress;

    @Getter
    @JsonIgnore
    private VolatileImage gameMap;

    @JsonIgnore
    private Image icon;

    /**
     * Основной метод рисования карты игры.
     * Для оптимизации указывается вьюпорт холста, чтобы игнорировать скрытое "за кадром".
     *
     * @param v2D volatile image холста
     */
    @Override
    public void draw(Graphics2D v2D) throws AWTException {
//        if (canvas == null) {
//            log.error("Нельзя рисовать мир, пока canvas = null!");
//            return;
//        }
//        Rectangle camera = canvas.getViewPort().getBounds();

        // рисуем готовый кадр мира:
//        v2D.drawImage(repaintMap(camera),
//
//                0, 0,
//                canvas.getWidth(), canvas.getHeight(),
//
//                camera.x, camera.y,
//                camera.width, camera.height,
//
//                canvas);
    }

    @Override
    public void addEnvironment(Environment env) {
        this.environments.add(env);
    }

    @Override
    public HardnessLevel getHardnesslevel() {
        return this.level;
    }

    @Override
    public void generate() {
        for (int i = 0; i < 32; ) {
            MockEnvironmentWithStorage nextMock = new MockEnvironmentWithStorage("mock_" + (i + 1),
                    dimension.getWidth() * Constants.MAP_CELL_DIM, dimension.getHeight() * Constants.MAP_CELL_DIM);
            boolean isBusy = false;
            for (Environment environment : getEnvironments()) {
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

    private VolatileImage repaintMap(Rectangle camera) {
//        if (this.gameMap == null) {
//            this.gameMap = canvas.createVolatileImage(dimension.width * Constants.MAP_CELL_DIM,
//                    dimension.height * Constants.MAP_CELL_DIM, new ImageCapabilities(true));
//        }

//        Graphics2D v2D;
//        int valid = this.gameMap.validate(canvas.getGraphicsConfiguration());
//        while (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
//            this.gameMap = canvas.createVolatileImage(dimension.width * Constants.MAP_CELL_DIM,
//                    dimension.height * Constants.MAP_CELL_DIM, new ImageCapabilities(true));
//            valid = this.gameMap.validate(canvas.getGraphicsConfiguration());
//        }
//        if (valid == VolatileImage.IMAGE_RESTORED) {
//            v2D = this.gameMap.createGraphics();
//        } else {
//            v2D = (Graphics2D) this.gameMap.getGraphics();
//        }

//        v2D.setClip(0, 0, camera.width, camera.height);

//        v2D.setColor(backColor);
//        v2D.fillRect(0, 0, camera.width, camera.height);

//        int n = 1;
//        v2D.setStroke(new BasicStroke(2f));
//        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {
//
//            // draw numbers of rows and columns:
//            if (Constants.isDebugInfoVisible()) {
//                String ns = n + scobe;
//                v2D.setColor(textColor);
//                v2D.drawString(ns, i - 26, 12);
//                v2D.drawString(ns, i - 34, gameMap.getHeight() - 12);
//
//                v2D.drawString(ns, 6, i - 16);
//                v2D.drawString(ns, gameMap.getWidth() - 24, i - 26);
//            }
//
//            // draw map grid cells:
//            v2D.setColor(linesColor);
//            v2D.drawLine(i, 0, i, gameMap.getHeight());
//            v2D.drawLine(0, i, gameMap.getWidth(), i);
//
//            n++;
//        }

        // рисуем центральные оси:
//        if (Constants.isDebugInfoVisible()) {
//            v2D.setColor(Color.RED);
//            v2D.setStroke(new BasicStroke(2f));
//            v2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
//            v2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());
//        }

        // рисуем окружение на карте:
//        drawEnvironments(v2D, camera);

        // рисуем игроков из контроллера на карте:
//        gameController.drawHeroes(v2D, canvas);

//        v2D.dispose();

        // return drown result:
        return this.gameMap;
    }

    private void drawEnvironments(Graphics2D g2D, Rectangle visibleRect) {
        for (Environment env : environments) {
            if (env.isInSector(visibleRect)) {
                env.draw(g2D);
                if (Constants.isDebugInfoVisible()) {
                    g2D.setColor(Color.ORANGE);
                    g2D.draw(env.getCollider().getShape());
                }
            }
        }
    }

    public Image getIcon() {
        if (this.icon == null) {
            if (isNetAvailable) {
                this.icon = (BufferedImage) Constants.CACHE.get("net");
            } else {
                log.debug(Constants.getNotRealizedString());
            }
        }
        return this.icon;
    }
}
