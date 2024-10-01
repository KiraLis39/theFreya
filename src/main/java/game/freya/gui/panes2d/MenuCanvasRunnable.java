package game.freya.gui.panes2d;

import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import static game.freya.config.Constants.SECOND_THREAD_SLEEP_MILLISECONDS;

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
//        addMouseWheelListener(this); // если понадобится - можно включить.

        if (Constants.getServer() != null && Constants.getServer().isOpen()) {
            gameControllerService.closeConnections();
            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
        }
        if (Constants.getLocalSocketConnection() != null && Constants.getLocalSocketConnection().isOpen()) {
            Constants.getLocalSocketConnection().close();
            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
        }

        thisThread = Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        runSecondThread();
    }

    private void runSecondThread() {
        setSecondThread("Menu second thread", new Thread(() -> {
            if (!Controls.isInitialized()) {
                init();
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (Controls.isMenuActive() && !getSecondThread().isInterrupted()) {
                // если изменился размер фрейма:
                if (getParentFrame().getBounds().getHeight() != parentHeightMemory) {
                    log.debug("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = getParentFrame().getBounds().getHeight();
                }

                try {
                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Завершена работа вспомогательного потока Меню.");
        }));
        getSecondThread().setUncaughtExceptionHandler((_, e) ->
                log.error("Ошибка вспомогательного потока главного меню: {}", ExceptionUtils.getFullExceptionMessage(e)));
        getSecondThread().start();
    }

    @Override
    public void init() {
        inAc();
        setVisible(true);

        loadGameImages();

        recalculateMenuRectangles();
        createSubPanes();
        reloadShapes(this);

        Controls.setInitialized(true);
    }

    private void loadGameImages() {
        try {
            URL necUrl = getClass().getResource("/images/game/");
            assert necUrl != null;
            Constants.CACHE.addAllFrom(necUrl);
        } catch (Exception e) {
            log.error("Menu canvas initialize exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        // ждём пока компонент не станет виден:
        while (getParent() == null || !isDisplayable() || !Controls.isInitialized()) {
            Thread.yield();
            if (System.currentTimeMillis() - lastTime > 3_000) {
                lastTime = System.currentTimeMillis();
                log.error("Не удалось запустить поток {} за отведённое время!", getName());
                if (!getSecondThread().isAlive()) {
                    runSecondThread();
                }
            }
        }

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
}
