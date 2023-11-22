package game.freya.gui.panes.handlers;

import fox.FoxRender;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.MovingVector;
import game.freya.gui.panes.MenuCanvas;
import game.freya.net.ClientHandler;
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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UIHandler {
    private static final int minimapDim = 1024;
    private static final int halfDim = (int) (minimapDim / 2d);
    private static final double ONE_TURN_PI = Math.PI / 4d;
    private GameController gameController;
    private BufferedImage minimapImage;
    private Rectangle minimapRect;
    private Rectangle minimapDebugRect, upLeftPaneRect, upCenterPaneRect, upRightPaneRect, downCenterPaneRect;
    private double heightMemory;
    private String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;
        this.minimapImage = new BufferedImage(minimapDim, minimapDim, BufferedImage.TYPE_INT_ARGB);

        startGameButtonText = "Начать игру";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        createNewButtonText = "Создать";
        randomButtonText = "Случайно";
        resetButtonText = "Сброс";
    }

    public void drawUI(FoxCanvas canvas, Graphics2D g2D) {
        Rectangle canvasRect = canvas.getBounds();

        if (canvas instanceof MenuCanvas menuCanvas) {
            drawMainMenu(g2D, menuCanvas);
        } else if (canvas.isOptionsMenuSetVisible()) {
            canvas.showOptions(g2D);
        } else {
            if (minimapRect == null || heightMemory != canvasRect.getHeight()) {
                heightMemory = canvasRect.getHeight();
                minimapRect = new Rectangle(2, canvasRect.height - 260, 256, 256);
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
                updateMiniMap();
            }

            // draw minimap:
            // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2), ...
            Composite cw = g2D.getComposite();
            g2D.setComposite(AlphaComposite.SrcAtop.derive(Constants.getUserConfig().getMiniMapOpacity()));
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
    }

    private void drawMainMenu(Graphics2D g2D, FoxCanvas canvas) {
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);

        if (canvas.isOptionsMenuSetVisible()) {
            canvas.showOptions(g2D);
        } else if (canvas.getWorldCreatingPane().isVisible()) {
            canvas.drawHeader(g2D, "Создание мира");

            // creating world buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(randomButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
            g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(randomButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(resetButtonText, canvas.getSecondButtonRect().x - 1, canvas.getSecondButtonRect().y + 17);
            g2D.setColor(canvas.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(resetButtonText, canvas.getSecondButtonRect().x, canvas.getSecondButtonRect().y + 18);
        } else if (canvas.getWorldsListPane() != null && canvas.getWorldsListPane().isVisible()) {
            canvas.drawHeader(g2D, "Выбор мира");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
            g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);
        } else if (canvas.getHeroesListPane() != null && canvas.getHeroesListPane().isVisible()) {
            canvas.drawHeader(g2D, "Выбор героя");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
            g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);
        } else if (canvas.getNetworkListPane() != null && canvas.getNetworkListPane().isVisible()) {
            canvas.drawHeader(g2D, "Сетевые подключения");

            g2D.setColor(Color.BLACK);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
            g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(createNewButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);
        } else if (canvas.getNetworkCreatingPane() != null && canvas.getNetworkCreatingPane().isVisible()) {
            canvas.drawHeader(g2D, "Создание подключения");
        } else if (canvas.getHeroCreatingPane() != null && canvas.getHeroCreatingPane().isVisible()) {
            showHeroCreating(g2D, canvas);
            return;
        } else {
            // default buttons text:
            g2D.setColor(Color.BLACK);
            g2D.drawString(startGameButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
            g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(startGameButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(coopPlayButtonText, canvas.getSecondButtonRect().x - 1, canvas.getSecondButtonRect().y + 17);
            g2D.setColor(canvas.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(coopPlayButtonText, canvas.getSecondButtonRect().x, canvas.getSecondButtonRect().y + 18);

            g2D.setColor(Color.BLACK);
            g2D.drawString(optionsButtonText, canvas.getThirdButtonRect().x - 1, canvas.getThirdButtonRect().y + 17);
            g2D.setColor(canvas.isThirdButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(optionsButtonText, canvas.getThirdButtonRect().x, canvas.getThirdButtonRect().y + 18);
        }

        g2D.setColor(Color.BLACK);
        g2D.drawString(canvas.isOptionsMenuSetVisible()
                || (canvas.getNetworkCreatingPane() != null && canvas.getNetworkCreatingPane().isVisible())
                || (canvas.getNetworkListPane() != null && canvas.getNetworkListPane().isVisible())
                || (canvas.getWorldCreatingPane() != null && canvas.getWorldCreatingPane().isVisible())
                ? canvas.getBackButtonText() : canvas.getExitButtonText(), canvas.getExitButtonRect().x - 1, canvas.getExitButtonRect().y + 17);
        g2D.setColor(canvas.isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(canvas.isOptionsMenuSetVisible()
                || (canvas.getNetworkCreatingPane() != null && canvas.getNetworkCreatingPane().isVisible())
                || (canvas.getNetworkListPane() != null && canvas.getNetworkListPane().isVisible())
                || (canvas.getWorldCreatingPane() != null && canvas.getWorldCreatingPane().isVisible())
                ? canvas.getBackButtonText() : canvas.getExitButtonText(), canvas.getExitButtonRect().x, canvas.getExitButtonRect().y + 18);
    }


    private void showHeroCreating(Graphics2D g2D, FoxCanvas canvas) {
        canvas.drawHeader(g2D, "Создание героя");

        // creating hero buttons text:
        g2D.setColor(Color.BLACK);
        g2D.drawString(randomButtonText, canvas.getFirstButtonRect().x - 1, canvas.getFirstButtonRect().y + 17);
        g2D.setColor(canvas.isFirstButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(randomButtonText, canvas.getFirstButtonRect().x, canvas.getFirstButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(resetButtonText, canvas.getSecondButtonRect().x - 1, canvas.getSecondButtonRect().y + 17);
        g2D.setColor(canvas.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(resetButtonText, canvas.getSecondButtonRect().x, canvas.getSecondButtonRect().y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(canvas.getBackButtonText(), canvas.getExitButtonRect().x - 1, canvas.getExitButtonRect().y + 17);
        g2D.setColor(canvas.isExitButtonOver() ? Color.GREEN : Color.WHITE);
        g2D.drawString(canvas.getBackButtonText(), canvas.getExitButtonRect().x, canvas.getExitButtonRect().y + 18);
    }

    private void updateMiniMap() {
        Point2D.Double hPos = gameController.getCurrentHeroPosition();
        MovingVector cVector = gameController.getCurrentHeroVector();
        int srcX = (int) (hPos.x - halfDim);
        int srcY = (int) (hPos.y - halfDim);

        // draw minimap:
        Graphics2D m2D = (Graphics2D) minimapImage.getGraphics();
        m2D.clearRect(0, 0, minimapImage.getWidth(), minimapImage.getHeight());
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

        // отображаем себя на миникарте:
        AffineTransform grTrMem = m2D.getTransform();
        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
        m2D.drawImage((Image) Constants.CACHE.get("green_arrow"),
                minimapImage.getWidth() / 2 - 32, minimapImage.getHeight() / 2 - 32,
                64, 64, null);
        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (ClientHandler client : gameController.getConnectedPlayers()) {
            HeroDTO hero = client.getHeroDto();
            m2D.setColor(Color.YELLOW);
            m2D.fillRect((int) (hero.getPosition().x - 3), (int) (hero.getPosition().y - 3), 6, 6);
        }

        // сканируем все сущности указанного квадранта:
        Rectangle scanRect = new Rectangle(
                Math.min(Math.max(srcX, 0), gameController.getCurrentWorldMap().getWidth() - minimapDim),
                Math.min(Math.max(srcY, 0), gameController.getCurrentWorldMap().getHeight() - minimapDim),
                minimapDim, minimapDim);
        gameController.getWorldEntities(scanRect).forEach(entity -> {
            m2D.setColor(Color.CYAN);
            m2D.fillRect((int) (entity.getPosition().x - 3), (int) (entity.getPosition().y - 3), 6, 6);
        });

        m2D.dispose();
    }
}
