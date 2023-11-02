package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, Runnable
public class GameCanvas extends FoxCanvas {
    private final transient GameController gameController;
    private final transient WorldDTO worldDTO;
    private boolean isGameActive;
    private String backToGameButtonText, optionsButtonText, saveButtonText, exitButtonText;
    private Rectangle backToGameButtonRect, optionsButtonRect, saveButtonRect, exitButtonRect;
    private boolean backToGameButtonOver = false, optionsButtonOver = false, saveButtonOver = false, exitButtonOver = false;
    private boolean initialized = false;

    public GameCanvas(WorldDTO worldDTO, GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "GameCanvas");
        this.worldDTO = worldDTO;
        this.gameController = gameController;

        setBackground(Color.MAGENTA.darker().darker());
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

        new Thread(this).start();
    }

    private void init() {
        backToGameButtonText = "Вернуться";
        optionsButtonText = "Настройки";
        saveButtonText = "Сохранить";
        exitButtonText = "Выйти в меню";

        backToGameButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.15D),
                (int) (getWidth() * 0.1D), 30);
        optionsButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.20D),
                (int) (getWidth() * 0.1D), 30);
        saveButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.25D),
                (int) (getWidth() * 0.1D), 30);
        exitButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.30D),
                (int) (getWidth() * 0.1D), 30);

        initialized = true;
    }

    @Override
    public void run() {
        setGameActive();

        while (isGameActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(2);
                }

                if (!initialized) {
                    init();
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.HIGH);

                        // draw all World`s graphic:
                        worldDTO.draw(g2D, this);

                        // is needs to draw the Menu:
                        if (Constants.isPaused()) {
                            drawPauseMode(g2D);
                        }

                        // draw debug info corner if debug mode on:
                        if (Constants.isDebugInfoVisible()) {
                            super.drawDebugInfo(g2D, worldDTO.getTitle()); // отладочная информация
                        }

                        g2D.dispose();
                    } while (getBufferStrategy().contentsRestored());
                } while (getBufferStrategy().contentsLost());
                getBufferStrategy().show();
            } catch (Exception e) {
                log.warn("Canvas draw bs exception: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            if (Constants.isFrameLimited()) {
                try {
                    Thread.sleep(Constants.getDiscreteDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("Thread of Game canvas is finalized.");
    }

    private void setGameActive() {
        this.isGameActive = true;
        Constants.setPaused(false);
    }

    private void drawPauseMode(Graphics2D g2D) {
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString("- PAUSED -",
                (int) (getWidth() / 2D - Constants.FFB.getStringBounds(g2D, "- PAUSED -").getWidth() / 2),
                getHeight() / 2);

        // fill left gray menu polygon:
        g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
        if (getLeftGrayMenuPoly() == null) {
            reloadShapes();
        }
        g2D.fillPolygon(getLeftGrayMenuPoly());

        drawEscMenu(g2D);
    }

    private void drawEscMenu(Graphics2D g2D) {
        // buttons text:
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(backToGameButtonText, backToGameButtonRect.x, backToGameButtonRect.y + 17);
        g2D.setColor(backToGameButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(backToGameButtonText, (int) (getWidth() * 0.035D), backToGameButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(optionsButtonText, optionsButtonRect.x, optionsButtonRect.y + 17);
        g2D.setColor(optionsButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(optionsButtonText, (int) (getWidth() * 0.035D), optionsButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(saveButtonText, saveButtonRect.x, saveButtonRect.y + 17);
        g2D.setColor(saveButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(saveButtonText, (int) (getWidth() * 0.035D), saveButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(exitButtonText, exitButtonRect.x, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(exitButtonText, (int) (getWidth() * 0.035D), exitButtonRect.y + 18);

//        if (Constants.isDebugInfoVisible()) {
//            g2D.setColor(Color.DARK_GRAY);
//            g2D.drawRoundRect(backToGameButtonRect.x, backToGameButtonRect.y, backToGameButtonRect.width, backToGameButtonRect.height, 3, 3);
//            g2D.drawRoundRect(optionsButtonRect.x, optionsButtonRect.y, optionsButtonRect.width, optionsButtonRect.height, 3, 3);
//            g2D.drawRoundRect(saveButtonRect.x, saveButtonRect.y, saveButtonRect.width, saveButtonRect.height, 3, 3);
//            g2D.drawRoundRect(exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height, 3, 3);
//        }
    }

    @Override
    public void stop() {
        this.isGameActive = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        backToGameButtonOver = backToGameButtonRect.contains(e.getPoint());
        optionsButtonOver = optionsButtonRect.contains(e.getPoint());
        saveButtonOver = saveButtonRect.contains(e.getPoint());
        exitButtonOver = exitButtonRect.contains(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (backToGameButtonOver) {
            Constants.setPaused(false);
        }
        if (optionsButtonOver) {
            // todo: доработать это говно
//            FoxTip tip = new FoxTip(Constants.RENDER, (JComponent) getParent(), FoxTip.TYPE.INFO, null, null, null);
//            tip.createFoxTip(FoxTip.TYPE.INFO, null, "1", "2", "3", (JComponent) getParent());
//            tip.showTip();
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.");
        }
        if (saveButtonOver) {
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.");
        }
        if (exitButtonOver) {
            // saveTheGame() ?..
            gameController.loadScreen(ScreenType.MENU_SCREEN);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        reloadShapes();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
