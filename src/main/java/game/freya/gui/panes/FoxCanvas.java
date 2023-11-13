package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.gui.panes.interfaces.iCanvas;
import lombok.Getter;
import lombok.Setter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Polygon;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
// iCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, Runnable
public abstract class FoxCanvas extends Canvas implements iCanvas {
    private static final short rightShift = 21;
    private static float downShift = 0;
    private final String name;
    private int frames = 0;
    private long timeStamp = System.currentTimeMillis();
    private Polygon leftGrayMenuPoly;
    private Polygon headerPoly;
    private Duration duration;

    protected FoxCanvas(GraphicsConfiguration gConf, String name) {
        super(gConf);
        this.name = name;
    }

    public void incrementFramesCounter() {
        this.frames++;
    }

    public void drawFps(Graphics2D g2D) {
        if (downShift == 0) {
            downShift = getHeight() * 0.14f;
        }
        g2D.setFont(Constants.DEBUG_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift - 1f, downShift + 1f);
        g2D.setColor(Color.GRAY);
        g2D.drawString("FPS: limit/mon/real (%s/%s/%s)"
                .formatted(Constants.getUserConfig().getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                        Constants.getRealFreshRate()), rightShift, downShift);
    }

    public void drawDebugInfo(Graphics2D g2D, String worldTitle, long inGamePlayed) {
        if (Constants.isDebugInfoVisible()) {
            incrementFramesCounter();

            if (System.currentTimeMillis() - this.timeStamp >= 1000L) {
                Constants.setRealFreshRate(this.frames);
                this.timeStamp = System.currentTimeMillis();
                this.frames = 0;
            }

            if (worldTitle != null) {
                duration = Duration.ofMillis(inGamePlayed + (System.currentTimeMillis() - Constants.getGameStartedIn()));
                String pass = LocalDateTime.of(0, 1, (int) (duration.toDaysPart() + 1),
                                duration.toHoursPart(), duration.toMinutesPart(), 0, 0)
                        .format(Constants.DATE_FORMAT_2);

                g2D.setFont(Constants.DEBUG_FONT);
                g2D.setColor(Color.BLACK);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + 22);
                g2D.drawString("В игре: %s".formatted(pass), rightShift - 1f, downShift + 43);

                g2D.setColor(Color.GRAY);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + 21);
                g2D.drawString("В игре: %s".formatted(pass), rightShift, downShift + 42);
            }
        }
    }

    public void reloadShapes(FoxCanvas canvas) {
        downShift = getHeight() * 0.14f;

        setLeftGrayMenuPoly(new Polygon(
                new int[]{0, (int) (canvas.getBounds().getWidth() * 0.25D), (int) (canvas.getBounds().getWidth() * 0.2D), 0},
                new int[]{0, 0, canvas.getHeight(), canvas.getHeight()},
                4));

        setHeaderPoly(new Polygon(
                new int[]{0, (int) (canvas.getWidth() * 0.3D), (int) (canvas.getWidth() * 0.29D), (int) (canvas.getWidth() * 0.3D), 0},
                new int[]{3, 3, (int) (canvas.getHeight() * 0.025D), (int) (canvas.getHeight() * 0.05D), (int) (canvas.getHeight() * 0.05D)},
                5));
    }
}
