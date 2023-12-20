package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.other.ScreenType;
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
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FOG;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_LINE_STIPPLE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glLineStipple;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3d;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL13.GL_SAMPLES;

@Slf4j
public class MenuCanvas extends FoxCanvas {
    static float theta = 0.5f;

    private static volatile boolean initialized = false;

    private final transient GameController gameController;

    private transient Thread resizeThread = null;

    private volatile boolean isMenuActive;

    public MenuCanvas(UIHandler uiHandler, GameController gameController) {
        super("MenuCanvas", gameController, uiHandler);
        this.gameController = gameController;

//        setSize(parentFrame.getSize());
        setBackground(Color.DARK_GRAY.darker());
        setIgnoreRepaint(true);
        setOpaque(false);
        setFocusable(false);

//        addMouseListener(this);
//        addMouseMotionListener(this);
//        addComponentListener(this);
//        addMouseWheelListener(this); // если понадобится - можно включить.

        if (gameController.isServerIsOpen()) {
            gameController.closeServer();
            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
        }
        if (gameController.isSocketIsOpen()) {
            gameController.closeSocket();
            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
        }

//        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
//        runSecondThread();
    }

    public void render() {
        configureThis();

//        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        glTranslatef(0.0f, 0.0f, -0.005f);

        glPushMatrix();
//            //glScalef(1.0f, 1.0f, 1.0f);
//            //glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
//            //glTranslatef(0.0f, 0.0f, -0.01f);

//            drawField();
        glPopMatrix();

        glPushMatrix();
        //glScalef(0.5f, 0.5f, 0.5f);
        //glTranslatef(0.0f, 0.0f, 0.0f);
        glRotatef(-30, 1.0f, 0.0f, 0.0f);
        glRotatef(theta, 0.0f, 1.0f, 0.0f);

        drawPoly();
        drawPoints();
        drawLines();

        theta += 0.5f;
        glPopMatrix();
    }

    private void drawField() {
        glBegin(GL_QUADS);

//        glNormal3f(0.0f, 0.0f, 1.0f);
        glColor3f(1.0f, 0.0f, 0.0f);
//        glTexCoord2f(-1.0f, -1.0f);
        glVertex3f(-1.0f, -1.0f, 0.0f);

        glColor3f(0.0f, 1.0f, 0.0f);
//        glTexCoord2f(-1.0f, 1.0f);
        glVertex3f(-1.0f, 1.0f, 0.0f);

        glColor3f(0.0f, 0.0f, 1.0f);
//        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 0.0f);

        glColor3f(1.0f, 1.0f, 0.0f);
//        glTexCoord2f(1.0f, -1.0f);
        glVertex3f(1.0f, -1.0f, 0.0f);

        glEnd();

        glLineWidth(3);
        glColor3f(0.0f, 0.0f, 0.0f);
        glBegin(GL_LINE_LOOP);

        glVertex3d(0.1f, 0.1f, 0.1f);
        glVertex3d(0.1f, 0.9f, 0.1f);
        glVertex3d(0.9f, 0.9f, 0.1f);
        glVertex3d(0.9f, 0.1f, 0.1f);

        glEnd();
    }

