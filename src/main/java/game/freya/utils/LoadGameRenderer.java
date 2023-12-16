package game.freya.utils;

import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

public class LoadGameRenderer { // extends GLCanvas implements GLEventListener, MouseMotionListener, MouseListener {
    public static float xrot = 0.0f, fxrot = 0.0f, lxrot = 0.0f;

    public static float yrot = 0.0f, fyrot = 0.0f, lyrot = 0.0f;

    public static float zrot = 0.0f, fzrot = 0.0f, lzrot = 0.0f;

    public static float scX = 1.0f, scY = 1.0f, scZ = 1.0f, scM = 0.005f;

    public static int choiseBinds = 1;

    public static volatile int xW, yW;

    static float redFloat = 0.0f, greenFloat = 0.0f, blueFloat = 0.0f, opasityFloat = 0.0f, aspect;

    //    GL2 gl2;
//
//    GLU glu;
//    GLUquadric gQ;
    GLCapabilities Capabilities;

    //    TextureData data = null;
//    filterMode filter = filterMode.LINEAR;
    boolean lightningOn = true, textureOn = true, blendOn = true, Vsync = true, smooth = true;

    boolean colorMaterial = true, doubleBuffered = false, hardwareAccelerated = false, fog = false;

    boolean buttonStartOver = false, buttonStartPressed = false, buttonExitOver = false, buttonExitPressed = false;

    boolean scaleOver = false;

    float[] LightAmbient = {0.75f, 0.75f, 1.0f, 1};

    float[] AmbientPosition = {1.0f, 1.0f, 3.0f, 1};

    float[] AmbientIntensity = {0.75f, 0.75f, 1.0f, 1};

    float[] AmbientDirection = {-1, -1, -1};

    float[] DiffusePosition = {-1.0f, -1.0f, 3.0f, 1};

    float[] DiffuseIntensity = {0.75f, 0.75f, 1.0f, 1};

    float[] DiffuseSpecular = {1.0f, 1.0f, 1.0f, 1};

    float[] SpecularColor = {1.0f, 1.0f, 1.0f, 1};

    float[] fogcolor = {0.2f, 0.2f, 0.2f, 1}; // цвет тумана

    ArrayList<Integer> textures = new ArrayList<>();

    int w, h, depthMin = 0, depthMax = 100;

