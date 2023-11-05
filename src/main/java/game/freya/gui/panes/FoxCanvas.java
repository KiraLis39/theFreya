package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.interfaces.iCanvas;
import lombok.Getter;
import lombok.Setter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Polygon;

@Getter
@Setter
// iCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public abstract class FoxCanvas extends Canvas implements iCanvas {
    private final String name;
    private int frames = 0;
    private long timeStamp = System.currentTimeMillis();
    private Polygon leftGrayMenuPoly;

    protected FoxCanvas(GraphicsConfiguration gConf, String name) {
        super(gConf);
        this.name = name;
    }

    public void incrementFramesCounter() {
        this.frames++;
    }

    public void drawDebugInfo(Graphics2D g2D, String worldTitle) {
        if (Constants.isDebugInfoVisible()) {
            if (System.currentTimeMillis() - this.timeStamp >= 1000L) {
                Constants.setRealFreshRate(this.frames);
                this.timeStamp = System.currentTimeMillis();
                setFrames(0);
            }

            final float downShift = getHeight() * 0.085f;
            final short rightShift = 21;
            short linesSpace = 24;

            g2D.setFont(Constants.DEBUG_FONT);
            if (worldTitle != null) {
                g2D.setColor(Color.BLACK);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift, downShift + linesSpace);
                g2D.setColor(Color.GRAY);
                g2D.drawString("Мир: %s".formatted(worldTitle), rightShift - 1f, downShift + linesSpace + 1);
                linesSpace += linesSpace;
            }

            g2D.setColor(Color.BLACK);
            g2D.drawString("FPS: лимит/монитор/реально (%s/%s/%s)"
                    .formatted(UserConfig.getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), rightShift, downShift + linesSpace);
            g2D.setColor(Color.GRAY);
            g2D.drawString("FPS: лимит/монитор/реально (%s/%s/%s)"
                    .formatted(UserConfig.getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), rightShift - 1f, downShift + linesSpace + 1);

            incrementFramesCounter();
        }
    }

    public void reloadShapes(FoxCanvas canvas) {
        setLeftGrayMenuPoly(new Polygon(
                new int[]{0, (int) (canvas.getBounds().getWidth() * 0.25D), (int) (canvas.getBounds().getWidth() * 0.2D), 0},
                new int[]{0, 0, canvas.getHeight(), canvas.getHeight()},
                4));
    }
}
