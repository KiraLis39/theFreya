package game.freya.gui.panes.handlers;

import fox.FoxRender;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.MovingVector;
import game.freya.gui.panes.MenuCanvas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.VolatileImage;

import static game.freya.config.Constants.ONE_TURN_PI;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UIHandler {
    private static final int minimapDim = 2048;

    private static final int halfDim = (int) (minimapDim / 2d);

    private GameController gameController;

    private VolatileImage minimapImage;

    private Rectangle upLeftPaneRect, upCenterPaneRect, upRightPaneRect, downCenterPaneRect;

    private double heightMemory;

    private String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText, repingButtonText;

    @Autowired
    public void setGameController(@Lazy GameController gameController) {
        this.gameController = gameController;

        startGameButtonText = "Начать игру";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        createNewButtonText = "Создать";
        randomButtonText = "Случайно";
        resetButtonText = "Сброс";
        repingButtonText = "Обновить";
    }

    public void drawUI(Graphics2D v2D, FoxCanvas canvas) throws AWTException {
        if (heightMemory != canvas.getBounds().getHeight()) {
            recreateRectangles(canvas);
        }

        if (canvas instanceof MenuCanvas menuCanvas) {
            drawMainMenu(v2D, menuCanvas);
            drawCreatorInfo(v2D, menuCanvas);
        } else if (canvas.isOptionsMenuSetVisible()) {
            canvas.showOptions(v2D);
        } else {
            // up left pane:
            v2D.setStroke(new BasicStroke(2f));
            v2D.setColor(new Color(0, 95, 0, 63));
            v2D.fillRect(upLeftPaneRect.x, upLeftPaneRect.y, upLeftPaneRect.width, upLeftPaneRect.height);
            if (Constants.isDebugInfoVisible()) {
                v2D.setColor(Color.GREEN);
                v2D.drawRect(upLeftPaneRect.x, upLeftPaneRect.y, upLeftPaneRect.width, upLeftPaneRect.height);
            }

            // up center pane:
            v2D.setColor(new Color(0, 0, 95, 63));
            v2D.fillRect(upCenterPaneRect.x, upCenterPaneRect.y, upCenterPaneRect.width, upCenterPaneRect.height);
            if (Constants.isDebugInfoVisible()) {
                v2D.setColor(Color.YELLOW);
                v2D.drawRect(upCenterPaneRect.x, upCenterPaneRect.y, upCenterPaneRect.width, upCenterPaneRect.height);
            }

            // up right pane:
            v2D.setColor(new Color(95, 0, 0, 63));
            v2D.fillRect(upRightPaneRect.x, upRightPaneRect.y, upRightPaneRect.width, upRightPaneRect.height);
            if (Constants.isDebugInfoVisible()) {
                v2D.setColor(Color.MAGENTA);
                v2D.drawRect(upRightPaneRect.x, upRightPaneRect.y, upRightPaneRect.width, upRightPaneRect.height);
            }

            // down center pane:
            v2D.setColor(new Color(95, 95, 0, 63));
            v2D.fillRect(downCenterPaneRect.x, downCenterPaneRect.y, downCenterPaneRect.width, downCenterPaneRect.height);
            if (Constants.isDebugInfoVisible()) {
                v2D.setColor(Color.YELLOW);
                v2D.drawRect(downCenterPaneRect.x, downCenterPaneRect.y, downCenterPaneRect.width, downCenterPaneRect.height);
            }

            // down left minimap:
            if (!Constants.isPaused()) {
                Rectangle mapButRect;
                if (Constants.isMinimapShowed()) {
                    mapButRect = canvas.getMinimapShowRect();

                    updateMiniMap(canvas);

                    // draw minimap:
                    // g2D.drawImage(minimapImage.getScaledInstance(256, 256, 2), ...
                    Composite cw = v2D.getComposite();
                    v2D.setComposite(AlphaComposite.SrcAtop.derive(Constants.getUserConfig().getMiniMapOpacity()));
                    v2D.drawImage(minimapImage, canvas.getMinimapRect().x, canvas.getMinimapRect().y,
                            canvas.getMinimapRect().width, canvas.getMinimapRect().height, null);
                    v2D.setComposite(cw);

                    if (Constants.isDebugInfoVisible()) {
                        v2D.setColor(Color.CYAN);
                        v2D.draw(canvas.getMinimapRect());
                    }
                } else {
                    mapButRect = canvas.getMinimapHideRect();
                }
                // draw minimap button:
                v2D.setColor(Color.YELLOW);
                v2D.fillRoundRect(mapButRect.x, mapButRect.y, mapButRect.width, mapButRect.height, 4, 4);
                v2D.setColor(Color.GRAY);
                v2D.drawRoundRect(mapButRect.x, mapButRect.y, mapButRect.width, mapButRect.height, 4, 4);

                v2D.setColor(Color.BLACK);
                v2D.setStroke(new BasicStroke(2f));
                v2D.drawPolyline(new int[]{mapButRect.x + 3, mapButRect.x + mapButRect.width / 2, mapButRect.x + mapButRect.width - 3},
                        new int[]{mapButRect.y + 3, mapButRect.y + mapButRect.height - 3, mapButRect.y + 3}, 3);
            } else {
                canvas.drawPauseMode(v2D);
            }

            // draw chat:
            canvas.getChat().draw(v2D);
        }
    }

    private void drawCreatorInfo(Graphics2D g2D, FoxCanvas canvas) {
        // down right corner text:
        g2D.setFont(Constants.INFO_FONT);
        g2D.setColor(Color.WHITE);
        g2D.drawString(canvas.getDownInfoString1(),
                (int) (canvas.getWidth() - Constants.FFB.getStringBounds(g2D, canvas.getDownInfoString1()).getWidth() - 6),
                canvas.getHeight() - 9);
        g2D.drawString(canvas.getDownInfoString2(),
                (int) (canvas.getWidth() - Constants.FFB.getStringBounds(g2D, canvas.getDownInfoString2()).getWidth() - 6),
                canvas.getHeight() - 25);
    }

    private void drawMainMenu(Graphics2D g2D, FoxCanvas canvas) {
        g2D.setFont(Constants.getUserConfig().isFullscreen() ? Constants.MENU_BUTTONS_BIG_FONT : Constants.MENU_BUTTONS_FONT);

        if (canvas.isOptionsMenuSetVisible()) {
            canvas.showOptions(g2D);
            return;
        } else if (canvas.getWorldCreatingPane() != null && canvas.getWorldCreatingPane().isVisible()) {
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

            g2D.setColor(Color.BLACK);
            g2D.drawString(repingButtonText, canvas.getSecondButtonRect().x - 1, canvas.getSecondButtonRect().y + 17);
            g2D.setColor(canvas.isSecondButtonOver() ? Color.GREEN : Color.WHITE);
            g2D.drawString(repingButtonText, canvas.getSecondButtonRect().x, canvas.getSecondButtonRect().y + 18);
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

    private void updateMiniMap(FoxCanvas canvas) throws AWTException {
        Point2D.Double myPos = gameController.getCurrentHeroPosition();
        MovingVector cVector = gameController.getCurrentHeroVector();
        int srcX = (int) (myPos.x - halfDim);
        int srcY = (int) (myPos.y - halfDim);

        Graphics2D m2D;
        if (minimapImage == null || minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            log.info("Recreating new minimap volatile image by incompatible...");
            minimapImage = canvas.createVolatileImage(minimapDim, minimapDim, new ImageCapabilities(true));
        }
        if (minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_RESTORED) {
            log.info("Awaits while minimap volatile image is restored...");
            m2D = this.minimapImage.createGraphics();
        } else {
            m2D = (Graphics2D) this.minimapImage.getGraphics();
            m2D.clearRect(0, 0, minimapImage.getWidth(), minimapImage.getHeight());
        }

        // draw minimap:
        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

//        v2D.setColor(backColor);
//        v2D.fillRect(0, 0, camera.width, camera.height);

        // отображаем себя на миникарте:
        AffineTransform grTrMem = m2D.getTransform();
        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
        m2D.drawImage((Image) Constants.CACHE.get("green_arrow"), halfDim - 64, halfDim - 64, 128, 128, null);
        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (HeroDTO connectedHero : gameController.getConnectedHeroes()) {
            if (gameController.getCurrentHeroUid().equals(connectedHero.getHeroUid())) {
                continue;
            }
            int otherHeroPosX = (int) (halfDim - (myPos.x - connectedHero.getPosition().x));
            int otherHeroPosY = (int) (halfDim - (myPos.y - connectedHero.getPosition().y));
//            log.info("Рисуем игрока {} в точке миникарты {}x{}...", connectedHero.getHeroName(), otherHeroPosX, otherHeroPosY);
            m2D.setColor(connectedHero.getBaseColor());
            m2D.fillRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
            m2D.setColor(connectedHero.getSecondColor());
            m2D.drawRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
        }

        // сканируем все сущности указанного квадранта:
        Rectangle scanRect = new Rectangle(
                Math.min(Math.max(srcX, 0), gameController.getCurrentWorldMap().getWidth() - minimapDim),
                Math.min(Math.max(srcY, 0), gameController.getCurrentWorldMap().getHeight() - minimapDim),
                minimapDim, minimapDim);
        m2D.setColor(Color.CYAN);
        gameController.getWorldEnvironments(scanRect)
                .forEach(entity -> m2D.fillRect(
                        (int) (entity.getPosition().x - 3),
                        (int) (entity.getPosition().y - 3),
                        6, 6));

        m2D.setStroke(new BasicStroke(5f));
        m2D.setPaint(Color.WHITE);
        m2D.drawRect(3, 3, minimapDim - 7, minimapDim - 7);

        m2D.setStroke(new BasicStroke(7f));
        m2D.setPaint(Color.GRAY);
        m2D.drawRect(48, 48, minimapDim - 96, minimapDim - 96);
        m2D.dispose();
    }

    private void recreateRectangles(FoxCanvas canvas) {
        Rectangle canvasRect = canvas.getBounds();

        heightMemory = canvasRect.getHeight();
        upLeftPaneRect = new Rectangle(1, 1, (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));
        upCenterPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.36f), 1,
                (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));
        upRightPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.666f), 1,
                (int) (canvasRect.getWidth() * 0.333f), (int) (canvasRect.getHeight() * 0.075f));
        downCenterPaneRect = new Rectangle((int) (canvasRect.getWidth() * 0.36f), (int) (canvasRect.getHeight() * 0.925f),
                (int) (canvasRect.getWidth() * 0.28f), (int) (canvasRect.getHeight() * 0.075f));
    }
}
