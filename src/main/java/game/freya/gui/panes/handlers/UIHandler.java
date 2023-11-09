package game.freya.gui.panes.handlers;

import fox.FoxRender;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.GameCanvas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UIHandler {
    private final int minimapDim = 256;
    private GameController gameController;
    private BufferedImage minimapImage;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
        this.minimapImage = new BufferedImage(minimapDim, minimapDim, BufferedImage.TYPE_INT_RGB);
    }

    public void drawUI(GameCanvas canvas, Graphics2D g2D) {
        Rectangle canvasRect = canvas.getBounds();

        // up left pane:
        g2D.setStroke(new BasicStroke(2f));
        g2D.setColor(Color.GREEN);
        g2D.drawRect(1, 1, (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));

        // up center pane:
        g2D.setColor(Color.YELLOW);
        g2D.drawRect((int) (canvasRect.getWidth() * 0.36f), 1,
                (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));

        // up right pane:
        g2D.setColor(Color.MAGENTA);
        g2D.drawRect((int) (canvasRect.getWidth() * 0.666f), 1,
                (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));

        // down center pane:
        g2D.setColor(Color.YELLOW);
        g2D.drawRect((int) (canvasRect.getWidth() * 0.36f), (int) (canvasRect.getHeight() * 0.925f),
                (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));

        // down left minimap:
        g2D.setColor(Color.CYAN);
        g2D.drawRect(3, canvasRect.height - 262, 256, 256);

        // draw minimap:
        updateMiniMap(canvas.getCurrentWorld());
        // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2), ...
        Composite cw = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        g2D.drawImage(minimapImage, 4, canvasRect.height - 259, 253, 253, Color.BLACK, canvas);
        g2D.setComposite(cw);
    }

    // todo: доделать карту
    private void updateMiniMap(WorldDTO currentWorld) {
        Point2D.Double plPos = gameController.getCurrentPlayer().getPosition();

        Graphics2D g2D = (Graphics2D) minimapImage.getGraphics();
        Constants.RENDER.setRender(g2D, FoxRender.RENDER.OFF);

        g2D.setColor(Color.WHITE);
        g2D.drawRect(16, 16, 224, 224);

        g2D.setFont(Constants.DEBUG_FONT);
        g2D.drawString(Constants.getNotRealizedString(),
                (int) (minimapDim / 2 - Constants.FFB.getStringBounds(g2D, Constants.getNotRealizedString()).getWidth() / 2),
                minimapDim / 2);

        g2D.dispose();
    }
}
