package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.handlers.UIHandler;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_LINE_STIPPLE;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLightfv;
import static org.lwjgl.opengl.GL11.glLineStipple;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMaterialfv;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3f;

@Slf4j
public class GameCanvas extends FoxCanvas {
    private final transient GameController gameController;

    private float camZspeed = 0f;

    private float heroSpeed = 0.05f;

    private float accelerationMod = 2.0f;

    private float pitchSpeed = 0.15f;

    private float yawSpeed = 0.33f;

    private final transient ByteBuffer temp = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());

    private boolean isAccelerated = false, isSneaked = false;

    private float theta = 0.5f;

    private float velocity = 0;

    private float currentPitch = 30;

    private float currentYaw = 0;

    private float heroXPos = 0, heroYPos = 0, heroHeight = -6;

    private transient Thread sneakThread;

    public GameCanvas(UIHandler uiHandler, GameController gameController) {
        super("GameCanvas", gameController, uiHandler);

        this.gameController = gameController;

        setBackground(Color.BLACK);
        setIgnoreRepaint(true);
        setOpaque(false);

        if (gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
            if (gameController.isCurrentWorldIsLocal() && !gameController.isServerIsOpen()) {
                gameController.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в локальной сетевой игре, но наш Сервер не запущен!");
            }

            if (!gameController.isSocketIsOpen()) {
                gameController.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в сетевой игре, но соединения с Сервером не существует!");
            }
        }

//        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
        setSecondThread("Game second thread", new Thread(() -> {
            // ждём пока основной поток игры запустится:
            long timeout = System.currentTimeMillis();
            while (!gameController.isGameActive()) {
                Thread.yield();
                if (System.currentTimeMillis() - timeout > 7_000) {
                    throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
                }
            }

            while (gameController.isGameActive() && !getSecondThread().isInterrupted()) {
                // check gameplay duration:
                checkGameplayDuration(gameController.getCurrentHeroInGameTime());

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
        }));
//        getSecondThread().start();
        setGameActive();
    }

    // вызывается в главном меню в начале работы данного класса:
    private void setGameActive() {
        init();
        gameController.setGameActive(true);

        super.createChat();

        Constants.setPaused(false);
        Constants.setGameStartedIn(System.currentTimeMillis());
    }

    public void render() {
        if (gameController.isGameActive()) {
            configureThis();

            glPushMatrix();

            moveHero();

            drawFloor();
            drawPyramid();

            glPopMatrix();

            glLightfv(GL_LIGHT0, GL_POSITION, temp.asFloatBuffer().put(new float[]{0.0f, -1.5f, 1.0f, 1.0f}).flip());
        }
    }

    private void drawFloor() {
        for (int i = -10; i < 10; i++) {
            for (int j = -10; j < 10; j++) {
                if ((i + j) % 2 == 0) {
                    glColor3f(0, 0, 0);
                } else {
                    glColor3f(255, 255, 255);
                }
                glPushMatrix();
                glTranslatef(i * 2f, j * 2f, 0);
                drawField();
                glPopMatrix();
            }
        }
    }

    private void drawPyramid() {
        glPushMatrix();
        glScalef(0.65f, 0.65f, 0.65f);
        glTranslatef(0.0f, 0.0f, 5.0f);
        glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(theta, 0.0f, 1.0f, 0.0f);

        drawPoly();
        drawPoints();
        drawLines();

        theta += 0.5f;
        glPopMatrix();
    }

    private void drawField() {
        glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[]{0.5f, 0.5f, 0.5f, 0.5f});
        glMaterialfv(GL_FRONT, GL_SPECULAR, new float[]{0.2f, 0.2f, 0.2f, 0.5f});
        glMaterialfv(GL_FRONT, GL_SHININESS, new float[]{0.2f, 0.2f, 0.2f, 0.5f});

        glBegin(GL_QUADS);
        // grass:
        glNormal3f(0.0f, 0.0f, -1.0f);
//        glColor3f(0.0f, 1.0f, 0.0f);

//        glTexCoord2f(-1.0f, -1.0f);
        glVertex3f(-1.0f, -1.0f, 0.0f);

//        glTexCoord2f(1.0f, -1.0f);
        glVertex3f(1.0f, -1.0f, 0.0f);

//        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 0.0f);

//        glTexCoord2f(-1.0f, 1.0f);
        glVertex3f(-1.0f, 1.0f, 0.0f);

        glEnd();
    }

    private void drawPoly() {
        glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[]{0.33f, 0.33f, 0.33f, 0.5f});
        glMaterialfv(GL_FRONT, GL_SPECULAR, new float[]{0.75f, 0.75f, 0.75f, 0.5f});
        glMaterialfv(GL_FRONT, GL_SHININESS, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        final float mod = 0.8f;

        glBegin(GL_TRIANGLES); // GL_TRIANGLES | GL_TRIANGLE_FAN | GL_TRIANGLE_STRIP

        glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, 0.5f, 1.0f);
        glVertex3f(-mod, -mod, mod);
        glVertex3f(mod, -mod, mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.0f, 0.85f, 0.0f, 0.5f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, 0.5f, 0.0f);
        glVertex3f(mod, -mod, mod);
        glVertex3f(mod, -mod, -mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.25f, 0.33f, 1.0f, 1.0f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(0.0f, 0.5f, -1.0f);
        glVertex3f(mod, -mod, -mod);
        glVertex3f(-mod, -mod, -mod);
        glVertex3f(0.0f, mod, 0.0f);

        glColor4f(0.85f, 0.85f, 0.85f, 0.5f);
        // glTexCoord2f(0.0f, 0.0f);
        glNormal3f(-1.0f, 0.5f, 0.0f);
        glVertex3f(-mod, -mod, -mod);
        glVertex3f(-mod, -mod, mod);
        glVertex3f(0.0f, mod, 0.0f);

        glEnd();

        glBegin(GL_QUADS);

        // при CCW низ рисуется по часовой (потому что вверх ногами!):
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
        glMaterialfv(GL_FRONT, GL_SPECULAR, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
        glMaterialfv(GL_FRONT, GL_SHININESS, new float[]{0.1f, 0.1f, 0.1f, 0.0f});
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

    public void run() {
        long lastTime = System.currentTimeMillis();
        long delta;

        // ждём пока компонент не станет виден:
        long timeout = System.currentTimeMillis();
        while (getParent() == null || !isDisplayable()) {
            Thread.yield();
            if (System.currentTimeMillis() - timeout > 15_000) {
                throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
            }
        }

        if (gameController.isCurrentWorldIsNetwork()) {
            log.info("Начинается трансляция данных на Сервер...");
            gameController.startClientBroadcast();
        }

        // старт потока рисования игры:
        while (gameController.isGameActive() && !Thread.currentThread().isInterrupted()) {
            delta = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();

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
                    if (gse.getErrorCode().equals(ErrorMessages.DRAW_ERROR.getErrorCode())) {
                        stop();
                    } else {
                        log.error("Непредвиденная ошибка при отрисовке игры: {}", ExceptionUtils.getFullExceptionMessage(gse));
                    }
                }
            }

            delayDrawing(delta);
        }
        log.info("Thread of Game canvas is finalized.");
    }

