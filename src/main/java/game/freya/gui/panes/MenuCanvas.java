package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.NetworkListPane;
import game.freya.net.data.NetConnectTemplate;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
// FoxCanvas уже включает в себя MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, Runnable
public class MenuCanvas extends FoxCanvas {
    private final transient GameController gameController;

    private final transient JFrame parentFrame;

    private transient Thread resizeThread = null;

    private volatile boolean isMenuActive, initialized = false;

    private double parentHeightMemory = 0;

    public MenuCanvas(UIHandler uiHandler, JFrame parentFrame, GameController gameController) {
        super("MenuCanvas", gameController, parentFrame, uiHandler);
        this.gameController = gameController;
        this.parentFrame = parentFrame;

        setSize(parentFrame.getSize());
        setBackground(Color.DARK_GRAY.darker());
        setIgnoreRepaint(true);
        setOpaque(false);
        setFocusable(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        if (gameController.isServerIsOpen()) {
            gameController.closeServer();
            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
        }
        if (gameController.isSocketIsOpen()) {
            gameController.closeSocket();
            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
        }

        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        runSecondThread();
    }

    private void runSecondThread() {
        setSecondThread("Menu second thread", new Thread(() -> {
            if (!initialized) {
                init();
            }

            while (isMenuActive && !getSecondThread().isInterrupted()) {
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
            log.info("Завершена работа вспомогательного потока меню.");
        }));
        getSecondThread().setUncaughtExceptionHandler((t, e) ->
                log.error("Ошибка вспомогательного потока главного меню: {}", ExceptionUtils.getFullExceptionMessage(e)));
        getSecondThread().start();
    }

    private void inAc() {
        final String frameName = "mainFrame";

        Constants.INPUT_ACTION.set(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, frameName, "backFunction",
                Constants.getUserConfig().getKeyPause(), 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isVisible()) {
                            onExitBack(MenuCanvas.this);
                        }
                    }
                });