    private void drawPoly() {
        final float mod = 0.8f;

        glBegin(GL_TRIANGLES); // GL_TRIANGLES | GL_TRIANGLE_FAN | GL_TRIANGLE_STRIP

        glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, 0.0f, 1.0f);
        glVertex3f(-mod, -mod, mod);
        glVertex3f(mod, -mod, mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(1.0f, 0.0f, 0.0f);
        glVertex3f(mod, -mod, mod);
        glVertex3f(mod, -mod, -mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, 0.0f, -1.0f);
        glVertex3f(mod, -mod, -mod);
        glVertex3f(-mod, -mod, -mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.85f, 0.85f, 0.85f, 0.5f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(-1.0f, 0.0f, 0.0f);
        glVertex3f(-mod, -mod, -mod);
        glVertex3f(-mod, -mod, mod);
        glVertex3f(0.0f, mod, 0.0f);

        glEnd();

        glBegin(GL_QUADS);

        // при CCW низ рисуется по часовой:
        glColor4f(0.8f, 0.2f, 0.1f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, -1.0f, 0.0f);
        glVertex3f(-mod, -mod, mod);
        glVertex3f(-mod, -mod, -mod);
        glVertex3f(mod, -mod, -mod);
        glVertex3f(mod, -mod, mod);

        glEnd();
    }

    private void drawLines() {
        glEnable(GL_LINE_STIPPLE);
        glLineStipple(1, (short) 0x0FFF); // 255 (0x00FF) | 0x3F07 | 0xAAAA
        glLineWidth(3);

        glBegin(GL_LINE_LOOP); // GL_LINES | GL_LINE_STRIP | GL_LINE_LOOP
        glColor3f(1.0f, 0.5f, 0.5f);
        glVertex2d(0.025f, 0.85f);
        glVertex2d(0.875f, -0.825f);

        glColor3f(1.0f, 0.0f, 1.0f);
        glVertex2d(0.85f, -0.9f);
        glVertex2d(-0.85f, -0.9f);

        glColor3f(0.0f, 1.0f, 1.0f);
        glVertex2d(-0.875f, -0.825f);
        glVertex2d(-0.025f, 0.85f);
        glEnd();
        glDisable(GL_LINE_STIPPLE);
    }

    private void drawPoints() {
        glPointSize(6);

        glBegin(GL_POINTS);
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(-1.0f, -1.0f, 0.0f);

        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(1.0f, -1.0f, 0.0f);

        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.0f, 1.0f, 0.0f);

        glEnd();
    }

