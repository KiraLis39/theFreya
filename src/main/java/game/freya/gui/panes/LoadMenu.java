package game.freya.gui.panes;

import game.freya.GameController;
import game.freya.enums.other.ScreenType;
import game.freya.gl.RenderScreen;
import game.freya.gui.WindowManager;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2d;

@Slf4j
public class LoadMenu extends RenderScreen {
    private static final ScreenType type = ScreenType.MENU_LOADING_SCREEN;

    private final GameController gameController;

    private final WindowManager windowManager;

    public LoadMenu(WindowManager windowManager, GameController gameController) {
        this.gameController = gameController;
        this.windowManager = windowManager;
    }

    @Override
    public void render(double w, double h) {
        glOrtho(0, windowManager.getWindow().getWidth(), windowManager.getWindow().getHeight(), 0, -1.0f, 1.0f);
        glFrontFace(GL_CW);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glPushMatrix();

        drawBackground(w, h);
        drawGameInfo(w, h);

        glPopMatrix();
    }

    @Override
    public ScreenType getType() {
        return type;
    }

    private void drawBackground(double w, double h) {
        if (gameController.isTextureExist("menu")) {
            gameController.bindTexture("menu");
        }

        glBegin(GL_QUADS);

        glColor3f(0.75f, 0.75f, 0.75f);
        glNormal3f(0, 0, -1);

        glTexCoord2f(0, 0);
        glVertex2d(0, 0);

        glTexCoord2f(1, 0);
        glVertex2d(w, 0);

        glTexCoord2f(1, 1);
        glVertex2d(w, h);

        glTexCoord2f(0, 1);
        glVertex2d(0, h);

        glEnd();

        gameController.unbindTexture("menu");
    }

    private void drawGameInfo(double w, double h) {
        // text:
        glColor3f(0.0f, 1.0f, 0.0f);
//        glRasterPos2i(10, 10);
//        glWindowPos2d(20, 30);
//        log.info("Попытка рисования строки через GLUT в потоке {}...", Thread.currentThread().getName());
//        Constants.getGlut().glutBitmapString(GLUT.BITMAP_HELVETICA_12, "frame [ms]: " + 123 + " (max=" + 321 + ")");
    }
}
