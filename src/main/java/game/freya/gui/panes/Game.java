package game.freya.gui.panes;

import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.other.MovingVector;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gl.RenderScreen;
import game.freya.gui.WindowManager;
import lombok.extern.slf4j.Slf4j;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

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
import static org.lwjgl.opengl.GL11.glFrustum;
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
public class Game extends RenderScreen {
    private static final float accelerationMod = 2.0f;

    private static final float camZspeed = 0f;

    private static final float pitchSpeed = 0.15f;

    private static final float yawSpeed = 0.33f;

    private static final int minimapDim = 2048;

    private static final int halfDim = (int) (minimapDim / 2d);

    private static final float[] ambientLight = {0.5f, 0.5f, 0.5f, 1.0f}; // 0.0f, 0.0f, 0.3f, 1.0f

    private static final float[] ambientSpecular = {1.0f, 0.33f, 0.33f, 1.0f};

    private static final float[] ambientPosition = {0.0f, 0.0f, -2.0f, 1.0f}; // 31.84215f, 36.019997f, 28.262873f, 1.0f

    private static final float[] ambientDirection = {0.0f, -0.25f, -0.5f, 1.0f};

    private static final float[] ambientAttenuation = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffuseLight = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float[] diffusePosition = {0.5f, 0.5f, -1.5f, 1.0f};

    private static final float[] diffuseSpecular = {0.65f, 0.65f, 0.65f, 1.0f};

    private static float currentPitch = 30;

    private static float currentYaw = 0;

    private static float heroXPos = 0, heroYPos = 0;

    private final GameController gameController;

    private final WindowManager windowManager;