    private void configureThis() {
        // текстуры:
        if (Constants.getGameConfig().isUseTextures()) {
            if (glIsEnabled(GL_TEXTURE_2D)) {
                return;
            }

            loadMenuTextures(); // подключаем текстуры, если требуется.
            glEnable(GL_TEXTURE_2D); // включаем отображение текстур.

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            // glEnable(GL_POLYGON_OFFSET_POINT);
            // glEnable(GL_POLYGON_OFFSET_LINE);
            // glEnable(GL_POLYGON_OFFSET_FILL);
            // glPolygonOffset(0.0f, 2.0f);

            switch (Constants.getUserConfig().getTexturesFilteringLevel()) {
                case NEAREST -> {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                }
                case LINEAR -> {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                }
                case MIPMAP -> {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
//                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
//                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);

                    glHint(GL_SAMPLES, 4);
                    glEnable(GL_MULTISAMPLE);
                }
            }

//            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
//		      glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
//            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_DONT_CARE);

//		      glHint(GL_TEXTURE_COMPRESSION_HINT, GL_FASTEST);
//            glHint(GL_TEXTURE_COMPRESSION_HINT, GL_NICEST);
//            glHint(GL_TEXTURE_COMPRESSION_HINT, GL_DONT_CARE);

//		      glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_FASTEST);
//		      glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_NICEST);
//            glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_DONT_CARE);

//		      glHint(GL_GENERATE_MIPMAP_HINT, GL_FASTEST);
//            glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
//            glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);
        } else {
            glDisable(GL_TEXTURE_2D);
//            glDisable(GL_POLYGON_OFFSET_POINT);
//            glDisable(GL_POLYGON_OFFSET_LINE);
//            glDisable(GL_POLYGON_OFFSET_FILL);
//            glPolygonOffset(0f, 0f);
        }

        // обрезание невидимых глазу частей:
        if (Constants.getGameConfig().isCullFaceGlEnabled()) {
            cullFace();
        } else {
            glDisable(GL_CULL_FACE);
        }

        // освещение:
        if (Constants.getGameConfig().isLightsEnabled()) {
            setLights();
        } else {
            // glDisable(GL_LIGHT1); // надо ли?
            // glDisable(GL_LIGHT0); // надо ли?
            glDisable(GL_LIGHTING);
            // glDisable(GL_NORMALIZE); // надо ли?
        }
        if (!Constants.getGameConfig().isColorMaterialEnabled()) {
            glDisable(GL_COLOR_MATERIAL);
        }

        // интерполяция
        if (Constants.getGameConfig().isSmoothEnabled()) {
            setSmooth();
        } else {
            setFlat();
        }

        // ?..
        if (Constants.getGameConfig().isBlendEnabled()) {
            setBlend();
        } else {
            setDepth();
        }

        // буфер глубины (учёт расположения объектов в глубину псевдо-объема):
        if (Constants.getGameConfig().isDepthEnabled()) {
            setDepth();
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        // туман:
        if (Constants.getGameConfig().isUseFog()) {
            setFog();
        } else {
            glDisable(GL_FOG);
        }

        // учёт прозрачности?..
        if (Constants.getGameConfig().isUseAlphaTest()) {
            setAlphaTest();
        } else {
            glDisable(GL_ALPHA_TEST);
        }
    }

    private void runSecondThread() {
        setSecondThread("Menu second thread", new Thread(() -> {
            if (!initialized) {
                init();
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (isMenuActive && !getSecondThread().isInterrupted()) {
                // если изменился размер фрейма:
//                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
//                    log.debug("Resizing by parent frame...");
//                    onResize();
//                    parentHeightMemory = parentFrame.getBounds().getHeight();
//                }

                try {
                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Завершена работа вспомогательного потока Меню.");
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

    public void run() {
        long lastTime = System.currentTimeMillis();
        long delta;

        // ждём пока компонент не станет виден:
        while (getParent() == null || !isDisplayable() || !initialized) {
            Thread.yield();
            if (System.currentTimeMillis() - lastTime > 3_000) {
                lastTime = System.currentTimeMillis();
                log.error("Не удалось запустить поток {} за отведённое время!", getName());
                if (!getSecondThread().isAlive()) {
                    runSecondThread();
                }
            }
        }

        this.isMenuActive = true;
        while (isMenuActive && !Thread.currentThread().isInterrupted()) {
            delta = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();

            try {
                if (isVisible() && isDisplayable()) {
                    drawNextFrame(delta);
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

            delayDrawing(delta);
        }
        log.info("Thread of Menu canvas is finalized.");
    }

    private void doAnimate() {
        if (getNetworkListPane().isVisible()) {
            if (isConnectionAwait()) {
                getNetworkListPane().repaint();
            }
            if (isPingAwait()) {
                getNetworkListPane().repaint();
            }
        }
    }

    private void drawNextFrame(long delta) {
        repaint(delta);
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

//            if (Constants.getUserConfig().isFullscreen()) {
//                setSize(parentFrame.getSize());
//            } else {
//                setSize(parentFrame.getRootPane().getSize());
//            }

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
        inAc();
        setVisible(true);

        recalculateMenuRectangles();
        createSubPanes();
        reloadShapes(this);

        initialized = true;
    }

    public void mouseMoved(MouseEvent e) {
        setFirstButtonOver(getFirstButtonRect() != null && getFirstButtonRect().contains(e.getPoint()));
        setSecondButtonOver(getSecondButtonRect() != null && getSecondButtonRect().contains(e.getPoint()));
        setThirdButtonOver(getThirdButtonRect() != null && getThirdButtonRect().contains(e.getPoint()));
        setFourthButtonOver(getFourthButtonRect() != null && getFourthButtonRect().contains(e.getPoint()));
        setExitButtonOver(getExitButtonRect() != null && getExitButtonRect().contains(e.getPoint()));
    }

    public void mouseReleased() {
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
        HeroDTO aNewToSave = new HeroDTO();

        aNewToSave.setBaseColor(newHeroTemplate.getBaseColor());
        aNewToSave.setSecondColor(newHeroTemplate.getSecondColor());

        aNewToSave.setCorpusType(newHeroTemplate.getChosenCorpusType());
        aNewToSave.setPeriferiaType(newHeroTemplate.getChosenPeriferiaType());
        aNewToSave.setPeriferiaSize(newHeroTemplate.getPeriferiaSize());

        aNewToSave.setWorldUid(newHeroTemplate.getWorldUid());
        aNewToSave.setCharacterUid(UUID.randomUUID());
        aNewToSave.setCharacterName(newHeroTemplate.getHeroName());
        aNewToSave.setOwnerUid(gameController.getCurrentPlayerUid());
        aNewToSave.setCreateDate(LocalDateTime.now());

        gameController.saveNewHero(aNewToSave, true);

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
