package game.freya.entities.dto;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import game.freya.enums.MovingVector;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.GameCanvas;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class WorldDTO extends ComponentAdapter implements iWorld {

    private final Random r = new Random(100);

    @Getter
    private final UUID uid;

    private final Set<HeroDTO> heroes = HashSet.newHashSet(3);

    @Setter
    @Getter
    private UUID author;

    @Setter
    @Getter
    private String title = "world_demo_" + r.nextInt(1000);

    @Setter
    @Getter
    private boolean isNetAvailable = false;

    @Setter
    @Getter
    private int passwordHash = -1;

    @Setter
    @Getter
    private Dimension dimension = new Dimension(32, 32);

    @Setter
    @Getter
    private HardnessLevel level = HardnessLevel.EASY;

    @Getter
    private LocalDateTime createDate = LocalDateTime.now();

    // custom fields:
    private GameCanvas canvas;

    @Getter
    private BufferedImage gameMap;

    @Getter
    private ImageIcon icon;

    public WorldDTO() {
        this.uid = UUID.randomUUID();
        this.title = "the_world_" + r.nextInt(1000);
        this.level = HardnessLevel.EASY;
        this.dimension = new Dimension(32, 32);
        this.passwordHash = -1;
    }

    public WorldDTO(
            UUID uid,
            UUID author,
            String title,
            HardnessLevel level,
            Dimension dimension,
            boolean isNetAvailable,
            int passwordHash,
            LocalDateTime createDate
    ) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.isNetAvailable = isNetAvailable;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
        this.createDate = createDate;
    }

    @Override
    public void addHero(HeroDTO heroDTO) {
        if (heroDTO.getUid() == null) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "hero uuid");
        }
        heroes.add(heroDTO);
    }

    @Override
    public void removeHero(HeroDTO heroDTO) {
        heroes.remove(heroDTO);
    }

    @Override
    public void init(GameCanvas canvas) {
        setCanvas(canvas);

        this.gameMap = new BufferedImage(
                dimension.width * Constants.MAP_CELL_DIM, dimension.height * Constants.MAP_CELL_DIM, BufferedImage.TYPE_INT_RGB);
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

    public boolean isHeroActive(HeroDTO hero, Rectangle visibleRect) {
        return visibleRect.contains(hero.getPosition()) && hero.getOwnedPlayer().isOnline();
    }

    private BufferedImage repaintMap(Rectangle visibleRect) {
        // re-draw map:
        Graphics2D m2D = (Graphics2D) this.gameMap.getGraphics();
        // m2D.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
        m2D.clip(visibleRect);
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.MED,
                Constants.getUserConfig().isUseSmoothing(), Constants.getUserConfig().isUseBicubic());

        m2D.setColor(new Color(52, 2, 52));
        m2D.fillRect(0, 0, gameMap.getWidth(), gameMap.getHeight());

        int n = 1;
        m2D.setStroke(new BasicStroke(2f));
        for (int i = Constants.MAP_CELL_DIM; i <= gameMap.getWidth(); i += Constants.MAP_CELL_DIM) {

            if (Constants.isDebugInfoVisible()) {
                m2D.setColor(new Color(1, 158, 217, 191));
                m2D.drawString(n + ")", i - 26, 12);
                m2D.drawString(n + ")", i - 34, gameMap.getHeight() - 12);

                m2D.drawString(n + ")", 6, i - 16);
                m2D.drawString(n + ")", gameMap.getWidth() - 24, i - 26);
            }

            m2D.setColor(new Color(0, 105, 210, 64));
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
        drawEnvironment(m2D, visibleRect);

        // рисуем игроков на карте:
        drawHeroes(m2D, visibleRect);

        m2D.dispose();
        return this.gameMap;
    }

    private void drawEnvironment(Graphics2D g2D, Rectangle visibleRect) {

    }

    private void drawHeroes(Graphics2D g2D, Rectangle visibleRect) {
        for (HeroDTO hero : heroes) {
            if (hero.getUid().equals(canvas.getCurrentHero().getUid())) {
                // если это - я:
                if (!Constants.isPaused()) {
                    checkPlayerMoving(hero, visibleRect);
                }
                hero.draw(g2D);
            } else if (isHeroActive(hero, visibleRect)) {
                // если чужой герой он-лайн и в пределах видимости:
                hero.draw(g2D);
            }
        }
    }

    private void checkPlayerMoving(HeroDTO currentHeroDto, Rectangle visibleRect) {
        Point2D.Double plPos = currentHeroDto.getPosition();

        boolean isViewMovableX = plPos.x > visibleRect.getWidth() / 2d
                && plPos.x < gameMap.getWidth() - (visibleRect.width - visibleRect.x) / 2d;
        boolean isViewMovableY = plPos.y > visibleRect.getHeight() / 2d
                && plPos.y < gameMap.getHeight() - (visibleRect.height - visibleRect.y) / 2d;

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

        canvas.setCurrentHero(currentHeroDto);
        heroes.add(currentHeroDto);
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

    public Set<HeroDTO> getHeroes() {
        return this.heroes;
    }
}
