package game.freya.gui.panes.handlers;

import fox.FoxRender;
import game.freya.config.Constants;
import game.freya.gui.panes.MenuCanvas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UIHandler {
    private Rectangle upLeftPaneRect, upCenterPaneRect, upRightPaneRect, downCenterPaneRect;

    private String startGameButtonText, coopPlayButtonText, optionsButtonText, randomButtonText, resetButtonText, createNewButtonText, repingButtonText;

    private double heightMemory;

    @Autowired
    public void init() {
        startGameButtonText = "Начать игру";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        createNewButtonText = "Создать";
        randomButtonText = "Случайно";
        resetButtonText = "Сброс";
        repingButtonText = "Обновить";
    }

    public void drawUI(Graphics2D v2D, FoxCanvas canvas) {
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

            // draw chat:
            if (canvas.getGameController().isCurrentWorldIsNetwork() && canvas.getChat() != null) {
                Constants.RENDER.setRender(v2D, FoxRender.RENDER.OFF);
                canvas.getChat().draw(v2D);
            }
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

        canvas.getChat().setLocation(new Point(canvas.getWidth() - canvas.getWidth() / 5 - 6, 72));
        canvas.getChat().setSize(new Dimension(canvas.getWidth() / 5, canvas.getHeight() / 4));
    }
}
