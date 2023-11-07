package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.ScreenType;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvas extends FoxCanvas {
    private final transient GameController gameController;
    private boolean isMenuActive;
    private String downInfoString1, downInfoString2;
    private String newGameButtonText, coopPlayButtonText, optionsButtonText, exitButtonText;
    private boolean newGameButtonOver = false, coopPlayButtonOver = false, optionsButtonOver = false, exitButtonOver = false;
    private Rectangle newGameButtonRect, coopPlayButtonRect, optionsButtonRect, exitButtonRect;
    private transient BufferedImage backMenuImage;
    private boolean initialized = false;

    public MenuCanvas(GameController gameController) {
        super(Constants.getGraphicsConfiguration(), "MenuCanvas");
        this.gameController = gameController;

        setBackground(Color.DARK_GRAY.darker());
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isMenuActive = true;

        while (isMenuActive) {
            if (getParent() == null || !isDisplayable()) {
                Thread.yield();
                continue;
            }

            try {
                if (getBufferStrategy() == null) {
                    createBufferStrategy(2);
                }

                do {
                    do {
                        Graphics2D g2D = (Graphics2D) getBufferStrategy().getDrawGraphics();
                        g2D.clearRect(0, 0, getWidth(), getHeight());
                        Constants.RENDER.setRender(g2D, FoxRender.RENDER.MED);
                        drawBackground(g2D);
                        drawMenu(g2D);
                        super.drawDebugInfo(g2D, null);
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
        log.info("Thread of Menu canvas is finalized.");
    }

    private void drawBackground(Graphics2D g2D) {
        if (!initialized) {
            init();
        }

        // draw background image:
        g2D.drawImage(backMenuImage, 0, 0, getWidth(), getHeight(), this);

        // fill left gray polygon:
        g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
        g2D.fillPolygon(getLeftGrayMenuPoly());

        // down text:
        g2D.setFont(Constants.INFO_FONT);
        g2D.setColor(Color.WHITE);
        g2D.drawString(downInfoString1, 16, getHeight() - 40);
        g2D.drawString(downInfoString2, 16, getHeight() - 22);
    }

    private void drawMenu(Graphics2D g2D) {
        if (!initialized) {
            init();
        }

        // buttons text:
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(newGameButtonText, newGameButtonRect.x - 1, newGameButtonRect.y + 17);
        g2D.setColor(newGameButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(newGameButtonText, newGameButtonRect.x, newGameButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(coopPlayButtonText, coopPlayButtonRect.x - 1, coopPlayButtonRect.y + 17);
        g2D.setColor(coopPlayButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(coopPlayButtonText, coopPlayButtonRect.x, coopPlayButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(optionsButtonText, optionsButtonRect.x - 1, optionsButtonRect.y + 17);
        g2D.setColor(optionsButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(optionsButtonText, optionsButtonRect.x, optionsButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(exitButtonText, exitButtonRect.x - 1, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(exitButtonText, exitButtonRect.x, exitButtonRect.y + 18);
    }

    private void init() {
        reloadShapes(this);

        try {
            Constants.CACHE.addIfAbsent("backMenuImage", ImageIO.read(new File("./resources/images/demo_menu.jpg")));
            backMenuImage = (BufferedImage) Constants.CACHE.get("backMenuImage");
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }

        downInfoString1 = Constants.getGameName().concat(" v.").concat(Constants.getGameVersion());
        downInfoString2 = Constants.getGameAuthor().concat("(%s)".formatted(2023));

        newGameButtonText = "Новая игра";
        coopPlayButtonText = "Игра по сети";
        optionsButtonText = "Настройки";
        exitButtonText = "Выход";

        recalculateMenuRectangles();

        this.initialized = true;
    }

    private void recalculateMenuRectangles() {
        newGameButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.15D),
                (int) (getWidth() * 0.1D), 30);
        coopPlayButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.20D),
                (int) (getWidth() * 0.1D), 30);
        optionsButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.25D),
                (int) (getWidth() * 0.1D), 30);
        exitButtonRect = new Rectangle((int) (getWidth() * 0.03525D),
                (int) (getHeight() * 0.85D),
                (int) (getWidth() * 0.1D), 30);
    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (newGameButtonOver) {
            gameController.loadScreen(ScreenType.GAME_SCREEN);
        }
        if (coopPlayButtonOver) {
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.", FOptionPane.TYPE.INFO);
        }
        if (optionsButtonOver) {
            // FoxTip плохо подходит для Rectangle т.к. нет возможности получить абсолютные координаты:
            // new Color(102, 242, 223), new Color(157, 159, 201)
//            FoxTip tip = new FoxTip(FoxTip.TYPE.INFO, null, "Не реализовано",
//                    "Данный функционал ещё находится\nв разработке.", "Приносим свои извинения", optionsButtonRect);
//            tip.showTip();
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.", FOptionPane.TYPE.INFO);
        }
        if (exitButtonOver && (int) new FOptionPane().buildFOptionPane(
                "Подтвердить:", "Выйти на рабочий стол?", FOptionPane.TYPE.YES_NO_TYPE).get() == 0) {
            gameController.exitTheGame(null);
        }
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        newGameButtonOver = newGameButtonRect != null && newGameButtonRect.contains(e.getPoint());
        coopPlayButtonOver = coopPlayButtonRect != null && coopPlayButtonRect.contains(e.getPoint());
        optionsButtonOver = optionsButtonRect != null && optionsButtonRect.contains(e.getPoint());
        exitButtonOver = exitButtonRect != null && exitButtonRect.contains(e.getPoint());
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
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void componentResized(ComponentEvent e) {
        reloadShapes(this);
        recalculateMenuRectangles();
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