    private final ByteBuffer temp = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());

    private float theta = 0.5f;

    private Thread sneakThread;

    public Game(WindowManager windowManager, GameController gameController) {
        // mock start:
        WorldDTO any = gameController.getAnyWorld();
        gameController.setCurrentWorld(any != null ? any : WorldDTO.builder()
                .author(UUID.randomUUID())
                .build());
        // mock end.

        this.windowManager = windowManager;
        this.gameController = gameController;

        if (gameController.getCurrentWorld() != null && gameController.isCurrentWorldIsNetwork()) {
            if (gameController.isCurrentWorldIsLocal() && !gameController.isServerIsOpen()) {
                windowManager.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в локальной сетевой игре, но наш Сервер не запущен!");
            }

            if (!gameController.isSocketIsOpen()) {
                windowManager.loadScreen(ScreenType.MENU_SCREEN);
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Мы в сетевой игре, но соединения с Сервером не существует!");
            }
        }

        windowManager.getWindow().createChat();

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
    }

    @Override
    public void render(double w, double h) {
        if (gameController.isGameActive()) {
            double frHeight = Math.tan((Constants.getUserConfig().getFov() / 360) * Math.PI) * Constants.getUserConfig().getZNear();
            double frWidth = frHeight * windowManager.getWindowAspect();
            glFrustum(-frWidth, frWidth, -frHeight, frHeight, Constants.getUserConfig().getZNear(), Constants.getUserConfig().getZFar());

            glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
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

    public void moveHero() {
        glRotatef(-currentPitch, 1, 0, 0);
        glRotatef(currentYaw, 0, 0, 1);

        float ugol = (float) (currentYaw / 180f * Math.PI);
        gameController.setVelocity(windowManager.isCameraMovingForward()
                ? getHeroSpeed() : windowManager.isCameraMovingBack() ? -getHeroSpeed() : 0);
        if (windowManager.isCameraMovingLeft()) {
            gameController.setVelocity(getHeroSpeed());
            ugol -= Math.PI * (windowManager.isCameraMovingForward() ? 0.25 : windowManager.isCameraMovingBack() ? 0.75 : 0.5);
        }
        if (windowManager.isCameraMovingRight()) {
            gameController.setVelocity(getHeroSpeed());
            ugol += Math.PI * (windowManager.isCameraMovingForward() ? 0.25 : windowManager.isCameraMovingBack() ? 0.75 : 0.5);
        }

        if (gameController.getVelocity() != 0) {
            heroXPos += Math.sin(ugol) * gameController.getVelocity();
            heroYPos += Math.cos(ugol) * gameController.getVelocity();
        }

//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(-heroXPos, -heroYPos, gameController.getHeroHeight());
    }

    public void setSneak(boolean b) {
        gameController.setSneak(b);
        if (sneakThread != null && sneakThread.isAlive()) {
            sneakThread.interrupt();
        }
        if (gameController.isSneaked()) {
            sneakThread = new Thread(() -> {
                while (gameController.getHeroHeight() < -4 && !Thread.currentThread().isInterrupted()) {
                    try {
                        gameController.setHeroHeight(gameController.getHeroHeight() + 0.1f);
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            sneakThread = new Thread(() -> {
                while (gameController.getHeroHeight() > -6 && !Thread.currentThread().isInterrupted()) {
                    try {
                        gameController.setHeroHeight(gameController.getHeroHeight() - 0.1f);
                        Thread.sleep(18);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        sneakThread.start();
    }

    // здесь вычисляется скорость передвижения героя по миру:
    protected float getHeroSpeed() {
        float heroSpeed = 0.085f;
//        heroSpeed = gameController.getCurrentHeroSpeed();
        return gameController.isAccelerated() ? heroSpeed * accelerationMod : gameController.isSneaked() ? heroSpeed * 0.5f : heroSpeed;
    }

    public void setCameraYaw(double yaw) {
        if (yaw != 0) {
            currentYaw += (float) (yaw * yawSpeed);
            if (currentYaw > 360) {
                currentYaw = 0;
            }
            if (currentYaw < 0) {
                currentYaw = 360;
            }
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
        }
    }

    public void moveCameraToHero() {
//        glTranslated(gameController.getCurrentHeroPosition().x, gameController.getCurrentHeroPosition().y, gameController.getCurrentHeroCorpusHeight());
        glTranslated(heroXPos, heroYPos, -6);
    }

    private void updateMiniMap() {
        Point2D.Double myPos = gameController.getCurrentHeroPosition();
        MovingVector cVector = gameController.getCurrentHeroVector();
        int srcX = (int) (myPos.x - halfDim);
        int srcY = (int) (myPos.y - halfDim);

//        Graphics2D m2D;
//        if (minimapImage == null || minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
//            log.info("Recreating new minimap volatile image by incompatible...");
////            minimapImage = createVolatileImage(minimapDim, minimapDim, new ImageCapabilities(true));
//        }
//        if (minimapImage.validate(Constants.getGraphicsConfiguration()) == VolatileImage.IMAGE_RESTORED) {
//            log.info("Awaits while minimap volatile image is restored...");
//            m2D = this.minimapImage.createGraphics();
//        } else {
//            m2D = (Graphics2D) this.minimapImage.getGraphics();
//            m2D.clearRect(0, 0, minimapImage.getWidth(), minimapImage.getHeight());
//        }

        // draw minimap:
//        Constants.RENDER.setRender(m2D, FoxRender.RENDER.OFF);

//        v2D.setColor(backColor);
//        v2D.fillRect(0, 0, camera.width, camera.height);

        // отображаем себя на миникарте:
//        AffineTransform grTrMem = m2D.getTransform();
//        m2D.rotate(ONE_TURN_PI * cVector.ordinal(), minimapImage.getWidth() / 2d, minimapImage.getHeight() / 2d); // Math.toRadians(90)
//        m2D.drawImage((Image) Constants.CACHE.get("green_arrow"), halfDim - 64, halfDim - 64, 128, 128, null);
//        m2D.setTransform(grTrMem);

        // отображаем других игроков на миникарте:
        for (HeroDTO connectedHero : gameController.getConnectedHeroes()) {
            if (gameController.getCurrentHeroUid().equals(connectedHero.getCharacterUid())) {
                continue;
            }
            int otherHeroPosX = (int) (halfDim - (myPos.x - connectedHero.getLocation().x));
            int otherHeroPosY = (int) (halfDim - (myPos.y - connectedHero.getLocation().y));
//            log.info("Рисуем игрока {} в точке миникарты {}x{}...", connectedHero.getHeroName(), otherHeroPosX, otherHeroPosY);
//            m2D.setColor(connectedHero.getBaseColor());
//            m2D.fillRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
//            m2D.setColor(connectedHero.getSecondColor());
//            m2D.drawRect(otherHeroPosX - 32, otherHeroPosY - 32, 64, 64);
        }

        if (gameController.getCurrentWorldMap() != null) {
            // сканируем все сущности указанного квадранта:
            Rectangle scanRect = new Rectangle(
                    Math.min(Math.max(srcX, 0), gameController.getCurrentWorldMap().getWidth() - minimapDim),
                    Math.min(Math.max(srcY, 0), gameController.getCurrentWorldMap().getHeight() - minimapDim),
                    minimapDim, minimapDim);

//            m2D.setColor(Color.CYAN);
//            gameController.getWorldEnvironments(scanRect)
//                    .forEach(entity -> {
//                        int otherHeroPosX = (int) (halfDim - (myPos.x - entity.getCenterPoint().x));
//                        int otherHeroPosY = (int) (halfDim - (myPos.y - entity.getCenterPoint().y));
//                        m2D.fillRect(otherHeroPosX - 16, otherHeroPosY - 16, 32, 32);
//                    });
        }

//        m2D.setStroke(new BasicStroke(5f));
//        m2D.setPaint(Color.WHITE);
//        m2D.drawRect(3, 3, minimapDim - 7, minimapDim - 7);

//        m2D.setStroke(new BasicStroke(7f));
//        m2D.setPaint(Color.GRAY);
//        m2D.drawRect(48, 48, minimapDim - 96, minimapDim - 96);
//        m2D.dispose();
    }
}
