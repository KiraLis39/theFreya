package game.freya.gui;

public class LoadGameRenderer {
//    private Texture tipTexture;

//    private BufferedImage pBuf;

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
////		drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint)  //src - null, dst - равный размерам канваса.
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
}
