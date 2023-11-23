package game.freya.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fox.FoxRender;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.GameCanvas;
import game.freya.items.interfaces.iEnvironment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Builder
public class WorldDTO extends ComponentAdapter implements iWorld {
    private static final Random r = new Random(100);
    @Getter
    @Builder.Default
    private final Set<iEnvironment> environments = HashSet.newHashSet(30);
    @Getter
    private UUID uid;
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
    private int passwordHash = -1;
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
    // custom fields:
    @JsonIgnore
    private GameCanvas canvas;

    @Getter
    @JsonIgnore
    private BufferedImage gameMap;

    @JsonIgnore
    private Image icon;

    @JsonIgnore
    private GameController gameController;


    @Override
    public void init(GameCanvas canvas, GameController controller) {
        this.canvas = canvas;

        this.gameMap = new BufferedImage(
                dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM, BufferedImage.TYPE_INT_RGB);
        this.gameController = controller;

        log.info("World {} initialized successfully", getTitle());
    }

    /**
     * Основной метод рисования карты игры.
     * Для оптимизации указывается вьюпорт холста, чтобы игнорировать скрытое "за кадром".
     *
     * @param g2D графика холста
     */
    @Override
    public void draw(Graphics2D g2D) {
        Rectangle visibleRect = canvas.getViewPort().getBounds();
        // рисуем готовый кадр мира:
        g2D.drawImage(repaintMap(visibleRect), 0, 0, canvas.getWidth(), canvas.getHeight(),
                visibleRect.x, visibleRect.y,
                visibleRect.width, visibleRect.height,
                canvas);
    }

    private BufferedImage repaintMap(Rectangle visibleRect) {
        final Color textColor = new Color(1, 158, 217, 191);
        final Color linesColor = new Color(0, 105, 210, 64);
        final Color backColor = new Color(52, 2, 52);
        final String scobe = ")";

        // re-draw map:
        Graphics2D m2D = (Graphics2D) this.gameMap.getGraphics();
        m2D.clip(visibleRect);
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.MED,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

        m2D.setColor(backColor);
        m2D.fillRect(0, 0, gameMap.getWidth(), gameMap.getHeight());

        int n = 1;
        m2D.setStroke(new BasicStroke(2f));
        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {

            // draw numbers of rows and columns:
            if (Constants.isDebugInfoVisible()) {
                String ns = n + scobe;
                m2D.setColor(textColor);
                m2D.drawString(ns, i - 26, 12);
                m2D.drawString(ns, i - 34, gameMap.getHeight() - 12);

                m2D.drawString(ns, 6, i - 16);
                m2D.drawString(ns, gameMap.getWidth() - 24, i - 26);
            }

            // draw map grid cells:
            m2D.setColor(linesColor);
            m2D.drawLine(i, 0, i, gameMap.getHeight());
            m2D.drawLine(0, i, gameMap.getWidth(), i);

            n++;
        }

        if (Constants.isDebugInfoVisible()) {
            m2D.setColor(Color.RED);
            m2D.setStroke(new BasicStroke(2f));
            m2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
            m2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());
        }

        // рисуем окружение на карте:
        drawEnvironments(m2D, visibleRect);

        // рисуем игроков из контроллера на карте:
        gameController.drawHeroes(m2D, visibleRect, canvas);

        m2D.dispose();

        // return drown result:
        return this.gameMap;
    }

    private void drawEnvironments(Graphics2D g2D, Rectangle visibleRect) {
        for (iEnvironment environment : environments) {
            if (visibleRect.contains(environment.getPosition())) {
                environment.draw(g2D);
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
