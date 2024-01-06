package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.handlers.FoxWindow;
import lombok.extern.slf4j.Slf4j;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.glfw.GLFW.GLFW_RAW_MOUSE_MOTION;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
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
import static org.lwjgl.opengl.GL11.glClearColor;
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
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3f;

@Slf4j
public class GameWindow extends FoxWindow {
    private final GameController gameController;

    private final ByteBuffer temp = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());

    private float theta = 0.5f;

    public GameWindow(WindowManager windowManager, GameController gameController) {
        super(ScreenType.GAME_SCREEN, "GameCanvas", windowManager, gameController);

        this.gameController = gameController;

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

        // перевод по-умолчанию в игровой режим мыши:
        Constants.setAltControlMode(false, getWindow());
        glfwSetInputMode(getWindow(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);

//        Thread.startVirtualThread(this);

        // запуск вспомогательного потока процессов игры:
//        setSecondThread("Game second thread", new Thread(() -> {
//            // ждём пока основной поток игры запустится:
//            long timeout = System.currentTimeMillis();
//            while (!gameController.isGameActive()) {
//                Thread.yield();
//                if (System.currentTimeMillis() - timeout > 7_000) {
//                    throw new GlobalServiceException(ErrorMessages.DRAW_TIMEOUT);
//                }
//            }
//
//            while (gameController.isGameActive() && !getSecondThread().isInterrupted()) {
//                // check gameplay duration:
//                checkGameplayDuration(gameController.getCurrentHeroInGameTime());
//
//                // если изменился размер фрейма:
////                if (parentFrame.getBounds().getHeight() != parentHeightMemory) {
////                    log.debug("Resizing by parent frame...");
////                    onResize();
////                    parentHeightMemory = parentFrame.getBounds().getHeight();
////                }
//
//                try {
//                    Thread.sleep(SECOND_THREAD_SLEEP_MILLISECONDS);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }));
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

        setActiveWindow(true, ScreenType.GAME_SCREEN);
    }

    @Override
    public void render() {
        if (gameController.isGameActive()) {
            configureThis();

            glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
            glPushMatrix();

            moveHero();

//            if (isZoomEnabled) { // not work.
//                glScalef(1.5f, 1.5f, 1.5f);
//                glTranslatef(0.0f, 0.0f, -0.5f);
//            }

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

    public synchronized void stop() {
        if (gameController.isGameActive()) {
            if (gameController.getCurrentWorld() != null) {
                boolean paused = Constants.isPaused();
                boolean debug = Constants.isDebugInfoVisible();

                Constants.setPaused(false);
                Constants.setDebugInfoVisible(false);
                gameController.doScreenShot(new Point(0, 0), new Rectangle(0, 0, 400, 400));
                Constants.setPaused(paused);
                Constants.setDebugInfoVisible(debug);
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
}
