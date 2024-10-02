package game.freya.gui.panes2d;

import game.freya.config.ApplicationProperties;
import game.freya.config.Controls;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvasRunnable extends RunnableCanvasPanel {
    private final transient Thread thisThread;
    private double parentHeightMemory = 0;

    public MenuCanvasRunnable(
            UIHandler uiHandler,
            JFrame parentFrame,
            GameControllerService gameControllerService,
            CharacterService characterService,
            ApplicationProperties props
    ) {
        super("MenuCanvas", gameControllerService, characterService, parentFrame, uiHandler, props);
        setParentFrame(parentFrame);

        setSize(parentFrame.getSize());
        setBackground(Color.DARK_GRAY.darker());
        setIgnoreRepaint(true);
        setOpaque(false);
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

        thisThread = Thread.startVirtualThread(this);
    }

    @Override
    public void init() {
        inAc();

        recalculateMenuRectangles();
        createSubPanes();
        reloadShapes(this);

        Controls.setInitialized(true);
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        Controls.setMenuActive(true);
        while (Controls.isMenuActive() && !Thread.currentThread().isInterrupted()) {
            try {
                if (isVisible() && isDisplayable()) {
                    repaint();
                }

                // продвигаем кадры вспомогательной анимации:
                doAnimate();

                // при успешной отрисовке:
                if (getDrawErrors() > 0) {
                    decreaseDrawErrorCount();
                }
            } catch (Exception e) {
                throwExceptionAndYield(e);
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    @Override
    public void stop() {
        super.stop();
        Controls.setMenuActive(false);
        if (thisThread != null && thisThread.isAlive()) {
            thisThread.interrupt();
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {

    }
}
