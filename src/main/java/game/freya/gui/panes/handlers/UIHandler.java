package game.freya.gui.panes.handlers;

import fox.FoxRender;
import game.freya.GameController;
import game.freya.config.Constants;
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
    private static final int minimapDim = 1024;
    private static final int halfDim = (int) (minimapDim / 2d);
    private GameController gameController;
    private BufferedImage minimapImage;
    private Rectangle minimapRect;
    private Rectangle minimapDebugRect, upLeftPaneRect, upCenterPaneRect, upRightPaneRect, downCenterPaneRect;
    private double heightMemory;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
        this.minimapImage = new BufferedImage(minimapDim, minimapDim, BufferedImage.TYPE_INT_RGB);
    }

    public void drawUI(GameCanvas canvas, Graphics2D g2D) {
        Rectangle canvasRect = canvas.getBounds();

        if (minimapRect == null || heightMemory != canvasRect.getHeight()) {
            heightMemory = canvasRect.getHeight();
            minimapRect = new Rectangle(2, canvasRect.height - 261, 256, 256);
            minimapDebugRect = new Rectangle(minimapRect.x + 6, minimapRect.y + 6,
                    minimapRect.width - 12, minimapRect.height - 12);
            upLeftPaneRect = new Rectangle(1, 1, (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));
            upCenterPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.36f), 1,
                    (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));
            upRightPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.666f), 1,
                    (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));
            downCenterPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.36f), (int) (canvasRect.getHeight() * 0.925f),
                    (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));
        }

        // up left pane:
        g2D.setColor(new Color(0, 95, 0, 63));
        g2D.fillRect(upLeftPaneRect.x, upLeftPaneRect.y, upLeftPaneRect.width, upLeftPaneRect.height);
        if (Constants.isDebugInfoVisible()) {
            g2D.setStroke(new BasicStroke(2f));
            g2D.setColor(Color.GREEN);
            g2D.drawRect(upLeftPaneRect.x, upLeftPaneRect.y, upLeftPaneRect.width, upLeftPaneRect.height);
        }

        // up center pane:
        g2D.setColor(new Color(0, 0, 95, 63));
        g2D.fillRect(upCenterPaneRect.x, upCenterPaneRect.y, upCenterPaneRect.width, upCenterPaneRect.height);
        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.YELLOW);
            g2D.drawRect(upCenterPaneRect.x, upCenterPaneRect.y, upCenterPaneRect.width, upCenterPaneRect.height);
        }

        // up right pane:
        g2D.setColor(new Color(95, 0, 0, 63));
        g2D.fillRect(upRightPaneRect.x, upRightPaneRect.y, upRightPaneRect.width, upRightPaneRect.height);
        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.MAGENTA);
            g2D.drawRect(upRightPaneRect.x, upRightPaneRect.y, upRightPaneRect.width, upRightPaneRect.height);
        }

        // down center pane:
        g2D.setColor(new Color(95, 95, 0, 63));
        g2D.fillRect(downCenterPaneRect.x, downCenterPaneRect.y, downCenterPaneRect.width, downCenterPaneRect.height);
        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.YELLOW);
            g2D.drawRect(downCenterPaneRect.x, downCenterPaneRect.y, downCenterPaneRect.width, downCenterPaneRect.height);
        }

        // down left minimap:
        if (!Constants.isPaused()) {
            updateMiniMap(canvas.getCurrentWorld().getGameMap());
        }

        // draw minimap:
        // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2), ...
        Composite cw = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, Constants.getUserConfig().getMiniMapOpacity()));
        g2D.drawImage(minimapImage,
                minimapRect.x + 1, minimapRect.y + 1, minimapRect.width - 2, minimapRect.height - 2,
                Color.BLACK, canvas);
        g2D.setComposite(cw);

        if (Constants.isDebugInfoVisible()) {
            g2D.setColor(Color.CYAN);
            g2D.draw(minimapRect);
        } else {
            g2D.setStroke(new BasicStroke(1.75f));
            g2D.setColor(Color.BLACK);
            g2D.draw(minimapRect);

            g2D.setStroke(new BasicStroke(0.25f));
            g2D.setColor(Color.GRAY);
            g2D.draw(minimapDebugRect);
        }

        if (Constants.isDebugInfoVisible()) {
            g2D.setFont(Constants.LITTLE_UNICODE_FONT);
            g2D.drawString(Constants.getNotRealizedString(),
                    (int) (minimapRect.x + (minimapRect.width / 2d - Constants.FFB
                            .getStringBounds(g2D, Constants.getNotRealizedString()).getWidth() / 2)),
                    minimapRect.y + minimapRect.height / 2);
        }
    }

    private void updateMiniMap(BufferedImage mapImage) {
        Point2D.Double hPos = gameController.getCurrentHero().getPosition();
        int srcX = (int) (hPos.x - halfDim);
        int srcY = (int) (hPos.y - halfDim);
        BufferedImage drown = mapImage.getSubimage(
                Math.min(Math.max(srcX, 0), mapImage.getWidth() - minimapDim),
                Math.min(Math.max(srcY, 0), mapImage.getHeight() - minimapDim),
                minimapDim, minimapDim);

        Graphics2D m2D = (Graphics2D) minimapImage.getGraphics();
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

        m2D.drawImage(drown, 0, 0, minimapImage.getWidth(), minimapImage.getHeight(), null);

        m2D.dispose();
    }
}
