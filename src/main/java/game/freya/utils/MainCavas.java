package game.freya.utils;

import org.lwjgl.openvr.Texture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainCavas { // extends GLCanvas implements GLEventListener {
//    public static FilterMode filter = FilterMode.NEAREST;

//    private static GLEventListener glList;

    private static final float tipSizeModificator = 1.0f;

    private static DecorClass decor;

    private final ArrayList<Integer> textures = new ArrayList<>();

    private final float testW = 0.10f;

    private final float testH = 0.30f;

    private final boolean moveVectorW = false;

//    private GLU glu;
//
//    private GLUT glut;
//
//    private GLUquadric gQ;
//
//    private TextRenderer textRenderer;

    private final boolean moveVectorH = false;

    Random r;

    byte[] tipBytes;

    int oldLength = 0;

    float tmpFloat = 0.25f;

    private Texture tipTexture, backTexture;

    private ByteArrayOutputStream os;

    private BufferedImage tBuf, pBuf;

    private int fps, frames;

    private long t0, t1;

    private float modificatorW;

    private float modificatorH;

    private float canvasWidth;

    private float canvasHeight;

    private float glVersion;

    private float gluVersion;

    private float aspect;

//    public MainCavas(TipSizeModificator norm, FilterMode nearest) {
////        glList = this;
//        setTipSize(norm);
//        setFilter(nearest);
//
//        decor = new DecorClass();
//    }

//    @Override
//    public void init(GLAutoDrawable drawable) {
//        Out.Print(MainCavas.class, 1, "GL initialization...");
//
//        r = new Random();
//        glu = new GLU();
//        glut = new GLUT();
//        gQ = glu.gluNewQuadric();
//        GL2 gl2 = drawable.getGL().getGL2();
////		GL2ES2 gl2es2 = drawable.getGL().getGL2ES2();
//        textRenderer = new TextRenderer(FONT0, true, true);
//        decor.Capabilities = new GLCapabilities(gl2.getGLProfile());
//
//        glVersion = Float.parseFloat(gl2.glGetString(GL2.GL_VERSION).substring(0, 3));
//        gluVersion = Float.parseFloat(glu.gluGetString(GLU.GLU_VERSION));
//        if (glVersion < 1.3f) {
//            Out.Print(MainCavas.class, 3, "\nOpenGL version " + glVersion + " < 1.3, some features may not work and program may crash\n");
//        }
//
//        try {
//            backTexture = TextureIO.newTexture(textureFile, true);
//            textures.add(0, backTexture.getTextureObject(gl2));
//        } catch (GLException | IOException e) {
//            e.printStackTrace();
//        }
//
//        decor.tune(drawable);
//
//        aspect = canvasWidth / canvasHeight;
//    }
//
//    @Override
//    public void dispose(GLAutoDrawable arg0) {
//        Out.Print(MainCavas.class, 2, "GL ended work...");
//    }
//
//    @Override
//    public void display(GLAutoDrawable drawable) {
//        GL2 gl2 = drawable.getGL().getGL2();
//        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
//
//        //DRAW:
//        {
//            //OPAQUE:
//            gl2.glMatrixMode(GL2.GL_PROJECTION);
//            gl2.glLoadIdentity(); // reset projection matrix
//            drawBackTexture(gl2);
//
//            gl2.glMatrixMode(GL2.GL_PROJECTION);
//            gl2.glLoadIdentity(); // reset projection matrix
//            glu.gluOrtho2D(-1.00, 1.00, -1.00, 1.00); // Задаём отображаемый куб
//            glu.gluPerspective(45.0, aspect, 0.001, 10.0); // fovy, aspect, zNear, zFar
//            glu.gluLookAt(0.00, 0.00, -1.00, 0.00, 0.00, 0.00, 0.00, 1.00, 0.00);
//            gl2.glViewport(0, 0, (int) canvasWidth, (int) canvasHeight);
//
//            //OPAQUE:
//
//
//            //TRANSPARENT:
//            drawYellowOval(gl2);
//
//            drawStrings(gl2);
//
//
//            gl2.glMatrixMode(GL2.GL_PROJECTION);
//            gl2.glLoadIdentity(); // reset projection matrix
//            glu.gluOrtho2D(-1.00, 1.00, -1.00, 1.00); // Задаём отображаемый куб
//
//            drawTips(drawable);
//
//            //OTHER:
//            drawFPS();
//        }
//        //END OF DRAW.
//
//        gl2.glEnd();
//        gl2.glFlush();
//    }
//
//    @Override
//    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
//        Out.Print(MainCavas.class, 1, "GL reshaping...");
//
//        canvasWidth = MainCavas.this.getWidth();
//        canvasHeight = MainCavas.this.getHeight();
//        aspect = canvasWidth / canvasHeight;
//    }
//
//    private void drawBackTexture(GL2 gl2) {
//        gl2.glEnable(GL2.GL_TEXTURE_2D);
//        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(0));
//
////		gl2.glRotatef(testW / 60, 0.02f, 0.001f, 0.00f);
////		gl2.glTranslatef(-0.05f + testH / 1000, 0.05f - testW / 1200, -0.15f);
////		gl2.glTexImage2D(
////				GL2.GL_TEXTURE_2D,    			//Always GL_TEXTURE_2D
////                0,                            					//0 for now
////                GL2.GL_RGB,                       		//Format OpenGL uses for image
////                getWidth(), getHeight(),  			//Width and height
////                0,                            					//The border of the image
////                GL2.GL_RGB, 							//GL_RGB, because pixels are stored in RGB format
////                GL2.GL_UNSIGNED_BYTE, 		//GL_UNSIGNED_BYTE, because pixels are stored as unsigned numbers
////                null											//The actual pixel data
////        );
////		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.res);
////		drawBitmap (Bitmap bitmap, Rect src, Rect dst, Paint paint)  //src - null, dst - равный размерам канваса.
//        gl2.glColor4f(1.00f, 1.00f, 1.00f, 1.00f);
//        gl2.glBegin(GL2.GL_QUADS);
//
//        gl2.glTexCoord3f(0.0f, 0.0f, -1.0f);
//        gl2.glVertex2f(-1.0f, -1.0f);
//        gl2.glTexCoord3f(1.0f, 0.0f, -1.0f);
//        gl2.glVertex2f(1.0f, -1.0f);
//        gl2.glTexCoord3f(1.0f, 1.0f, -1.0f);
//        gl2.glVertex2f(1.0f, 1.0f);
//        gl2.glTexCoord3f(0.0f, 1.0f, -1.0f);
//        gl2.glVertex2f(-1.0f, 1.0f);
//
//        gl2.glEnd();
//        gl2.glFlush();
//        gl2.glDisable(GL2.GL_TEXTURE_2D);
//    }
//
//    private void drawYellowOval(GL2 gl2) {
////		tmpFloat = r.nextFloat();
//        if (moveVectorW) {
//            testW += tmpFloat;
//        } else {
//            testW -= tmpFloat;
//        }
//        if (moveVectorH) {
//            testH += tmpFloat;
//        } else {
//            testH -= tmpFloat;
//        }
//        if (testW < -300.00f || testW > 300.0f) {
//            moveVectorW = !moveVectorW;
//        }
//        if (testH < -300.00f || testH > 300.0f) {
//            moveVectorH = !moveVectorH;
//        }
//
////		gl2.glPolygonOffset(factor, units);
////		gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
////		gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
////		gl2.glFrontFace(GL2.GL_CW); //отменить: glFrontFace(GL_CCW)
//
////		gl2.glEnable(GL2.GL_CULL_FACE);
////		gl2.glСullFace(GL2.GL_FRONT); // gl2.glСullFace(GL2.GL_BACK) по умолчанию.
//
//
////		gl2.glTranslatef(0.25f - testW / 1800, 	-0.25f + testH / 1800, 	1.00f - testW / testH);
//
////		gl2.glRotatef(Math.abs(testW - testH), 	0.75f, 	-0.50f + testW / testH, 	0.00f);
//        gl2.glRotatef(45, 0, 0, 1);
//        glu.gluQuadricDrawStyle(gQ, GLU.GLU_FILL);
//
//        gl2.glColor4f(0.00f, 0.00f, 1.00f, 1.00f);
//
//        glu.gluSphere(gQ, testW / testH / 12, 16, 16);
//        glu.gluDeleteQuadric(gQ);
//
////		gl2.glRotatef(0 - testW * 1.25f, 		0.00f, 		0.50f + testW / 100, 		0.50f);
//        glu.gluQuadricDrawStyle(gQ, GLU.GLU_POINT);
//
//        gl2.glColor4f(0.0f, 1.00f, 0.00f, 1.00f);
//
//        glu.gluSphere(gQ, testW / testH / 4 + testW / 2000, 64, 32);
//        glu.gluDeleteQuadric(gQ);
//
////		gl2.glRotatef(0 - testW * 1.5f, 		0.00f, 		0.75f - testW / 90, 		0.75f);
//        glu.gluQuadricDrawStyle(gQ, GLU.GLU_LINE);
//
//        gl2.glColor4f(1.00f, 0.00f, 0.00f, 1.00f);
//
//        glu.gluSphere(gQ, testW / testH / 8, 32, 8);
//        glu.gluDeleteQuadric(gQ);
//
//
//        gl2.glEnd();
//        gl2.glFlush();
////		gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
//    }
//
//    private void drawStrings(GL2 gl2) {
//        gl2.glColor4f(0.00f, 0.00f, 0.00f, 0.50f);
//        gl2.glWindowPos2f(8.00f, 19.00f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Press any key to continue...");
//
//        gl2.glColor4f(0.75f, 0.75f, 0.75f, 0.75f);
//        gl2.glWindowPos2f(10.00f, 20.00f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Press any key to continue...");
//
//
//        gl2.glColor4f(0.75f, 0.75f, 1.00f, 1.00f);
//        gl2.glRasterPos3f(-0.50f, 0.50f, 0.50f);
//        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Initialized display");
//        gl2.glRasterPos3f(-0.50f, 0.45f, 0.50f);
//        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Display initialization");
//
//        gl2.glEnd();
//        gl2.glFlush();
//    }
//
//    private void drawTips(GLAutoDrawable drawable) {
//        GL2 gl2 = drawable.getGL().getGL2();
//        if ((tBuf = StartMenu.getPaintBuffer()) != null) {
//            if (tBuf != pBuf) {
//                try {
//                    pBuf = tBuf;
//                    os = new ByteArrayOutputStream() {
//                        @Override
//                        public synchronized byte[] toByteArray() {
//                            return this.buf;
//                        }
//                    };
//                    ImageIO.write(pBuf, "png", os);
//                    pBuf.flush();
//                    tipTexture = TextureIO.newTexture(new ByteArrayInputStream(os.toByteArray()), true, "png");
//                    os.close();
////					BufferedImage bigImage = GraphicsUtilities.createThumbnail(ImageIO.read(file), 300);		ImageInputStream bigInputStream = ImageIO.createImageInputStream(pBuf);
//                } catch (GLException | IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        } else {
//            tipTexture = null;
//        }
//        if (tipTexture != null) {
//            qualityTipDraw(gl2, modificatorW, modificatorH);
//        }
//    }
//
//    void qualityTipDraw(GL2 gl2, float modificatorW, float modificatorH) {
//        gl2.glEnable(GL2.GL_TEXTURE_2D);
//        try {
//            gl2.glBindTexture(GL2.GL_TEXTURE_2D, tipTexture.getTextureObject(gl2));
//        } catch (GLException e) {
//            e.printStackTrace();
//        }
//
//        modificatorW = (2.0f - tipTexture.getWidth() / canvasWidth * tipSizeModificator);
//        modificatorH = (2.0f - tipTexture.getHeight() / canvasHeight * tipSizeModificator);
//
//        gl2.glBegin(GL2.GL_QUADS);
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//
//        gl2.glTexCoord3f(0.0f, 0.0f, 1.0f);
//        gl2.glVertex2f(-0.99f, -1.00f + modificatorH);
//        gl2.glTexCoord3f(1.0f, 0.0f, 1.0f);
//        gl2.glVertex2f(0.99f - modificatorW, -1.00f + modificatorH);
//        gl2.glTexCoord3f(1.0f, 1.0f, 1.0f);
//        gl2.glVertex2f(0.99f - modificatorW, 0.99f);
//        gl2.glTexCoord3f(0.0f, 1.0f, 1.0f);
//        gl2.glVertex2f(-0.99f, 0.99f);
//
//        gl2.glEnd();
//        gl2.glFlush();
//
//        gl2.glDisable(GL2.GL_TEXTURE_2D);
//    }
//
//    private void drawFPS() {
//        frames++;
//        t1 = System.currentTimeMillis();
//
//        if (t1 - t0 >= 1000) {
//            fps = frames;
//            t0 = t1;
//            frames = 0;
//        }
//
//        textRenderer.beginRendering((int) canvasWidth, (int) canvasHeight);
//
//        textRenderer.setColor(Color.RED);
//        textRenderer.draw(
//				fps + ":FPS",
//                (int) ((canvasWidth / 2) - ffb.getStringBounds(getGraphics(), ":FPS").getWidth() * 0.85f),
//                (int) (canvasHeight - 50)
//        );
//        textRenderer.draw(
//				"GL: " + glVersion + "   GLU: " + gluVersion,
//                (int) ((canvasWidth / 2) - ffb.getStringBounds(getGraphics(), ("GL: " + glVersion + "   GLU: " + gluVersion)).getWidth() * 0.75f),
//                (int) (canvasHeight - 30)
//        );
//
//        textRenderer.endRendering();
//        textRenderer.flush();
//    }
//
//    public void saveGLScreenshot() {
//        int WIDTH = getWidth();
//        int HEIGHT = getHeight();
//
//        System.out.println("0)");
//        // read current buffer
//        FloatBuffer imageData = BufferUtils.createFloatBuffer(WIDTH * HEIGHT * 3);
//        GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGB, GL11.GL_FLOAT, imageData);
//        imageData.rewind();
//
//        System.out.println("1)");
//        // fill rgbArray for BufferedImage
//        int[] rgbArray = new int[WIDTH * HEIGHT];
//        for (int y = 0; y < HEIGHT; ++y) {
//            for (int x = 0; x < WIDTH; ++x) {
//                int r = (int) (imageData.get() * 255) << 16;
//                int g = (int) (imageData.get() * 255) << 8;
//                int b = (int) (imageData.get() * 255);
//                int i = ((HEIGHT - 1) - y) * WIDTH + x;
//                rgbArray[i] = r + g + b;
//            }
//        }
////	    imageData = null;
//
//        System.out.println("2)");
//        // create and save image
//        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
//        image.setRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);
//
//        System.out.println("3)");
//        //check folder exist:
//        while (!new File("./screenshots/").exists()) {
//            try {
//                new File("./screenshots/").mkdirs();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println("4)");
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        File outputfile = new File("./screenshots/screenshot_" + dateFormat.format(System.currentTimeMillis()) + ".png");
//        try {
//            ImageIO.write(image, "png", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        image = null;
//    }
//
//    public void saveRobotScreenshot() {
//        SimpleDateFormat dateFormat;
//        BufferedImage screenBuffer;
//        File outputfile;
//
//        while (!new File("./screenshots/").exists()) {
//            try {
//                new File("./screenshots/").mkdirs();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        outputfile = new File("./screenshots/screenshot_" + dateFormat.format(System.currentTimeMillis()) + ".png");
//
//        try {
//            screenBuffer = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
//            ImageIO.write(screenBuffer, "png", outputfile);
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (HeadlessException e) {
//            e.printStackTrace();
//        } catch (AWTException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public GLEventListener getGLListener() {
//        return glList;
//    }
//
//    public void setTipSize(TipSizeModificator modificator) {
//        switch (modificator) {
//            case MIN:
//                tipSizeModificator = 0.75f;
//                break;
//            case SMALL:
//                tipSizeModificator = 1.00f;
//                break;
//            case NORM:
//                tipSizeModificator = 1.25f;
//                break;
//            case BIG:
//                tipSizeModificator = 1.50f;
//                break;
//            case MAX:
//                tipSizeModificator = 1.75f;
//                break;
//            default:
//        }
//    }
//
//    public void setFilter(FilterMode mode) {
//        filter = mode;
//    }
//
//    public enum FilterMode {NEAREST, LINEAR, MIPMAP, OPTIMAL}
//
//    public enum TipSizeModificator {MIN, SMALL, NORM, BIG, MAX}
}
