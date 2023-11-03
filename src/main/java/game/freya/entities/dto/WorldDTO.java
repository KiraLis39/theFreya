package game.freya.entities.dto;

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
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
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

    private final Map<String, PlayerDTO> players = HashMap.newHashMap(2);

    private FoxCanvas canvas;

    private BufferedImage gameMap;

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
        this.canvas = canvas;

        // если ещё не нарисована карта - рисуем:
        if (this.gameMap == null) {
            this.gameMap = new BufferedImage(
                    dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D = (Graphics2D) gameMap.getGraphics();
            g2D.setColor(Color.GREEN);
            g2D.setStroke(new BasicStroke(2f));

            int n = 1;
            for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {
                g2D.drawString(n + ")", i - 26, 12);
                g2D.drawLine(i, 0, i, gameMap.getHeight());

                g2D.drawString(n + ")", 6, i - 16);
                g2D.drawLine(0, i, gameMap.getWidth(), i);
                n++;
            }

            g2D.setColor(Color.RED);
            g2D.setStroke(new BasicStroke(2f));
            g2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
            g2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());

            g2D.dispose();
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        if (this.gameMap == null) {
            init(this.canvas);
        }

        // если нужно перерисовать gameMap...
    }
}