//    private void paint() {
//        Graphics2D g2D = (Graphics2D) g;
//        try {
//            super.drawBackground(g2D);
//        } catch (AWTException e) {
//            log.error("Game paint exception here: {}", ExceptionUtils.getFullExceptionMessage(e));
//        }
//        g2D.dispose();
//    }

    public synchronized void stop() {
        if (gameController.isGameActive() || isVisible()) {
            if (gameController.getCurrentWorld() != null) {
                boolean paused = Constants.isPaused();
                boolean debug = Constants.isDebugInfoVisible();

                Constants.setPaused(false);
                Constants.setDebugInfoVisible(false);
                gameController.doScreenShot(new Point(0, 0), new Rectangle(0, 0, 400, 400));
                Constants.setPaused(paused);
                Constants.setDebugInfoVisible(debug);
            }

            if (getSecondThread() != null) {
                getSecondThread().interrupt();
            }

            // защита от зацикливания т.к. loadScreen может снова вызвать этот метод контрольно:
            if (gameController.isGameActive()) {
                gameController.setGameActive(false);

                gameController.saveCurrentWorld();

                if (getDuration() != null && gameController.getPlayedHeroesService().isCurrentHeroNotNull()) {
                    gameController.setCurrentHeroOfflineAndSave(getDuration());
                }

                // если игра сетевая и локальная - останавливаем сервер при выходе из игры:
                if (gameController.isCurrentWorldIsNetwork()) {
                    if (gameController.isCurrentWorldIsLocal()) {
                        if (gameController.closeServer()) {
                            log.info("Сервер успешно остановлен");
                        } else {
                            log.warn("Возникла ошибка при закрытии сервера.");
                        }
                    }
                    if (gameController.isSocketIsOpen()) {
                        gameController.closeSocket();
                    }
                }

//                gameController.loadScreen(ScreenType.MENU_SCREEN);
            }
        }
    }

    public void init() {
        log.info("Do canvas re-initialization...");

        // проводим основную инициализацию класса текущего мира:
        gameController.initCurrentWorld(this);
    }

    private void justSave() {
        gameController.justSaveOnlineHero(getDuration());
        gameController.saveCurrentWorld();
    }

    public void mouseReleased(MouseEvent e) {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getAudiosPane().setVisible(true);
                getVideosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);

            } else {
                Constants.setPaused(false);
                setOptionsMenuSetVisible(false);
            }
        }
        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getVideosPane().setVisible(true);
                getAudiosPane().setVisible(false);
                getHotkeysPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            }
        }
        if (isThirdButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getHotkeysPane().setVisible(true);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
                getGameplayPane().setVisible(false);
            } else {
                // нет нужды в паузе здесь, просто сохраняемся:
                justSave();
                Constants.setPaused(false);
                new FOptionPane().buildFOptionPane("Успешно", "Игра сохранена!",
                        FOptionPane.TYPE.INFO, null, Constants.getDefaultCursor(), 3, false);
            }
        }
        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                getGameplayPane().setVisible(true);
                getHotkeysPane().setVisible(false);
                getVideosPane().setVisible(false);
                getAudiosPane().setVisible(false);
            } else {
                Constants.showNFP();
            }
        }
        if (isExitButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                onExitBack();
            } else if ((int) new FOptionPane().buildFOptionPane("Подтвердить:", "Выйти в главное меню?",
                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0) {
                stop();
            }
        }

        if (gameController.isGameActive()) {
            if (getMinimapShowRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(false);
            }
            if (getMinimapHideRect().contains(e.getPoint())) {
                Constants.setMinimapShowed(true);
            }
        }
    }

    public void moveHero() {
        glRotatef(-currentPitch, 1, 0, 0);
        glRotatef(currentYaw, 0, 0, 1);

        float ugol = (float) (currentYaw / 180f * Math.PI);
        velocity = isCameraMovingForward() ? getHeroSpeed() : isCameraMovingBack() ? -getHeroSpeed() : 0;
        if (isCameraMovingLeft()) {
            velocity = getHeroSpeed();
            ugol -= Math.PI * (isCameraMovingForward() ? 0.25 : isCameraMovingBack() ? 0.75 : 0.5);
        }
        if (isCameraMovingRight()) {
            velocity = getHeroSpeed();
            ugol += Math.PI * (isCameraMovingForward() ? 0.25 : isCameraMovingBack() ? 0.75 : 0.5);
        }

        if (velocity != 0) {
            heroXPos += Math.sin(ugol) * velocity;
            heroYPos += Math.cos(ugol) * velocity;
        }

//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(-heroXPos, -heroYPos, heroHeight);
    }

    private float getHeroSpeed() {
        return isAccelerated ? heroSpeed * accelerationMod : heroSpeed;
    }

    public void setCameraYaw(double yaw) {
        if (yaw != 0) {
            currentYaw += (float) (yaw * yawSpeed);
//            if (currentYaw > 360) {
//                currentYaw = 0;
//            }
//            if (currentYaw < 0) {
//                currentYaw = 360;
//            }
//            log.info("Yaw: {}", (int) currentYaw);
        }
    }

    public void setCameraPitch(double pitch) {
        if (pitch != 0) {
            currentPitch += (float) (pitch * pitchSpeed);
            if (currentPitch < 0) {
                currentPitch = 0;
            }
            if (currentPitch > 180) {
                currentPitch = 180;
            }
//            log.info("Pitch: {}", (int) currentPitch);
        }
    }

    public void moveCameraToHero() {
//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(heroXPos, heroYPos, -6);
    }

    public void setAcceleration(boolean b) {
        this.isAccelerated = b;
    }

    public void setSneak(boolean b) {
        this.isSneaked = b;
        if (sneakThread != null && sneakThread.isAlive()) {
            sneakThread.interrupt();
        }
        if (isSneaked) {
            sneakThread = new Thread(() -> {
                while (heroHeight < -4 && !Thread.currentThread().isInterrupted()) {
                    try {
                        heroHeight += 0.1f;
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            sneakThread = new Thread(() -> {
                while (heroHeight > -6 && !Thread.currentThread().isInterrupted()) {
                    try {
                        heroHeight -= 0.1f;
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        sneakThread.start();
    }
}
