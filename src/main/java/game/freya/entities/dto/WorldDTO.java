package game.freya.entities.dto;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.FoxCanvas;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Slf4j
@RequiredArgsConstructor
public class WorldDTO extends ComponentAdapter implements iWorld {

    private final UUID uid;

    private final String title;

    private final int passwordHash;

    private final Dimension dimension;

    private final HardnessLevel level;

    private final Map<String, PlayerDTO> players = HashMap.newHashMap(3);

    private FoxCanvas canvas;

    private VolatileImage gameMap;

    public WorldDTO(String title) {
        this(UUID.randomUUID(), title, HardnessLevel.EASY, new Dimension(64, 32), -1);
    }

    public WorldDTO(String title, HardnessLevel level, int passwordHash) {
        this(UUID.randomUUID(), title, level, new Dimension(64, 32), passwordHash);
    }

    public WorldDTO(UUID uid, String title, HardnessLevel level, Dimension dimension, int passwordHash) {
        this.uid = uid;
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void addPlayer(PlayerDTO playerDTO) {
        players.putIfAbsent(playerDTO.getNickName(), playerDTO);
    }

    @Override
    public void removePlayer(PlayerDTO playerDTO) {
        players.remove(playerDTO.getNickName());
    }

    @Override
    public void init(FoxCanvas canvas) {
        if (canvas != null) {
            this.canvas = canvas;
        }

        do {
            // validate the map image:
            if (this.gameMap == null || this.gameMap.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
                this.gameMap = this.canvas.createVolatileImage(
                        dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM);
            }

            // re-draw map:
            Graphics2D g2D = gameMap.createGraphics();
            g2D.setColor(new Color(52, 2, 52));
            g2D.fillRect(0, 0, gameMap.getWidth(), gameMap.getHeight());

            int n = 1;
            g2D.setStroke(new BasicStroke(2f));
            for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {
                g2D.setColor(new Color(1, 158, 217, 191));
                g2D.drawString(n + ")", i - 26, 12);
                g2D.drawString(n + ")", i - 34, gameMap.getHeight() - 12);

                g2D.drawString(n + ")", 6, i - 16);
                g2D.drawString(n + ")", gameMap.getWidth() - 24, i - 26);

                g2D.setColor(new Color(0, 105, 210, 64));
                g2D.drawLine(i, 0, i, gameMap.getHeight());
                g2D.drawLine(0, i, gameMap.getWidth(), i);

                n++;
            }

            g2D.setColor(Color.RED);
            g2D.setStroke(new BasicStroke(2f));
            g2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
            g2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());

            g2D.dispose();
        } while (this.gameMap.contentsLost());
    }

    /**
     * Основной метод рисования карты игры.
     * Здесь, на холсте gameMap, отрисовывается при каждом вызове данного метода всё перечисленное в методе.
     * Для оптимизации указывается ректангл вьюпорта холста, чтобы игнорировать скрытое "за кадром".
     *
     * @param g2D         графика холста
     * @param visibleRect отображаемый на холсте прямоугольник игрового мира.
     */
    @Override
    public void draw(Graphics2D g2D, Rectangle visibleRect) {
        if (this.gameMap == null) {
            init(this.canvas);
        }

        validateImage();

        Graphics2D mapGraphics2D = (Graphics2D) this.gameMap.getGraphics();
        Constants.RENDER.setRender(mapGraphics2D, FoxRender.RENDER.MED);

        // рисуем окружение на карте:
        drawEnvironment(mapGraphics2D, visibleRect);

        // рисуем игроков на карте:
        drawPlayers(mapGraphics2D, visibleRect);

        // рисуем готовый кадр карты:
        g2D.drawImage(this.gameMap, 0, 0, canvas.getWidth(), canvas.getHeight(),
                visibleRect.x, visibleRect.y,
                visibleRect.width, visibleRect.height,
                canvas);
    }

    private void drawEnvironment(Graphics2D g2D, Rectangle visibleRect) {

    }

    private void drawPlayers(Graphics2D g2D, Rectangle visibleRect) {
        players.values().forEach(player -> {
            if (player.isOnline() && visibleRect.contains(player.getPosition())) {
                player.draw(g2D);
            }
        });
    }

    private void validateImage() {
        if (this.gameMap.validate(Constants.getGraphicsConfiguration()) != VolatileImage.IMAGE_OK) {
            init(null);
        }
    }
}
