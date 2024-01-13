package game.freya.gui.panes;

import game.freya.GameController;
import game.freya.enums.other.ScreenType;
import game.freya.gl.RenderScreen;
import game.freya.gui.WindowManager;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Rectangle2D;

import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRasterPos2i;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3d;
import static org.lwjgl.opengl.GL14.glWindowPos2d;

@Slf4j
public class Menu extends RenderScreen {
    private final GameController gameController;

    private final WindowManager windowManager;

    private static final ScreenType type = ScreenType.MENU_SCREEN;

    private double widthMemory = -1;

    private volatile double leftShift, upShift, downShift, verticalSpace = -1, btnHeight, btnWidth;

    public Menu(WindowManager windowManager, GameController gameController) {
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
        drawGrayCorner(w, h);
        drawMenu(w, h);
        drawGameInfo(w, h);
//        drawDebug(w, h, null);

        // text:
        glColor3f(0.0f, 1.0f, 0.0f);
        glRasterPos2i(10, 10);
        glWindowPos2d(20, 30);
        //glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "frame [ms]: " + 123 + " (max=" + 321 + ")");
//        for (char c : "Respect mah authoritah!".toCharArray()) {
//            glutBitmapCharacter(font, c);
//        }

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

    private void drawGrayCorner(double w, double h) {
        // glEnable(GL_BLEND);

        glBegin(GL_QUADS);

        glColor4f(0.0f, 0.0f, 0.0f, 0.8f);

        glVertex2d(0.0f, 0.0f);
        glVertex2d(w / 3.5f, 0);
        glVertex2d(w / 4.0f, h);
        glVertex2d(0, h);

        glEnd();

        // glDisable(GL_BLEND);
    }

    private void drawMenu(double w, double h) {
        if (w != widthMemory) {
            verticalSpace = 8.5f;
            btnHeight = h * 0.08f;
            btnWidth = w * 0.18f;
            leftShift = w * 0.0225f;
            upShift = h * 0.15f;
            downShift = 25.0f;
            widthMemory = w;
        }

        if (gameController.isTextureExist("metallicBtnOFF")) {
            gameController.bindTexture("metallicBtnOFF");
        }

        glBegin(GL_QUADS);

        glColor4f(0.2f, 0.4f, 0.75f, 0.5f);
        glNormal3f(0, 0, -1);

        drawButton("start", new Rectangle2D.Double(leftShift, upShift, btnWidth, btnHeight));
        drawButton("net game", new Rectangle2D.Double(leftShift, upShift + (verticalSpace + btnHeight),
                btnWidth, btnHeight));
        drawButton("options", new Rectangle2D.Double(leftShift, upShift + (verticalSpace + btnHeight) * 2,
                btnWidth, btnHeight));
        drawButton("something else", new Rectangle2D.Double(leftShift, upShift + (verticalSpace + btnHeight) * 3,
                btnWidth, btnHeight));
        drawButton("something else", new Rectangle2D.Double(leftShift, upShift + (verticalSpace + btnHeight) * 4,
                btnWidth, btnHeight));

        glNormal3f(0, 0, 1);
        drawButton("exit", new Rectangle2D.Double(leftShift, h - downShift - btnHeight,
                btnWidth, btnHeight));

        glEnd();

        gameController.unbindTexture("metallicBtnOFF");
    }

    private void drawButton(String text, Rectangle2D btnRect) {
        glTexCoord2f(0, 0);
        glVertex3d(btnRect.getX(), btnRect.getY(), 0.2f);

        glTexCoord2f(1, 0);
        glVertex3d(btnRect.getX() + btnRect.getWidth(), btnRect.getY(), 0.2f);

        glTexCoord2f(1, 1);
        glVertex3d(btnRect.getX() + btnRect.getWidth(), btnRect.getY() + btnRect.getHeight(), 0.2f);

        glTexCoord2f(0, 1);
        glVertex3d(btnRect.getX(), btnRect.getY() + btnRect.getHeight(), 0.2f);
    }

    private void drawGameInfo(double w, double h) {

    }
}
