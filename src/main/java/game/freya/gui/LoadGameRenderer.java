package game.freya.gui;

import org.lwjgl.openvr.Texture;

import java.awt.image.BufferedImage;

public class LoadGameRenderer {
    private static final float tipSizeModificator = 1.0f;

    private Texture tipTexture;

    private BufferedImage pBuf;

    private void gameFon() {
        // glu.gluLookAt(0, 0, 5, 0, 0, 0, 0, 1, 0);
        // glu.gluPerspective(80.0, aspect, 0.1, 10.0); // fovy, aspect, zNear, zFar
        // glFrustum(45.0, 45.0, 45.0, 45.0, 0.1, 100.0);
        // if (w <= h) {
        //      glOrtho(-1.0, 1.0, -1.0 / aspect, 1.0 / aspect, -1.0, 1.0); //
        //      aspect <= 1
        // } else {
        //      glOrtho(-1.0 * aspect, 1.0 * aspect, -1.0, 1.0, -1.0, 1.0); //
        //      aspect > 1
        // }

//        glBegin(GL_QUADS);
//        // Front
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-size / 2, -size / 2, size / 2);
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(size / 2, -size / 2, size / 2);
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(size / 2, size / 2, size / 2);
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-size / 2, size / 2, size / 2);
//
//        // Top Face
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-size / 2, size / 2, -size / 2);
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-size / 2, size / 2, size / 2);
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(size / 2, size / 2, size / 2);
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(size / 2, size / 2, -size / 2);
//
//        // Bottom Face
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(-size / 2, -size / 2, -size / 2);
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(size / 2, -size / 2, -size / 2);
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(size / 2, -size / 2, size / 2);
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(-size / 2, -size / 2, size / 2);
//
//        // Right face
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(size / 2, -size / 2, -size / 2);
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(size / 2, size / 2, -size / 2);
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(size / 2, size / 2, size / 2);
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(size / 2, -size / 2, size / 2);
//
//        // Left Face
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-size / 2, -size / 2, -size / 2);
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(-size / 2, -size / 2, size / 2);
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(-size / 2, size / 2, size / 2);
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-size / 2, size / 2, -size / 2);
//
//        // Back
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(-size / 2, -size / 2, -size / 2);
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(-size / 2, size / 2, -size / 2);
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(size / 2, size / 2, -size / 2);
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(size / 2, -size / 2, -size / 2);
//
//        glEnd();
    }

    private void gameMenu() {
//        glBegin(GL_QUADS);
//
//        glNormal3f(0.0f, 0.0f, 1.0f);
//        glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-menuSize, -menuSize / 2, menuSize / 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(menuSize, -menuSize / 2, menuSize / 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(menuSize, menuSize / 2, menuSize / 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-menuSize, menuSize / 2, menuSize / 2);
//
//        glEnd();
    }

    private void foxCube() {
//        glBegin(GL_QUADS);
//        glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
//
//        // Back
//        glNormal3f(0, 0, -1);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex4f(modelSize, -modelSize, -modelSize, 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex4f(-modelSize, -modelSize, -modelSize, 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex4f(-modelSize, modelSize, -modelSize, 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex4f(modelSize, modelSize, -modelSize, 2);
//
//        // Left Face
//        glNormal3f(-1, 0, 0);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex4f(-modelSize, -modelSize, -modelSize, 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex4f(-modelSize, -modelSize, modelSize, 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex4f(-modelSize, modelSize, modelSize, 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex4f(-modelSize, modelSize, -modelSize, 2);
//
//        // Bottom
//        glNormal3f(0, -1, 0);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-modelSize / 2, -modelSize / 2, -modelSize / 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(modelSize / 2, -modelSize / 2, -modelSize / 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(modelSize / 2, -modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-modelSize / 2, -modelSize / 2, modelSize / 2);
//
//        // Right face
//        glNormal3f(1, 0, 0);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex4f(modelSize, -modelSize, modelSize, 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex4f(modelSize, -modelSize, -modelSize, 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex4f(modelSize, modelSize, -modelSize, 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex4f(modelSize, modelSize, modelSize, 2);
//
//        // Top
//        glNormal3f(0, 1, 0);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-modelSize / 2, modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(modelSize / 2, modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(modelSize / 2, modelSize / 2, -modelSize / 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-modelSize / 2, modelSize / 2, -modelSize / 2);
//
//        // Front
//        glNormal3f(0, 0, 1);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex4f(-modelSize, -modelSize, modelSize, 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex4f(modelSize, -modelSize, modelSize, 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex4f(modelSize, modelSize, modelSize, 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex4f(-modelSize, modelSize, modelSize, 2);
//
//        glEnd();
    }

    private void gameLabel() {
//        glBegin(GL_QUADS);
//
//        glNormal3f(0.0f, 0.0f, 1.0f);
//        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//
//        glTexCoord2f(0.0f, 0.0f);
//        glVertex3f(-modelSize, -modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(1.0f, 0.0f);
//        glVertex3f(modelSize, -modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(1.0f, 1.0f);
//        glVertex3f(modelSize, modelSize / 2, modelSize / 2);
//
//        glTexCoord2f(0.0f, 1.0f);
//        glVertex3f(-modelSize, modelSize / 2, modelSize / 2);
//
//        glEnd();
    }

    // *** *** ***

    public void display() {
//        //OPAQUE:
//        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity(); // reset projection matrix
//        drawBackTexture(gl2);
//
//        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity(); // reset projection matrix
//        glu.gluOrtho2D(-1.00, 1.00, -1.00, 1.00); // Задаём отображаемый куб
//        glu.gluPerspective(45.0, aspect, 0.001, 10.0); // fovy, aspect, zNear, zFar
//        glu.gluLookAt(0.00, 0.00, -1.00, 0.00, 0.00, 0.00, 0.00, 1.00, 0.00);
//        glViewport(0, 0, (int) canvasWidth, (int) canvasHeight);
//
//
//        //TRANSPARENT:
//        drawYellowOval(gl2);
//
//        drawStrings(gl2);
//
//        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity(); // reset projection matrix
//        glu.gluOrtho2D(-1.00, 1.00, -1.00, 1.00); // Задаём отображаемый куб
//
//        drawTips(drawable);
//
//        glEnd();
    }

    private void drawBackTexture() {
//        glEnable(GL_TEXTURE_2D);
//        glBindTexture(GL_TEXTURE_2D, textures.get(0));
//
////		glRotatef(testW / 60, 0.02f, 0.001f, 0.00f);
////		glTranslatef(-0.05f + testH / 1000, 0.05f - testW / 1200, -0.15f);
////		glTexImage2D(
////				GL_TEXTURE_2D,    			//Always GL_TEXTURE_2D
////                0,                            					//0 for now
////                GL_RGB,                       		//Format OpenGL uses for image
////                getWidth(), getHeight(),  			//Width and height
////                0,                            					//The border of the image
////                GL_RGB, 							//GL_RGB, because pixels are stored in RGB format
////                GL_UNSIGNED_BYTE, 		//GL_UNSIGNED_BYTE, because pixels are stored as unsigned numbers
////                null											//The actual pixel data
////        );
////		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.res);
////		drawBitmap (Bitmap bitmap, Rect src, Rect dst, Paint paint)  //src - null, dst - равный размерам канваса.
//
//        glColor4f(1.00f, 1.00f, 1.00f, 1.00f);
//        glBegin(GL_QUADS);
//
//        glTexCoord3f(0.0f, 0.0f, -1.0f);
//        glVertex2f(-1.0f, -1.0f);
//        glTexCoord3f(1.0f, 0.0f, -1.0f);
//        glVertex2f(1.0f, -1.0f);
//        glTexCoord3f(1.0f, 1.0f, -1.0f);
//        glVertex2f(1.0f, 1.0f);
//        glTexCoord3f(0.0f, 1.0f, -1.0f);
//        glVertex2f(-1.0f, 1.0f);
//
//        glEnd();
//        glFlush();
//        glDisable(GL_TEXTURE_2D);
    }

    private void drawStrings() {
//        glColor4f(0.00f, 0.00f, 0.00f, 0.50f);
//        glWindowPos2f(8.00f, 19.00f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Press any key to continue...");
//
//        glColor4f(0.75f, 0.75f, 0.75f, 0.75f);
//        glWindowPos2f(10.00f, 20.00f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Press any key to continue...");
//
//
//        glColor4f(0.75f, 0.75f, 1.00f, 1.00f);
//        glRasterPos3f(-0.50f, 0.50f, 0.50f);
//        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Initialized display");
//        glRasterPos3f(-0.50f, 0.45f, 0.50f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Display initialization");
//
//        glEnd();
//        glFlush();
    }

    private void drawTips() {
//        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
//            ImageIO.write(pBuf, "png", os);
//            pBuf.flush();
//            tipTexture = TextureIO.newTexture(new ByteArrayInputStream(os.toByteArray()), true, "png");
////			BufferedImage bigImage = GraphicsUtilities.createThumbnail(ImageIO.read(file), 300);
//            ImageInputStream bigInputStream = ImageIO.createImageInputStream(pBuf);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    void qualityTipDraw() {
//        glEnable(GL_TEXTURE_2D);
//        try {
//            glBindTexture(GL_TEXTURE_2D, tipTexture.getTextureObject(gl2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        modificatorW = (2.0f - tipTexture.getWidth() / canvasWidth * tipSizeModificator);
//        modificatorH = (2.0f - tipTexture.getHeight() / canvasHeight * tipSizeModificator);
//
//        glBegin(GL_QUADS);
//        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//
//        glTexCoord3f(0.0f, 0.0f, 1.0f);
//        glVertex2f(-0.99f, -1.00f + modificatorH);
//        glTexCoord3f(1.0f, 0.0f, 1.0f);
//        glVertex2f(0.99f - modificatorW, -1.00f + modificatorH);
//        glTexCoord3f(1.0f, 1.0f, 1.0f);
//        glVertex2f(0.99f - modificatorW, 0.99f);
//        glTexCoord3f(0.0f, 1.0f, 1.0f);
//        glVertex2f(-0.99f, 0.99f);
//
//        glEnd();
//        glFlush();
//
//        glDisable(GL_TEXTURE_2D);
    }
}
