package game.freya.entities.dto;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import game.freya.enums.MovingVector;
import game.freya.gui.panes.GameCanvas;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.geom.Point2D;
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
    private final Map<String, PlayerDTO> players = HashMap.newHashMap(3);
    private final Map<String, HeroDTO> heroes = HashMap.newHashMap(3);

    // custom fields:
    private GameCanvas canvas;

    private BufferedImage gameMap;

    private boolean initialized = false;

    public WorldDTO(UUID uid, String title, HardnessLevel level, Dimension dimension, int passwordHash) {
        this.uid = uid;
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void addHero(HeroDTO playerDTO) {
        heroes.putIfAbsent(playerDTO.getHeroName(), playerDTO);
    }

    @Override
    public void removeHero(HeroDTO playerDTO) {
        heroes.remove(playerDTO.getHeroName());
    }

    @Override
    public void init(GameCanvas canvas) {
        setCanvas(canvas);

        this.gameMap = new BufferedImage(
                dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM, BufferedImage.TYPE_INT_RGB);

        this.initialized = true;
    }

    public void setCanvas(GameCanvas canvas) {
        if (canvas != null) {
            this.canvas = canvas;
            log.debug("Income canvas dim: {}x{}-{}x{}", canvas.getX(), canvas.getY(), canvas.getWidth(), canvas.getHeight());
        }
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
        // рисуем готовый кадр мира:
        g2D.drawImage(repaintMap(visibleRect), 0, 0, canvas.getWidth(), canvas.getHeight(),
                visibleRect.x, visibleRect.y,
                visibleRect.width, visibleRect.height,
                canvas);
    }

    private BufferedImage repaintMap(Rectangle visibleRect) {
        // re-draw map:
        Graphics2D m2D = (Graphics2D) this.gameMap.getGraphics();
        // m2D.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
        m2D.clip(visibleRect);
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.MED);

        m2D.setColor(new Color(52, 2, 52));
        m2D.fillRect(0, 0, gameMap.getWidth(), gameMap.getHeight());

        int n = 1;
        m2D.setStroke(new BasicStroke(2f));
        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {
            m2D.setColor(new Color(1, 158, 217, 191));
            m2D.drawString(n + ")", i - 26, 12);
            m2D.drawString(n + ")", i - 34, gameMap.getHeight() - 12);

            m2D.drawString(n + ")", 6, i - 16);
            m2D.drawString(n + ")", gameMap.getWidth() - 24, i - 26);

            m2D.setColor(new Color(0, 105, 210, 64));
            m2D.drawLine(i, 0, i, gameMap.getHeight());
            m2D.drawLine(0, i, gameMap.getWidth(), i);

            n++;
        }

        m2D.setColor(Color.RED);
        m2D.setStroke(new BasicStroke(2f));
        m2D.drawLine(0, gameMap.getHeight() / 2, gameMap.getWidth(), gameMap.getHeight() / 2);
        m2D.drawLine(gameMap.getWidth() / 2, 0, gameMap.getWidth() / 2, gameMap.getHeight());

        // рисуем окружение на карте:
        drawEnvironment(m2D, visibleRect);

        // рисуем игроков на карте:
        drawPlayers(m2D, visibleRect);

        m2D.dispose();
        return this.gameMap;
    }

    private void drawEnvironment(Graphics2D g2D, Rectangle visibleRect) {

    }

    private void drawPlayers(Graphics2D g2D, Rectangle visibleRect) {
        HeroDTO currentHeroDto = canvas.getCurrentHero();
        Point2D.Double plPos = currentHeroDto.getPosition();

        heroes.values().forEach(hero -> {
            if (hero.getHeroName().equals(currentHeroDto.getHeroName())) {
                if (!Constants.isPaused()) {
                    // check player moving:
                    boolean isViewMovableX = plPos.x > canvas.getWidth() / 2d
                            && plPos.x < gameMap.getWidth() - canvas.getWidth() / 2d;
                    boolean isViewMovableY = plPos.y > canvas.getHeight() / 2d
                            && plPos.y < gameMap.getHeight() - canvas.getHeight() / 2d;

                    if (canvas.isPlayerMovingUp()) {
                        for (int i = 0; i < currentHeroDto.getSpeed(); i++) {
                            if (!isPlayerCanGo(visibleRect, MovingVector.UP)) {
                                break;
                            }
                            currentHeroDto.moveUp();
                        }
                        if (isViewMovableY) {
                            canvas.dragDown((double) currentHeroDto.getSpeed());
                        }
                    } else if (canvas.isPlayerMovingDown()) {
                        for (int i = 0; i < currentHeroDto.getSpeed(); i++) {
                            if (!isPlayerCanGo(visibleRect, MovingVector.DOWN)) {
                                break;
                            }
                            currentHeroDto.moveDown();
                        }
                        if (isViewMovableY) {
                            canvas.dragUp((double) currentHeroDto.getSpeed());
                        }
                    }

                    if (canvas.isPlayerMovingRight()) {
                        for (int i = 0; i < currentHeroDto.getSpeed(); i++) {
                            if (!isPlayerCanGo(visibleRect, MovingVector.RIGHT)) {
                                break;
                            }
                            currentHeroDto.moveRight();
                        }
                        if (isViewMovableX) {
                            canvas.dragLeft((double) currentHeroDto.getSpeed());
                        }
                    } else if (canvas.isPlayerMovingLeft()) {
                        for (int i = 0; i < currentHeroDto.getSpeed(); i++) {
                            if (!isPlayerCanGo(visibleRect, MovingVector.LEFT)) {
                                break;
                            }
                            currentHeroDto.moveLeft();
                        }
                        if (isViewMovableX) {
                            canvas.dragRight((double) currentHeroDto.getSpeed());
                        }
                    }
                }

                currentHeroDto.draw(g2D);
            } else if (hero.getOwnedPlayer().isOnline() && visibleRect.contains(hero.getPosition())) {
                hero.draw(g2D);
            }
        });
    }

    private boolean isPlayerCanGo(Rectangle visibleRect, MovingVector vector) {
        Point2D.Double pos = canvas.getCurrentHero().getPosition();
        if (!visibleRect.contains(pos)) {
            canvas.moveViewToPlayer(0, 0);
        }
        return switch (vector) {
            case UP -> pos.y > 0;
            case DOWN -> pos.y < gameMap.getHeight();
            case LEFT -> pos.x > 0;
            case RIGHT -> pos.x < gameMap.getWidth();
        };
    }
}
