package game.freya.gui.panes;

import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.SceneController;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.services.CharacterService;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

import static game.freya.config.Constants.SECOND_THREAD_SLEEP_MILLISECONDS;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, ComponentListener, KeyListener, Runnable
public class GamePaneRunnable extends RunnableCanvasPanel {
    private final transient GameControllerService gameControllerService;
    private final transient SceneController sceneController;
    private double parentHeightMemory = 0;
    private final transient Thread thisThread;

    public GamePaneRunnable(
            UIHandler uiHandler,
            JFrame parentFrame,
            GameControllerService gameControllerService,
            CharacterService characterService,
            SceneController sceneController,
            ApplicationProperties props
    ) {
        super("GameCanvas", gameControllerService, characterService, parentFrame, uiHandler, props);

        this.gameControllerService = gameControllerService;
        this.sceneController = sceneController;
        setParentFrame(parentFrame);

        setSize(parentFrame.getSize());
        setBackground(Color.BLACK);
        getRootPane().setBackground(Color.RED);
        setIgnoreRepaint(true);
        setOpaque(false);
//        setFocusable(false);

        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);
        addKeyListener(this);
//        addComponentListener(this);

        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            if (gameControllerService.getWorldService().getCurrentWorld().isLocal() && !Constants.getServer().isOpen()) {
                sceneController.loadScene(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в локальной сетевой игре, но наш Сервер не запущен!");
            }

            if (!Constants.getLocalSocketConnection().isOpen()) {
                sceneController.loadScene(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в сетевой игре, но соединения с Сервером не существует!");
            }
        }

        thisThread = Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        if (getSecondThread() != null && getSecondThread().isAlive()) {
            try {
                getSecondThread().interrupt();
                getSecondThread().join(1_000);
            } catch (InterruptedException _) {
            }
        }
        setSecondThread("Game second thread", new Thread(() -> {
            // ждём пока основной поток игры запустится:
            long timeout = System.currentTimeMillis();
            while (!Controls.isGameActive()) {
                Thread.yield();
                if (System.currentTimeMillis() - timeout > 7_000) {
                    throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
                }
            }

            while (Controls.isGameActive() && !getSecondThread().isInterrupted()) {
                // check gameplay duration:
                setDuration(Duration.ofMillis(gameControllerService.getCharacterService().getCurrentHero().getInGameTime()
                        + (System.currentTimeMillis() - Constants.getGameStartedIn())));

                // если изменился размер фрейма:
                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
                    log.debug("Resizing by parent frame...");
                    onResize();
                    parentHeightMemory = parentFrame.getBounds().getHeight();
                }

                try {
                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
        getSecondThread().start();
    }

    @Override
    public void run() {
        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable()) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 15_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        // инициализируем все для игры, отображаем окно игры, переключаем isGameActive в true:
        setGameActive();
        requestFocusInWindow();

        // старт бродкастинга с Сервером:
        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            log.info("Начинается трансляция данных на Сервер...");
            Constants.getLocalSocketConnection().startClientBroadcast();
        }

        // старт потока рисования игры:
        while (Controls.isGameActive() && !Thread.currentThread().isInterrupted()) {
            if (!getParentFrame().isActive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

//            if (!Controls.isPaused()) {
//                dragViewIfNeeds();
//            }

            try {
                repaint();

                // при успешной отрисовке:
                if (getDrawErrors() > 0) {
                    decreaseDrawErrorCount();
                }
            } catch (Exception e) {
                try {
                    throwExceptionAndYield(e);
                } catch (GlobalServiceException gse) {
                    if (gse.getCode().equals(ErrorMessages.DRAW_ERROR.getCode())) {
                        stop();
                    } else {
                        log.error("Непредвиденная ошибка при отрисовке игры: {}", ExceptionUtils.getFullExceptionMessage(gse));
                    }
                }
            }
        }
        log.info("Thread of Game canvas is finalized.");
    }

    @Override
    public void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса текущего мира:
        gameControllerService.getWorldService().getCurrentWorld().init(this, gameControllerService);

        reloadShapes(this);
        recalculateMenuRectangles();

        // если не создан вьюпорт - создаём:
        if (getViewPort() == null) {
            recreateViewPort();
        }

        if (!Controls.isControlsMapped()) {
            // назначаем горячие клавиши управления:
            inAc();
        }

        moveViewToPlayer(0, 0);

        requestFocus();
    }

    @Override
    public void stop() {
        super.stop();
        if (thisThread != null && thisThread.isAlive()) {
            thisThread.interrupt();
        }
        exitToMenu(getDuration());
    }

    public void exitToMenu(Duration gameDuration) {
        // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
        if (Controls.isGameActive()) {
            // выходим из активной игры:
            Controls.setGameActive(false);

            gameControllerService.saveTheGame(gameDuration);
            gameControllerService.closeConnections();
            closeGraphics();

            sceneController.loadScene(ScreenType.MENU_SCREEN);
        }
    }
}
