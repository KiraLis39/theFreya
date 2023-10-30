package game.freya.gui.panes;

import game.freya.config.Constants;
import game.freya.gui.panes.interfaces.iCanvas;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public abstract class FoxCanvas extends Canvas implements iCanvas { // уже включает в себя MouseListener, Runnable
    private int frames = 0;
    private long timeStamp = System.currentTimeMillis();

    protected FoxCanvas(GraphicsConfiguration gConf) {
        super(gConf);
    }

    public void incrementFramesCounter() {
        this.frames++;
    }

    public void drawDebugInfo(Graphics2D g2D, String worldTitle) {
        short lineY = 35;

        g2D.setFont(Constants.DEBUG_FONT);

        if (worldTitle != null) {
            g2D.setColor(Color.BLACK);
            g2D.drawString("Мир: %s".formatted(worldTitle), 32, lineY);
            g2D.setColor(Color.WHITE);
            g2D.drawString("Мир: %s".formatted(worldTitle), 31, lineY + 1);
            lineY += lineY;
        }

        if (Constants.isDebugInfoVisible()) {
            if (System.currentTimeMillis() - this.timeStamp >= 1000L) {
                Constants.setRealFreshRate(this.frames);
                this.timeStamp = System.currentTimeMillis();
                setFrames(0);
            }

            g2D.setColor(Color.BLACK);
            g2D.drawString("Частота кадров: лимит/монитор/реально (%s/%s/%s)"
                    .formatted(Constants.getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), 32, lineY);
            g2D.setColor(Color.WHITE);
            g2D.drawString("Частота кадров: лимит/монитор/реально (%s/%s/%s)"
                    .formatted(Constants.getScreenDiscreteLimit(), Constants.MON.getRefreshRate(),
                            Constants.getRealFreshRate()), 31, lineY + 1);

            incrementFramesCounter();
        }
    }
}