        Constants.INPUT_ACTION.set(JComponent.WHEN_IN_FOCUSED_WINDOW, frameName, "enterNextFunction",
                KeyEvent.VK_ENTER, 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (getHeroesListPane().isVisible()) {
                            playWithThisHero(gameController.getMyCurrentWorldHeroes().get(0));
                            getHeroesListPane().setVisible(false);
                        } else if (getWorldsListPane().isVisible()) {
                            UUID lastWorldUid = gameController.getCurrentPlayerLastPlayedWorldUid();
                            if (gameController.isWorldExist(lastWorldUid)) {
                                chooseOrCreateHeroForWorld(lastWorldUid);
                            } else {
                                chooseOrCreateHeroForWorld(
                                        gameController.findAllWorldsByNetworkAvailable(false).get(0).getUid());
                            }
                        } else {
                            getWorldsListPane().setVisible(true);
                        }
                    }
                });
    }

    @Override
    public void run() {
        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable() || !initialized) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 3_000) {
                timeout = System.currentTimeMillis();
                log.error("Не удалось запустить поток {} за отведённое время!", getName());
                if (!getSecondThread().isAlive()) {
                    runSecondThread();
                }
            }
        }

        this.isMenuActive = true;
        while (isMenuActive && !Thread.currentThread().isInterrupted()) {
            if (!parentFrame.isActive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (!getSecondThread().isAlive()) {
                runSecondThread();
            }

            try {
                if (isVisible() && isDisplayable()) {
                    drawNextFrame();
                }

                if (getNetworkListPane().isVisible()) {
                    if (isConnectionAwait()) {
                        getNetworkListPane().repaint();
                    }
                    if (isPingAwait()) {
                        getNetworkListPane().repaint();
                    }
                }

                // при успешной отрисовке:
                if (getDrawErrors() > 0) {
                    decreaseDrawErrorCount();
                }
            } catch (Exception e) {
                throwExceptionAndYield(e);
            }

            if (Constants.isFpsLimited()) {
                doDrawDelay();
            }
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void drawNextFrame() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;
        try {
            super.drawBackground(g2D);
        } catch (AWTException e) {
            log.error("Ошибка отрисовки кадра игры: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
        g2D.dispose();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        onResize();
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

    private void onResize() {
        if (resizeThread != null && resizeThread.isAlive()) {
            return;
        }

        resizeThread = new Thread(() -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.debug("Resizing of menu canvas...");

            if (Constants.getUserConfig().isFullscreen()) {
                setSize(parentFrame.getSize());
            } else {
                setSize(parentFrame.getRootPane().getSize());
            }

            reloadShapes(this);
            recalculateMenuRectangles();

            setRevolatileNeeds(true);
        });
        resizeThread.start();
        try {
            resizeThread.join(500);
        } catch (InterruptedException e) {
            resizeThread.interrupt();
        }
    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameController.deleteWorld(worldUid);
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        gameController.deleteHero(heroUid);
    }

    public void openCreatingNewHeroPane(HeroDTO template) {
        getHeroesListPane().setVisible(false);
        getHeroCreatingPane().setVisible(true);
        if (template != null) {
            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
        }
    }

    public void exitTheGame() {
        stop();
        gameController.exitTheGame(null);
    }

    @Override
    public void stop() {
        this.isMenuActive = false;
        closeBackImage();
        setVisible(false);
    }

    @Override
    public void init() {
        reloadShapes(this);
        recalculateMenuRectangles();
        inAc();

        setVisible(true);
        createSubPanes();
        this.initialized = true;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setFirstButtonOver(getFirstButtonRect() != null && getFirstButtonRect().contains(e.getPoint()));
        setSecondButtonOver(getSecondButtonRect() != null && getSecondButtonRect().contains(e.getPoint()));
        setThirdButtonOver(getThirdButtonRect() != null && getThirdButtonRect().contains(e.getPoint()));
        setFourthButtonOver(getFourthButtonRect() != null && getFourthButtonRect().contains(e.getPoint()));
        setExitButtonOver(getExitButtonRect() != null && getExitButtonRect().contains(e.getPoint()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getAudiosPane().isVisible()) {
                    getAudiosPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getWorldsListPane().isVisible()) {
                getWorldsListPane().setVisible(false);
                getWorldCreatingPane().setVisible(true);
            } else if (getHeroesListPane().isVisible()) {
                openCreatingNewHeroPane(null);
            } else if (getNetworkListPane().isVisible()) {
                getNetworkListPane().setVisible(false);
                getNetworkCreatingPane().setVisible(true);
            } else {
                if (gameController.findAllWorldsByNetworkAvailable(false).isEmpty()) {
                    getWorldCreatingPane().setVisible(true);
                } else {
                    getWorldsListPane().setVisible(true);
                }
            }
        }

        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                // нажато Настройки графики:
                if (!getVideosPane().isVisible()) {
                    getVideosPane().setVisible(true);
                    getAudiosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getNetworkListPane().isVisible()) {
                ((NetworkListPane) getNetworkListPane()).reloadNet(this);
            } else {
                getNetworkListPane().setVisible(true);
            }
        }

        if (isThirdButtonOver()) {
            if (!isOptionsMenuSetVisible() && !getHeroCreatingPane().isVisible() && !getWorldsListPane().isVisible()) {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (isOptionsMenuSetVisible()) {
                if (!getHotkeysPane().isVisible()) {
                    getHotkeysPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getGameplayPane().isVisible()) {
                    getGameplayPane().setVisible(true);
                    getHotkeysPane().setVisible(false);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isExitButtonOver()) {
            onExitBack(this);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


    // LOCAL game methods:

    /**
     * Когда создаём локальный, несетевой мир - идём сюда, для его сохранения и указания как текущий мир в контроллере.
     *
     * @param newWorld модель нового мира для сохранения.
     */
    public void saveNewLocalWorldAndCreateHero(WorldDTO newWorld) {
        gameController.setCurrentWorld(gameController.saveNewWorld(newWorld));
        chooseOrCreateHeroForWorld(gameController.getCurrentWorldUid());
    }


    // NETWORK game methods:
    public void serverUp(WorldDTO aNetworkWorld) {
        getNetworkListPane().repaint(); // костыль для отображения анимации

        // Если игра по сети, но Сервер - мы, и ещё не запускался:
        gameController.setCurrentWorld(gameController.saveNewWorld(aNetworkWorld));

        // Открываем локальный Сервер:
        if (gameController.isCurrentWorldIsLocal() && gameController.isCurrentWorldIsNetwork() && !gameController.isServerIsOpen()) {
            if (gameController.openServer()) {
                log.info("Сервер сетевой игры успешно активирован на {}", gameController.getServerAddress());
            } else {
                log.warn("Что-то пошло не так при активации Сервера.");
                new FOptionPane().buildFOptionPane("Server error:", "Что-то пошло не так при активации Сервера.", 60, true);
                return;
            }
        }

        if (gameController.isSocketIsOpen()) {
            log.error("Socket should was closed here! Closing...");
            gameController.closeSocket();
        }

        // Подключаемся к локальному Серверу как новый Клиент:
        connectToServer(NetConnectTemplate.builder()
                .address(aNetworkWorld.getNetworkAddress())
                .passwordHash(aNetworkWorld.getPasswordHash())
                .worldUid(aNetworkWorld.getUid())
                .build());
    }

    public void connectToServer(NetConnectTemplate connectionTemplate) {
        getHeroesListPane().setVisible(false);

        setConnectionAwait(true);
        getNetworkListPane().repaint(); // костыль для отображения анимации

        if (connectionTemplate.address().isBlank()) {
            new FOptionPane().buildFOptionPane("Ошибка адреса:", "Адрес сервера не может быть пустым.", 10, true);
        }

        // 1) приходим сюда с host:port для подключения
        String address = connectionTemplate.address().trim();
        String h = address.contains(":") ? address.split(":")[0].trim() : address;
        Integer p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;
        getNetworkListPane().repaint(); // костыль для отображения анимации
        try {
            // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
            if (gameController.connectToServer(h.trim(), p, connectionTemplate.passwordHash())) {
                // 3) проверка героя в этом мире:
                chooseOrCreateHeroForWorld(gameController.getCurrentWorldUid());
            } else {
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, gameController.getLocalSocketConnection().getLastExplanation());
            }
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            if (gse.getErrorCode().equals("ER07")) {
                new FOptionPane().buildFOptionPane("Не доступно:", gse.getMessage(), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            }
        } catch (IllegalThreadStateException tse) {
            log.error("Connection Thread state exception: {}", ExceptionUtils.getFullExceptionMessage(tse));
        } catch (Exception e) {
            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка подключения '%s'.\n"
                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10/13:13958)")
                    .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
        } finally {
            //gameController.closeSocket();
            setConnectionAwait(false);
        }
    }


    // BASE game methods:

    /**
     * После выбора мира - приходим сюда для создания нового героя или
     * выбора существующего, для игры в данном мире.
     *
     * @param worldUid uuid выбранного для игры мира.
     */
    public void chooseOrCreateHeroForWorld(UUID worldUid) {
        getWorldsListPane().setVisible(false);
        getWorldCreatingPane().setVisible(false);
        getNetworkListPane().setVisible(false);
        getNetworkCreatingPane().setVisible(false);

        gameController.setCurrentWorld(worldUid);
        if (gameController.getMyCurrentWorldHeroes().isEmpty()) {
            getHeroCreatingPane().setVisible(true);
        } else {
            getHeroesListPane().setVisible(true);
        }
    }

    /**
     * Приходим сюда для создания нового героя для мира.
     *
     * @param newHeroTemplate модель нового героя для игры в новом мире.
     */
    public void saveNewHeroAndPlay(HeroCreatingPane newHeroTemplate) {
        // сохраняем нового героя и проставляем как текущего:
        gameController.saveNewHero(HeroDTO.builder()
                .heroUid(UUID.randomUUID())
                .heroName(newHeroTemplate.getHeroName())
                .baseColor(newHeroTemplate.getBaseColor())
                .secondColor(newHeroTemplate.getSecondColor())
                .corpusType(newHeroTemplate.getChosenCorpusType())
                .periferiaType(newHeroTemplate.getChosenPeriferiaType())
                .periferiaSize(newHeroTemplate.getPeriferiaSize())
                .ownerUid(gameController.getCurrentPlayerUid())
                .worldUid(newHeroTemplate.getWorldUid())
                .createDate(LocalDateTime.now())
                .build(), true);

        // если подключение к Серверу уже закрылось пока мы собирались:
        if (gameController.isCurrentWorldIsNetwork() && !gameController.isServerIsOpen()) {
            log.warn("Сервер уже закрыт. Требуется повторное подключение.");
            getHeroCreatingPane().setVisible(false);
            getHeroesListPane().setVisible(false);
            getNetworkListPane().setVisible(true);
            return;
        }

        playWithThisHero(gameController.getCurrentHero());
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(HeroDTO hero) {
        gameController.setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());
        gameController.setCurrentHero(hero);

        // если этот мир по сети:
        if (gameController.isCurrentWorldIsNetwork()) {
            // шлем на Сервер своего выбранного Героя:
            if (gameController.registerCurrentHeroOnServer()) {
                gameController.getPlayedHeroesService().addHero(gameController.getCurrentHero());
                startGame();
            } else {
                log.error("Сервер не принял нашего Героя: {}", gameController.getLocalSocketConnection().getLastExplanation());
                gameController.setCurrentHeroOfflineAndSave(null);
                getHeroCreatingPane().repaint();
                getHeroesListPane().repaint();
            }
        } else {
            // иначе просто запускаем мир и играем локально:
            startGame();
        }
    }

    public void startGame() {
        getHeroCreatingPane().setVisible(false);
        getHeroesListPane().setVisible(false);

        log.info("Подготовка к запуску игры должна была пройти успешно. Запуск игрового мира...");
        gameController.loadScreen(ScreenType.GAME_SCREEN);
    }
}