    public LoadGameRenderer() {
//        addMouseListener(this);
//        addMouseMotionListener(this);
//        addGLEventListener(this);
    }

//    @Override
//    public void init(GLAutoDrawable drawable) {
//        glu = new GLU();
//        gQ = glu.gluNewQuadric();
//        gl2 = drawable.getGL().getGL2();
//        Capabilities = new GLCapabilities(gl2.getGLProfile());
//
//        screenSizeTest();
//
//        decorations();
//
//        try {
//            textures.add(0, TextureIO.newTexture(ResourceManager.getFilesLink("textureTest"), true).getTextureObject(gl2));
//            textures.add(1, TextureIO.newTexture(ResourceManager.getFilesLink("textureTest2"), true).getTextureObject(gl2));
//            textures.add(2, TextureIO.newTexture(ResourceManager.getFilesLink("textureTest3"), true).getTextureObject(gl2));
//            textures.add(3, TextureIO.newTexture(ResourceManager.getFilesLink("textureTest4"), true).getTextureObject(gl2));
//            textures.add(4, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList0"), true).getTextureObject(gl2));
//            textures.add(5, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList1"), true).getTextureObject(gl2));
//            textures.add(6, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList2"), true).getTextureObject(gl2));
//            textures.add(7, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList0_0"), true).getTextureObject(gl2));
//            textures.add(8, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList1_0"), true).getTextureObject(gl2));
//            textures.add(9, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList2_0"), true).getTextureObject(gl2));
//            textures.add(10, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList3"), true).getTextureObject(gl2));
//            textures.add(11, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList4"), true).getTextureObject(gl2));
//            textures.add(12, TextureIO.newTexture(ResourceManager.getFilesLink("menuButtonsList5"), true).getTextureObject(gl2));
//        } catch (IOException exc) {
//            Out.Print(LoadGameRenderer.class, 3, "Проблема возникла при обработке текстур в центральном окне главного меню игры.");
//            exc.printStackTrace();
//        }
//    }
//
//    public void dispose(GLAutoDrawable drawable) {
//    }
//
//    @Override
//    public void display(GLAutoDrawable drawable) {
//        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
//        gl2.glLoadIdentity();
//
//        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//
//        bindTexture(2);
//        gameFon();
//
//        bindTexture(1);
//        foxCube();
//
//        gluTest();
//
//        bindTexture(0);
//        gameLabel();
//
//        bindTexture(4);
//        gameMenu();
//
//        bindTexture(10);
//        gameMenu2();
//
//        yrot += 0.025f;
//
//        gl2.glFlush();
//    }
//
//    @Override
//    public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
//        screenSizeTest();
//        setViewport();
//
//        gl2.glMatrixMode(GL2.GL_PROJECTION); // choose projection matrix
//        gl2.glLoadIdentity();
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity(); // reset
//    }
//
//    private void decorations() {
//        gl2.glClearColor(redFloat, greenFloat, blueFloat, opasityFloat);
//
//        if (Vsync) {
//            gl2.setSwapInterval(1);
//        }
//
//        Capabilities.setDoubleBuffered(doubleBuffered);
//        Capabilities.setHardwareAccelerated(hardwareAccelerated);
//
//        if (textureOn) {
//            gl2.glEnable(GL2.GL_TEXTURE_2D);
//        }
//
//        if (fog) {
//            gl2.glEnable(GL2.GL_FOG);
//            gl2.glFogfv(GL2.GL_FOG_COLOR, fogcolor, 0); // устанавливаем цвет тумана
//            gl2.glFogf(GL2.GL_FOG_DENSITY, 0.75f);
//        }
//
//        if (lightningOn) {
//            gl2.glEnable(GL2.GL_LIGHTING);
//            gl2.glEnable(GL2.GL_NORMALIZE);
//            gl2.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, LightAmbient, 0);
//
//            gl2.glEnable(GL2.GL_LIGHT0);
//
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, AmbientIntensity, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, AmbientIntensity, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, AmbientPosition, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, AmbientDirection, 0);
//
//            gl2.glLighti(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 0);
//            gl2.glLighti(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 90);
//
//            gl2.glEnable(GL2.GL_LIGHT1);
//
//            gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, AmbientIntensity, 0);
//            gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, DiffuseIntensity, 0);
//            gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, DiffusePosition, 0);
//            gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, DiffuseSpecular, 0);
//        }
//
//        if (colorMaterial) {
//            gl2.glEnable(GL2.GL_COLOR_MATERIAL);
//
//            gl2.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE); // разрешить	режим освещенности для двух граней gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
//
//            gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, SpecularColor, 0);
//            gl2.glMaterialf(GL2.GL_BACK, GL2.GL_SHININESS, 128);
//
//            /*
//             * GL_AMBIENT рассеянный свет GL_DIFFUSE тоже рассеянный свет,
//             * пояснения смотри ниже GL_SPECULAR отраженный свет GL_EMISSION
//             * излучаемый свет GL_SHININESS степень отраженного света
//             * GL_AMBIENT_AND_DIFFUSE оба рассеянных света
//             */
//        }
//
//        if (smooth) // интерполяция
//        {
//            gl2.glEnable(GL2.GL_SMOOTH);
//            gl2.glShadeModel(GL2.GL_SMOOTH);
//
//            gl2.glEnable(GL2.GL_LINE_SMOOTH);
//            gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
//
//            gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
//            gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
//        } else {
//            gl2.glEnable(GL2.GL_FLAT);
//            gl2.glShadeModel(GL2.GL_FLAT);
//        }
//
//        if (blendOn) {
//            gl2.glEnable(GL2.GL_BLEND);
//            gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
////			gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
//
//            if (gl2.glIsEnabled(GL2.GL_DEPTH_TEST)) // Буфер глубины или z-буфер	используется для удаления невидимых линий и поверхностей.
//            {
//                gl2.glDepthMask(false);
//                gl2.glDisable(GL2.GL_DEPTH_TEST);
//            }
//
//            gl2.glEnable(GL2.GL_ALPHA_TEST);
//        } else {
//            if (gl2.glIsEnabled(GL2.GL_BLEND)) {
//                gl2.glDisable(GL2.GL_BLEND);
//            }
//
//            gl2.glClearDepth(1.0f);
//            gl2.glDepthMask(true);
//            gl2.glDepthFunc(GL2.GL_LEQUAL);
//            gl2.glEnable(GL2.GL_DEPTH_TEST); // Буфер глубины или z-буфер используется для удаления невидимых линий и поверхностей.
//
//            /*
//             * Направление обхода вершин лицевых сторон можно изменить вызовом
//             * команды glFrontFace(GL_CW), а отменить с glFrontFace(GL_CCW)
//             *
//             * Чтобы изменить метод отображения многоугольника используется
//             * команда glPolygonMode(GLenum face, Glenum mode) Параметр mode
//             * определяет, как будут отображаться многоугольники, а параметр
//             * face устанавливает тип многоугольников, к которым будет
//             * применяться эта команда и может принимать следующие значения:
//             *
//             * GL_FRONT для лицевых граней GL_BACK для обратных граней
//             * GL_FRONT_AND_BACK для всех граней
//             *
//             * Параметр mode может быть равен: GL_POINT при таком режиме будут
//             * отображаться только вершины многоугольников. GL_LINE при таком
//             * режиме многоугольник будет представляться набором отрезков.
//             * GL_FILL при таком режиме многоугольники будут закрашиваться
//             * текущим цветом с учетом освещения и этот режим установлен по
//             * умолчанию.
//             *
//             * Кроме того, можно указывать, какой тип граней отображать на
//             * экране. Для этого сначала надо установить соответствующий режим
//             * вызовом команды glEnable(GL_CULL_FACE), а затем выбрать тип
//             * отображаемых граней с помощью команды glСullFace(GLenum mode)
//             *
//             * Вызов с параметром GL_FRONT приводит к удалению из изображения
//             * всех лицевых граней, а с параметром GL_BACK обратных (установка
//             * по умолчанию).
//             */
//
//            // gl2.glFrontFace(GL2.GL_CW);
//            // gl2.glEnable(GL2.GL_CULL_FACE);
//            // gl2.glCullFace(GL2.GL_FRONT);
//        }
//
//        if (filter.equals(filterMode.LINEAR)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
//        } else if (filter.equals(filterMode.NEAREST)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
//        } else if (filter.equals(filterMode.MIPMAP)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
//
//            gl2.glHint(GL2.GL_SAMPLES, 4);
//            gl2.glEnable(GL2.GL_MULTISAMPLE);
//        }
//
//        gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
//    }
//
//    private void gameFon() {
//        setViewport();
//
//        gl2.glMatrixMode(GL2.GL_PROJECTION);
//        gl2.glLoadIdentity();
//
//        // glu.gluLookAt(0, 0, 5, 0, 0, 0, 0, 1, 0);
//        glu.gluPerspective(80.0, aspect, 0.1, 10.0); // fovy, aspect, zNear, zFar
//        // gl2.glFrustum(45.0, 45.0, 45.0, 45.0, 0.1, 100.0);
//        // if (w <= h)
//        // {
//        // gl2.glOrtho(-1.0, 1.0, -1.0 / aspect, 1.0 / aspect, -1.0, 1.0); //
//        // aspect <= 1
//        // }
//        // else
//        // {
//        // gl2.glOrtho(-1.0 * aspect, 1.0 * aspect, -1.0, 1.0, -1.0, 1.0); //
//        // aspect > 1
//        // }
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity();
//
//        // gl2.glDepthRange(depthMin, depthMax);
//
//        gl2.glTranslatef(0.0f, 0.0f, -0.3f);
//
//        gl2.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
//        gl2.glRotatef(yrot, 0.0f, 1.0f, 1.0f);
//        // gl2.glRotatef(zrot, 0.0f, 0.0f, 1.0f);
//
//        float size = 1.0f;
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//
//        gl2.glBegin(GL2.GL_QUADS);
//
//        // Front
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(size / 2, -size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(size / 2, size / 2, size / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, size / 2, size / 2);
//
//        // Top Face
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(size / 2, size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(size / 2, size / 2, -size / 2);
//
//        // Bottom Face
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(size / 2, -size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(size / 2, -size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, size / 2);
//
//        // Right face
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(size / 2, -size / 2, -size / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(size / 2, size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(size / 2, size / 2, size / 2);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(size / 2, -size / 2, size / 2);
//
//        // Left Face
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, -size / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, size / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, size / 2, size / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, size / 2, -size / 2);
//
//        // Back
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(-size / 2, -size / 2, -size / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(-size / 2, size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(size / 2, size / 2, -size / 2);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(size / 2, -size / 2, -size / 2);
//
//        gl2.glEnd();
//    }
//
//    private void gameMenu() {
//        gl2.glMatrixMode(GL2.GL_PROJECTION);
//        gl2.glLoadIdentity();
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity();
//
//        gl2.glViewport(w / 4 - w / 25, h / 2 - h / 10, w / 2, h / 6);
//        gl2.glTranslatef(0.0f, -0.3f, 0.0f);
//
//        float menuSize = 1.0f;
//
//        gl2.glBegin(GL2.GL_QUADS);
//
//        gl2.glNormal3f(0.0f, 0.0f, 1.0f);
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-menuSize, -menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(menuSize, -menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(menuSize, menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-menuSize, menuSize / 2, menuSize / 2);
//
//        gl2.glEnd();
//    }
//
//    private void gameMenu2() {
//        gl2.glMatrixMode(GL2.GL_PROJECTION);
//        gl2.glLoadIdentity();
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity();
//
//        gl2.glViewport(w / 4 - w / 25, h / 2 - h / 4, w / 2, h / 6);
//        gl2.glTranslatef(0.0f, 0.0f, 0.0f);
//
//        float menuSize = 1.0f;
//
//        gl2.glBegin(GL2.GL_QUADS);
//
//        gl2.glNormal3f(0.0f, 0.0f, 1.0f);
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-menuSize, -menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(menuSize, -menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(menuSize, menuSize / 2, menuSize / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-menuSize, menuSize / 2, menuSize / 2);
//
//        gl2.glEnd();
//    }
//
//    private void foxCube() {
//        float modelSize = 0.25f;
//
//        gl2.glMatrixMode(GL2.GL_PROJECTION);
//        gl2.glLoadIdentity();
//
//        gl2.glViewport(0, 0, w, h);
//        gl2.glTranslatef(-0.6f, 0.4f, 0.0f);
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity();
//
//        gl2.glRotatef(fxrot, 1.0f, 0.0f, 0.0f);
//        gl2.glRotatef(fyrot, 0.0f, 1.0f, 0.0f);
//        gl2.glRotatef(fzrot, 0.0f, 0.0f, 1.0f);
//        gl2.glScalef(scX, scY, scZ);
//
//        gl2.glBegin(GL2.GL_QUADS);
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
//
//        // Back
//        gl2.glNormal3f(0, 0, -1);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex4f(modelSize, -modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex4f(-modelSize, -modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex4f(-modelSize, modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex4f(modelSize, modelSize, -modelSize, 2);
//
//        // Left Face
//        gl2.glNormal3f(-1, 0, 0);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex4f(-modelSize, -modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex4f(-modelSize, -modelSize, modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex4f(-modelSize, modelSize, modelSize, 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex4f(-modelSize, modelSize, -modelSize, 2);
//
//        // Bottom
//        gl2.glNormal3f(0, -1, 0);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-modelSize / 2,
//                -modelSize / 2, -modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(modelSize / 2,
//                -modelSize / 2, -modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(modelSize / 2,
//                -modelSize / 2, modelSize / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-modelSize / 2,
//                -modelSize / 2, modelSize / 2);
//
//        // Right face
//        gl2.glNormal3f(1, 0, 0);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex4f(modelSize, -modelSize, modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex4f(modelSize, -modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex4f(modelSize, modelSize, -modelSize, 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex4f(modelSize, modelSize, modelSize, 2);
//
//        // Top
//        gl2.glNormal3f(0, 1, 0);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-modelSize / 2,
//                modelSize / 2, modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(modelSize / 2, modelSize
//                / 2, modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(modelSize / 2, modelSize
//                / 2, -modelSize / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-modelSize / 2,
//                modelSize / 2, -modelSize / 2);
//
//        // Front
//        gl2.glNormal3f(0, 0, 1);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex4f(-modelSize, -modelSize, modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex4f(modelSize, -modelSize, modelSize, 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex4f(modelSize, modelSize, modelSize, 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex4f(-modelSize, modelSize, modelSize, 2);
//
//        fxrot += 0.1f;
//        fyrot += 0.1f;
//        fzrot += 0.1f;
//
//        scX += scM;
//        scY += scM;
//        scZ += scM;
//
//        scM += -scX / 100 + 0.01f;
//
//        gl2.glEnd();
//    }
//
//    private void gameLabel() {
//        gl2.glMatrixMode(GL2.GL_PROJECTION);
//        gl2.glLoadIdentity();
//
//        gl2.glMatrixMode(GL2.GL_MODELVIEW);
//        gl2.glLoadIdentity();
//
//        gl2.glViewport(w / 6 - w / 30, h / 2 + h / 30, w / 6 * 4, h / 3);
//        gl2.glTranslatef(0.0f, 0.0f, 0.2f);
//
//        float modelSize = 1.0f;
//
//        gl2.glBegin(GL2.GL_QUADS);
//
//        gl2.glNormal3f(0.0f, 0.0f, 1.0f);
//        gl2.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//        gl2.glTexCoord2f(0.0f, 0.0f);
//        gl2.glVertex3f(-modelSize, -modelSize / 2, modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 0.0f);
//        gl2.glVertex3f(modelSize, -modelSize / 2, modelSize / 2);
//        gl2.glTexCoord2f(1.0f, 1.0f);
//        gl2.glVertex3f(modelSize, modelSize / 2, modelSize / 2);
//        gl2.glTexCoord2f(0.0f, 1.0f);
//        gl2.glVertex3f(-modelSize, modelSize / 2, modelSize / 2);
//
//        gl2.glEnd();
//    }
//
//    private void gluTest() {
//        float modelSize = 0.23f;
//
//        gl2.glColor3f(scM, scM, scM);
//
//        glu.gluQuadricDrawStyle(gQ, GLU.GLU_LINE);
//        glu.gluSphere(gQ, modelSize, 20, 20);
//        glu.gluDeleteQuadric(gQ);
//    }
//
//    private void bindTexture(int bindNum) {
//        if (bindNum == 4) {
//            if (buttonStartOver) {
//                if (buttonStartPressed) {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(6));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(5));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(4));
//            }
//        } else if (bindNum == 7) {
//            if (buttonStartOver) {
//                if (buttonStartPressed) {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(9));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(8));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(7));
//            }
//        } else if (bindNum == 10) {
//            if (buttonExitOver) {
//                if (buttonExitPressed) {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(12));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(11));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(10));
//            }
//        } else {
//            gl2.glBindTexture(GL2.GL_TEXTURE_2D, textures.get(bindNum));
//        }
//    }
//
//    private void screenSizeTest() {
//        w = StartMenu.getStartMenuWidth();
//        h = StartMenu.getStartMenuHeight();
//    }
//
//    public void setViewport() {
//        if (w > h) {
//            aspect = w / h;
//        } else if (w < h) {
//            aspect = h / w;
//        } else {
//            aspect = 1;
//        }
//
//        gl2.glViewport(0, 0, StartMenu.getStartMenuWidth(), StartMenu.getStartMenuHeight());
//    }
//
//    private Boolean mouseOnButtonStart(int a, int b) {
//        return a > w / 4 - w / 25 && a < w - w / 10 * 3 &&
//                b > h / 40 * 14 && b < h / 2 - h / 19;
//    }
//
//    private Boolean mouseOnButtonExit(int a, int b) {
//        return a > w / 4 - w / 25 && a < w - w / 10 * 3 &&
//                b > h / 40 * 19 && b < h / 40 * 22;
//    }
//
//    public void mouseDragged(MouseEvent e) {
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e) {
//        buttonStartOver = mouseOnButtonStart(e.getX(), e.getY());
//        buttonExitOver = mouseOnButtonExit(e.getX(), e.getY());
//    }
//
//    public void mouseClicked(MouseEvent e) {
//    }
//
//    @Override
//    public void mousePressed(MouseEvent e) {
//        if (mouseOnButtonStart(e.getX(), e.getY())) {
//            buttonStartPressed = true;
////			new MiniGameFrame(); //потом удалить. Это только для быстрого перехода!
//            new RunGameFrame();
//            StartMenu.hideMainMenu();
////			Library.sEngineModule.startSound(ResourseManager.getAudioFile("soundTest"));
//        } else {
//            buttonStartPressed = false;
//        }
//
//        if (mouseOnButtonExit(e.getX(), e.getY())) {
//            buttonExitPressed = true;
//            int exit = JOptionPane.showConfirmDialog(null, "Закрыть игру и выйти?", "Подтверждение:",
//                    JOptionPane.YES_NO_OPTION, 0);
//
//            switch (exit) {
//                case 0:
//                    Out.Print(LoadGameRenderer.class, 0, "Выполнение выхода из игры.");
//                    ExitOut.emergencyExit(0);
//                    break;
//                case 1:
//                    break;
//                default:
//            }
////			Library.sEngineModule.startSound(ResourseManager.getAudioFile("soundTest"));
//        } else {
//            buttonExitPressed = false;
//        }
//
//        StartMenu.setMouse3DClk(e.getPoint());
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        buttonExitPressed = false;
//        buttonStartPressed = false;
//    }
//
//    public void mouseEntered(MouseEvent e) {
//    }
//
//    public void mouseExited(MouseEvent e) {
//    }
//
//    enum filterMode {LINEAR, NEAREST, MIPMAP}
}
