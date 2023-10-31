package game.freya.gui.panes;

import fox.FoxRender;
import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class MenuCanvas extends FoxCanvas { // уже включает в себя MouseListener, MouseMotionListener, Runnable
    private final GameController gameController;
    private boolean isMenuActive;
    private Polygon leftGrayMenuPoly;
    private String downInfoString1, downInfoString2;
    private String newGameButtonText, coopPlayButtonText, optionsButtonText, exitButtonText;
    private boolean newGameButtonOver = false, coopPlayButtonOver = false, optionsButtonOver = false, exitButtonOver = false;
    private Rectangle newGameButtonRect, coopPlayButtonRect, optionsButtonRect, exitButtonRect;
    private BufferedImage backMenuImage;
    private boolean initialized = false;

    public MenuCanvas(GameController gameController) {
        super(Constants.getGraphicsConfiguration());
        this.gameController = gameController;

        setBackground(Color.DARK_GRAY.darker());
        addMouseListener(this);
        addMouseMotionListener(this);

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
    }

    private void drawBackground(Graphics2D g2D) {
        if (!initialized) {
            init();
        }

        // draw background image:
        g2D.drawImage(backMenuImage, 0, 0, getWidth(), getHeight(), this);

        // fill left gray polygon:
        g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
        g2D.fillPolygon(leftGrayMenuPoly);

        // down text:
        g2D.setFont(Constants.INFO_FONT);
        g2D.setColor(Color.WHITE);
        g2D.drawString(downInfoString1, 16, getHeight() - 41);
        g2D.drawString(downInfoString2, 16, getHeight() - 21);

        g2D.setColor(Color.GRAY);
        g2D.drawString(downInfoString1, 15, getHeight() - 40);
        g2D.drawString(downInfoString2, 15, getHeight() - 20);
    }

    private void drawMenu(Graphics2D g2D) {
        if (!initialized) {
            init();
        }

        // buttons text:
        g2D.setFont(Constants.MENU_BUTTONS_FONT);
        g2D.setColor(Color.BLACK);
        g2D.drawString(newGameButtonText, newGameButtonRect.x, newGameButtonRect.y + 17);
        g2D.setColor(newGameButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(newGameButtonText, (int) (getWidth() * 0.035D), newGameButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(coopPlayButtonText, coopPlayButtonRect.x, coopPlayButtonRect.y + 17);
        g2D.setColor(coopPlayButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(coopPlayButtonText, (int) (getWidth() * 0.035D), coopPlayButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(optionsButtonText, optionsButtonRect.x, optionsButtonRect.y + 17);
        g2D.setColor(optionsButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(optionsButtonText, (int) (getWidth() * 0.035D), optionsButtonRect.y + 18);

        g2D.setColor(Color.BLACK);
        g2D.drawString(exitButtonText, exitButtonRect.x, exitButtonRect.y + 17);
        g2D.setColor(exitButtonOver ? Color.GREEN : Color.WHITE);
        g2D.drawString(exitButtonText, (int) (getWidth() * 0.035D), exitButtonRect.y + 18);

//        if (Constants.isDebugInfoVisible()) {
//            g2D.setColor(Color.DARK_GRAY);
//            g2D.drawRoundRect(newGameButtonRect.x, newGameButtonRect.y, newGameButtonRect.width, newGameButtonRect.height, 3, 3);
//            g2D.drawRoundRect(coopPlayButtonRect.x, coopPlayButtonRect.y, coopPlayButtonRect.width, coopPlayButtonRect.height, 3, 3);
//            g2D.drawRoundRect(optionsButtonRect.x, optionsButtonRect.y, optionsButtonRect.width, optionsButtonRect.height, 3, 3);
//            g2D.drawRoundRect(exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height, 3, 3);
//        }
    }

    private void init() {
        leftGrayMenuPoly = new Polygon(
                new int[]{0, (int) (getBounds().getWidth() * 0.25D), (int) (getBounds().getWidth() * 0.2D), 0},
                new int[]{0, 0, getHeight(), getHeight()},
                4);

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
                (int) (getHeight() * 0.30D),
                (int) (getWidth() * 0.1D), 30);

        initialized = true;
    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (newGameButtonOver) {
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.");
        }
        if (coopPlayButtonOver) {
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.");
        }
        if (optionsButtonOver) {
            // todo: доработать это говно
//            FoxTip tip = new FoxTip(Constants.RENDER, (JComponent) getParent(), FoxTip.TYPE.INFO, null, null, null);
//            tip.createFoxTip(FoxTip.TYPE.INFO, null, "1", "2", "3", (JComponent) getParent());
//            tip.showTip();
            new FOptionPane().buildFOptionPane("Не реализовано:",
                    "Приносим свои извинения! Данный функционал ещё находится в разработке.");
        }
        if (exitButtonOver) {
            gameController.exitTheGame(null);
        }
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        newGameButtonOver = newGameButtonRect.contains(e.getPoint());
        coopPlayButtonOver = coopPlayButtonRect.contains(e.getPoint());
        optionsButtonOver = optionsButtonRect.contains(e.getPoint());
        exitButtonOver = exitButtonRect.contains(e.getPoint());
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
}
